package com.timlis.web.admin;

import com.timlis.pojo.Tag;
import com.timlis.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/admin")
public class TagsController {

    @Autowired
    private TagService tagsService;

    @GetMapping("/tags")
    public String tags(@PageableDefault(size = 6, sort = {"id"}, direction = Sort.Direction.DESC)
                                Pageable pageable, Model model) {
        Page<Tag> tagPage = tagsService.listTag(pageable);
        model.addAttribute("tagPage", tagPage);
        return "admin/tags";

    }

    @GetMapping("/tags/input")
    public String tagsInput(Model model) {
        model.addAttribute("tag",new Tag());
        return "admin/tags-input";
    }

    @GetMapping("/tags/findById/{id}")
    public String findById(@PathVariable Long id,Model model){
        model.addAttribute("tag",tagsService.getTag(id));
        return "admin/tags-input";
    }

    @PostMapping("/tags/save")
    public String tagSave(@Valid Tag tag, BindingResult result,RedirectAttributes attributes) {
        Tag Tag1 = tagsService.getTagByName(tag.getName());
        if (Tag1 != null){
            result.rejectValue("name","nameError","该标签名称已存在！");
        }

        if (result.hasErrors()){

            return "admin/tags-input";
        }
        Tag t = tagsService.saveTag(tag);
        if (t == null) {
            attributes.addFlashAttribute("message", "添加失败");
        } else {
            attributes.addFlashAttribute("message", "添加成功");
        }
        return "redirect:/admin/tags";
    }

    @PostMapping("/tags/save/{id}")
    public String tagUpdate(@Valid Tag tag, BindingResult result, @PathVariable Long id, RedirectAttributes attributes) {


        Tag tag1 = tagsService.getTagByName(tag.getName());
        if (tag1 != null){
            result.rejectValue("name","nameError","该标签名称已存在！");
        }

        if (result.hasErrors()){

            return "admin/tags-input";
        }
        Tag t = tagsService.updateTag(tag);
        if (t == null) {
            attributes.addFlashAttribute("message", "更新失败");
        } else {
            attributes.addFlashAttribute("message", "更新成功");
        }
        return "redirect:/admin/tags";
    }

    @GetMapping("/tags/delete/{id}")
    public String deleteById(@PathVariable Long id,RedirectAttributes attributes){
        tagsService.deleteTag(id);
        attributes.addFlashAttribute("message", "删除成功");
        return "redirect:/admin/tags";
    }




}
