package com.timlis.web.admin;

import com.timlis.pojo.Type;
import com.timlis.service.TypesService;
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
public class TypesController {

    @Autowired
    private TypesService typesService;

    @GetMapping("/types")
    public String types(@PageableDefault(size = 6, sort = {"id"}, direction = Sort.Direction.DESC)
                                Pageable pageable, Model model) {
        Page<Type> typePage = typesService.listType(pageable);
        model.addAttribute("typePage", typePage);
        return "admin/types";

    }

    @GetMapping("/types/input")
    public String typesInput(Model model) {
        model.addAttribute("type",new Type());
        return "admin/types-input";
    }

    @GetMapping("/types/findById/{id}")
    public String findById(@PathVariable Long id,Model model){
        model.addAttribute("type",typesService.getType(id));
        return "admin/types-input";
    }

    @PostMapping("/types/save")
    public String typeSave(@Valid Type type, BindingResult result,RedirectAttributes attributes) {
        Type type1 = typesService.getTypeByName(type.getName());
        if (type1 != null){
            result.rejectValue("name","nameError","该分类名称已存在！");
        }

        if (result.hasErrors()){

            return "admin/types-input";
        }
        Type t = typesService.saveType(type);
        if (t == null) {
            attributes.addFlashAttribute("message", "添加失败");
        } else {
            attributes.addFlashAttribute("message", "添加成功");
        }
        return "redirect:/admin/types";
    }

    @PostMapping("/types/save/{id}")
    public String typeUpdate(@Valid Type type, BindingResult result, @PathVariable Long id, RedirectAttributes attributes) {


        Type type1 = typesService.getTypeByName(type.getName());
        if (type1 != null){
            result.rejectValue("name","nameError","该分类名称已存在！");
        }

        if (result.hasErrors()){

            return "qadmin/types-input";
        }
        Type t = typesService.updateType(type);
        if (t == null) {
            attributes.addFlashAttribute("message", "更新失败");
        } else {
            attributes.addFlashAttribute("message", "更新成功");
        }
        return "redirect:/admin/types";
    }

    @GetMapping("/types/delete/{id}")
    public String deleteById(@PathVariable Long id,RedirectAttributes attributes){
        typesService.deleteType(id);
        attributes.addFlashAttribute("message", "删除成功");
        return "redirect:/admin/types";
    }




}
