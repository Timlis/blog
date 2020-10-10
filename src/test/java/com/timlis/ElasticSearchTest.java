package com.timlis;

import com.timlis.dao.BlogRepository;
import com.timlis.dao.EsBlogRepository;
import com.timlis.document.EsBlog;
import com.timlis.pojo.Blog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Iterator;
import java.util.List;

@SpringBootTest
public class ElasticSearchTest {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private EsBlogRepository esBlogRepository;

    @Test
    public void testAdd(){
        List<Blog> blogList = blogRepository.findAll();
        for (Blog blog : blogList) {
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
        System.out.println("success....");
    }

//    @Test
//    public void testQuery(){
//        Iterator<EsBlog> iterator = esBlogRepository.findAll().iterator();
//        while (iterator.hasNext()){
//            System.out.println(iterator.next());
//        }
//    }
//
//    @Test
//    public void testQueryByKeyWord(){
//        Iterator<EsBlog> iterator = esBlogRepository.findByKeywords("spring",null).iterator();
//        while (iterator.hasNext()){
//            System.out.println(iterator.next());
//        }
//    }

//    @Test
//    public void testDelete(){
//        esBlogRepository.deleteAll();
//        System.out.println("done...");
//    }


}
