package com.timlis.web.admin;

import com.timlis.pojo.Blog;
import com.timlis.pojo.ClientBlog;
import com.timlis.pojo.User;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class BlogController {



    @Autowired
    private BlogService blogService;

    @Autowired
    private TypesService typesService;

    @Autowired
    private TagService tagService;

    @GetMapping("/blogs")
    public String blog(@PageableDefault(size = 5, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable, ClientBlog blog, Model model) {

        model.addAttribute("typeList", typesService.listType());
        model.addAttribute("pageBlog", blogService.listBlog(pageable, blog));
        return "admin/blogs";
    }

    @PostMapping("/blogs/search")
    public String search(@PageableDefault(size = 5, sort = {"updateTime"}, direction = Sort.Direction.DESC) Pageable pageable, ClientBlog blog, Model model) {

        model.addAttribute("pageBlog", blogService.listBlog(pageable, blog));
        return "admin/blogs :: blogList";
    }

    @GetMapping("/blogs/blogs-input")
    public String blogInput(Model model) {

        model.addAttribute("blog", new Blog());
        model.addAttribute("typeList", typesService.listType());
        model.addAttribute("tagList", tagService.listTag());

        return "admin/blogs-input";
    }

    @PostMapping("/blogs/blog-save")
    public String blogSave(Blog blog, RedirectAttributes attributes, HttpSession session) {
        blog.setUser((User) session.getAttribute("user"));
        blog.setType(typesService.getType(blog.getType().getId()));
        blog.setTags(tagService.listTag(blog.getTagIds()));
        Blog b;
        if (blog.getId() == null){
            b = blogService.saveBlog(blog);
        }else {
            b = blogService.updateBlog(blog.getId(),blog);
        }

        if (b != null) {
            attributes.addFlashAttribute("message", "添加失败");
        } else {
            attributes.addFlashAttribute("message", "添加成功");
        }
        return "redirect:/admin/blogs";
    }

    @GetMapping("/blogs/edit-input/{id}")
    public String updateSave(@PathVariable Long id,Model model) {

        model.addAttribute("typeList", typesService.listType());
        model.addAttribute("tagList", tagService.listTag());
        Blog blog = blogService.getBlog(id);
        blog.init();
        model.addAttribute("blog",blog);
        return "admin/blogs-input";


    }

    @GetMapping("/blogs/delete/{id}")
    public String delete(@PathVariable Long id,RedirectAttributes attributes){
        blogService.deleteBlog(id);
        attributes.addFlashAttribute("message","删除成功");
        return "redirect:/admin/blogs";
    }
}
