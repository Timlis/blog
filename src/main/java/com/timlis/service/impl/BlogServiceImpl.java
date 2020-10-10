package com.timlis.service.impl;

import com.timlis.dao.BlogRepository;
import com.timlis.dao.EsBlogRepository;
import com.timlis.document.EsBlog;
import com.timlis.exception.NotFoundException;
import com.timlis.pojo.Blog;
import com.timlis.pojo.ClientBlog;
import com.timlis.pojo.Type;
import com.timlis.service.BlogService;
import com.timlis.util.MarkDownUtils;
import com.timlis.util.MybeanUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class BlogServiceImpl implements BlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private EsBlogRepository esBlogRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Blog getBlog(Long id) {
        return blogRepository.getOne(id);
    }

    /**
     * 主页点击博客查看博客详细内容
     * @param id
     * @return
     */
    @Override
    public Blog getAndConvert(Long id) {
        Optional<Blog> optional = blogRepository.findById(id);
        Blog blog = optional.get();
        if (blog == null) {
            throw new NotFoundException("该博客不存在");
        }


        //更新浏览数
        Integer view = addViewFromRedis(id);
        System.out.println(id + " now views is " + view);
        blog.setViews(view);

        Blog b = new Blog();
        BeanUtils.copyProperties(blog, b);
        String context = b.getContent();
        b.setContent(MarkDownUtils.markdownToHtmlExtensions(context));
        return b;
    }

    /***
     * 设置views从redis中获取
     * @param blogPage
     * @return
     */
    private Page<Blog> setViews(Page<Blog> blogPage){
        List<Blog> content = blogPage.getContent();
        content.forEach(blog -> {blog.setViews(getViewFromRedis(blog.getId()));});
        return new PageImpl<>(content);
    }

    /**
     * 从Redis中获取浏览量并且+1，如果为null，则从数据库中获取
     * @param id
     * @return
     */
    private Integer addViewFromRedis(Long id) {
        if (redisTemplate.opsForValue().get(String.valueOf(id)) == null) {
            Optional<Blog> optional = blogRepository.findById(id);
            Integer views = optional.get().getViews();
            System.out.println("get view from mysql");
            redisTemplate.opsForValue().set(String.valueOf(id), views);
            redisTemplate.opsForValue().increment(String.valueOf(id),1);
        } else {
            System.out.println("get view from reids");
            redisTemplate.opsForValue().increment(String.valueOf(id),1);
        }
        return (Integer) redisTemplate.opsForValue().get(String.valueOf(id));
    }

    /**
     * 从Redis中获取浏览量并且，如果为null，则从数据库中获取
     * @param id
     * @return
     */
    private Integer getViewFromRedis(Long id) {
        if (redisTemplate.opsForValue().get(String.valueOf(id)) == null) {
            Optional<Blog> optional = blogRepository.findById(id);
            Integer views = optional.get().getViews();
            System.out.println("get view from mysql");
            redisTemplate.opsForValue().set(String.valueOf(id), views);
        }
        return (Integer) redisTemplate.opsForValue().get(String.valueOf(id));
    }

    /**
     * 搜索
     * @param pageable
     * @param blog
     * @return
     */
    @Override
    public Page<Blog> listBlog(Pageable pageable, ClientBlog blog) {
        return blogRepository.findAll(new Specification<Blog>() {
            @Override
            public Predicate toPredicate(Root<Blog> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (!"".equals(blog.getTitle()) && blog.getTitle() != null) {
                    predicates.add(criteriaBuilder.like(root.<String>get("title"), "%" + blog.getTitle() + "%"));
                }
                if (blog.getTypeId() != null) {
                    predicates.add(criteriaBuilder.equal(root.<Type>get("type"), blog.getTypeId()));

                }
                if (blog.isRecommend()) {
                    predicates.add(criteriaBuilder.equal(root.<Boolean>get("recommend"), blog.isRecommend()));
                }
                criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
                return null;
            }
        }, pageable);
    }

    /**
     * 主页显示所有博客列表
     * @param pageable
     * @return
     */
    @Override
    public Page<Blog> listBlog(Pageable pageable) {

        return setViews(blogRepository.findAll(pageable));
    }

    /**
     * 搜索
     * @param query
     * @param pageable
     * @return
     */
    @Override
    public Page<Blog> listBlog(String query, Pageable pageable) {
        return blogRepository.findByQuery(query, pageable);
    }


    @Override
    public Page<Blog> listBlog(Long tagId, Pageable pageable) {
        return blogRepository.findAll(new Specification<Blog>() {
            @Override
            public Predicate toPredicate(Root<Blog> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                Join join = root.join("tags");
                return cb.equal(join.get("id"), tagId);
            }
        }, pageable);
    }

    /**
     * 从ElsaticSerach中搜索博客
     * @param keywords
     * @param pageable
     * @return
     * @throws IOException
     */
    @Override
    public Page<EsBlog> listBlogFromEalsticSearch(String keywords, Pageable pageable) throws IOException {


        SearchRequest searchRequest = new SearchRequest("blogsindex");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //精确匹配
        String[] strings = new String[]{"title", "description"};
        MultiMatchQueryBuilder termQueryBuilder = QueryBuilders.multiMatchQuery(keywords, "title", "description");
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title").field("description");
        highlightBuilder.requireFieldMatch(true);  //多个单词高亮的话，要把这个设置为true
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);


        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        ///解析结果
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {

            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            HighlightField description = highlightFields.get("description");
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();  //没高亮的数据
            if (title != null) {
                System.out.println("title==" + title);
                Text[] fragments = title.fragments();
                String n_title = "";
                for (Text text : fragments) {
                    n_title += text;
                }
                sourceAsMap.put("title", n_title);
            }
            if (description != null) {
                System.out.println("description==" + description);
                Text[] fragments = description.fragments();
                String n_description = "";
                for (Text text : fragments) {
                    n_description += text;
                }
                sourceAsMap.put("description", n_description);
            }
            list.add(sourceAsMap);
        }

        Page<EsBlog> blogPage = new PageImpl(list, pageable, list.size());
        return blogPage;
    }

    /**
     * 列出推荐博客
     * @param size
     * @return
     */
    @Override
    public List<Blog> listRecommendBlog(Integer size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "updateTime");
        Pageable pageable = PageRequest.of(0, size, sort);
        return blogRepository.findTop(pageable);
    }


    /**
     * 添加博客
     * @param blog
     * @return
     */
    @Override
    public Blog saveBlog(Blog blog) {
        if (blog.getId() == null) {

            blog.setCreateTime(new Date());
            blog.setUpdateTime(new Date());
            blog.setViews(0);
        } else {
            blog.setUpdateTime(new Date());
        }
        addBlogsToElastic(blog);

        if (esBlogRepository.findById(blog.getId()) != null) {
            esBlogRepository.deleteById(blog.getId());
        }

        //添加到es
        EsBlog esBlog = new EsBlog();
        esBlog.setId(blog.getId());
        esBlog.setNickname(blog.getUser().getNickname());
        esBlog.setAvatar(blog.getUser().getAvatar());
        esBlog.setDescription(blog.getDescription());
        esBlog.setTitle(blog.getTitle());
        esBlog.setType(blog.getType().getName());
        esBlog.setViews(blog.getViews());
        esBlog.setUpdateTime(blog.getUpdateTime());
        esBlog.setFirstImage(blog.getFirstPicture());
        //更新es数据
        esBlogRepository.save(esBlog);

        return blogRepository.save(blog);
    }

    /**
     * 更新博客
     * @param id
     * @param blog
     * @return
     */
    @Override
    public Blog updateBlog(Long id, Blog blog) {
        Blog b = blogRepository.getOne(id);
        if (b == null) {
            throw new NotFoundException("该博客不存在！");
        }
        BeanUtils.copyProperties(blog, b, MybeanUtils.getNullPropertyNames(blog));
        b.setUpdateTime(new Date());
        //先删除旧值
        esBlogRepository.deleteById(id);

        EsBlog esBlog = new EsBlog();
        esBlog.setId(blog.getId());
        esBlog.setNickname(blog.getUser().getNickname());
        esBlog.setAvatar(blog.getUser().getAvatar());
        esBlog.setDescription(blog.getDescription());
        esBlog.setTitle(blog.getTitle());
        esBlog.setType(blog.getType().getName());
        esBlog.setViews(blog.getViews());
        esBlog.setUpdateTime(blog.getUpdateTime());
        esBlog.setFirstImage(blog.getFirstPicture());
        //更新es数据
        esBlogRepository.save(esBlog);

        return blogRepository.save(b);
    }

    /**
     * 删除博客
     * @param id
     */
    @Override
    public void deleteBlog(Long id) {
        esBlogRepository.deleteById(id);
        blogRepository.deleteById(id);
    }

    /**
     * 按时间归档博客
     * @return
     */
    @Override
    public Map<String, List<Blog>> archiveBlog() {
        List<String> years = blogRepository.findGroupYear();
        Map<String, List<Blog>> map = new HashMap<>();
        for (String year : years) {
            map.put(year, blogRepository.findByYear(year));
        }
        return map;
    }

    /**
     * 统计博客数量
     * @return
     */
    @Override
    public Long countBlog() {
        return blogRepository.count();
    }

    /**
     * 添加博客到ElasticSearch中
     * @param blog
     */
    private void addBlogsToElastic(Blog blog) {
        EsBlog esBlog = new EsBlog();
        esBlog.setId(blog.getId());
        esBlog.setNickname(blog.getUser().getNickname());
        esBlog.setAvatar(blog.getUser().getAvatar());
        esBlog.setDescription(blog.getDescription());
        esBlog.setTitle(blog.getTitle());
        esBlog.setType(blog.getType().getName());
        esBlog.setViews(blog.getViews());
        esBlog.setUpdateTime(blog.getUpdateTime());
        esBlog.setFirstImage(blog.getFirstPicture());
        esBlogRepository.save(esBlog);
    }
}
