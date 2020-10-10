package com.timlis.web;

import com.timlis.exception.NotFoundException;
import com.timlis.pojo.Blog;
import com.timlis.pojo.ClientBlog;
import com.timlis.service.BlogService;
import com.timlis.service.TagService;
import com.timlis.service.TypesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class IndexController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private TypesService typesService;

    @Autowired
    private TagService tagService;

    @GetMapping("/")
    public String index(@PageableDefault(size = 5, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable, Model model) {

        model.addAttribute("page", blogService.listBlog(pageable));
        model.addAttribute("types", typesService.listTypeTop(6));
        model.addAttribute("tags", tagService.listTagTop(10));
        model.addAttribute("recommendBlog", blogService.listRecommendBlog(8));
        return "index";
    }

//    @PostMapping("search")
//    public String search(@PageableDefault(size = 5, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable,
//                         @RequestParam String query, Model model) {
//
//        model.addAttribute("page", blogService.listBlog("%" + query + "%", pageable));
//        model.addAttribute("query",query);
//
//        return "search";
//    }

    @PostMapping("search")
    public String search(@PageableDefault(size = 5, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                         @RequestParam String query, Model model) throws IOException {

        model.addAttribute("page", blogService.listBlogFromEalsticSearch(query,pageable));
        model.addAttribute("query",query);

        return "search";
    }

    @GetMapping("/blog")
    public String blog() {

        return "blog";
    }

    @GetMapping("/blog/{id}")
    public String blog(@PathVariable Long id,Model model){

        model.addAttribute("blog",blogService.getAndConvert(id));
        return "blog";
    }


}