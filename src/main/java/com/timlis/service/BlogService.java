package com.timlis.service;

import com.timlis.document.EsBlog;
import com.timlis.pojo.Blog;
import com.timlis.pojo.ClientBlog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface BlogService {

    Blog getBlog(Long id);

    Blog getAndConvert(Long id);

    Page<Blog> listBlog(Pageable pageable, ClientBlog blog);

    Page<Blog> listBlog(Pageable pageable);

    Page<Blog> listBlog(String query,Pageable pageable);

    Page<EsBlog> listBlogFromEalsticSearch(String keywords,Pageable pageable) throws IOException;


    Page<Blog> listBlog(Long tagId,Pageable pageable);

    List<Blog> listRecommendBlog(Integer size);

    Blog saveBlog(Blog blog);

    Blog updateBlog(Long id,Blog blog);

    void deleteBlog(Long id);

    Map<String,List<Blog>> archiveBlog();

    Long countBlog();


}
