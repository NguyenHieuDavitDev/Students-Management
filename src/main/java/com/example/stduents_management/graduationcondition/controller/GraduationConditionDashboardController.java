package com.example.stduents_management.graduationcondition.controller;

import com.example.stduents_management.graduationcondition.dto.GraduationConditionRequest;
import com.example.stduents_management.graduationcondition.dto.GraduationConditionResponse;
import com.example.stduents_management.graduationcondition.service.GraduationConditionService;
import com.example.stduents_management.trainingprogram.dto.TrainingProgramResponse;
import com.example.stduents_management.trainingprogram.service.TrainingProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/graduation-conditions")
@RequiredArgsConstructor
public class GraduationConditionDashboardController {

    private final GraduationConditionService graduationConditionService;
    private final TrainingProgramService trainingProgramService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        Page<GraduationConditionResponse> items = graduationConditionService.search(keyword, page, size);
        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "graduation-conditions/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("graduationConditionRequest", new GraduationConditionRequest());
        model.addAttribute("programs", getActiveProgramsWithoutCondition());
        return "graduation-conditions/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("graduationConditionRequest") GraduationConditionRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("programs", getActiveProgramsWithoutCondition());
            return "graduation-conditions/form";
        }
        try {
            graduationConditionService.create(req);
            redirect.addFlashAttribute("success", "Thêm điều kiện xét tốt nghiệp thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "create");
            model.addAttribute("programs", getActiveProgramsWithoutCondition());
            model.addAttribute("globalError", e.getMessage());
            return "graduation-conditions/form";
        }
        return "redirect:/admin/graduation-conditions";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        GraduationConditionResponse r = graduationConditionService.getById(id);
        GraduationConditionRequest req = new GraduationConditionRequest();
        req.setProgramId(r.programId());
        req.setMinCredits(r.minCredits());
        req.setMinGpa(r.minGpa());
        req.setRequiredCertificate(r.requiredCertificate());
        req.setRequiredCourses(r.requiredCourses());

        model.addAttribute("mode", "edit");
        model.addAttribute("conditionId", id);
        model.addAttribute("graduationConditionRequest", req);
        model.addAttribute("programs", getAllActivePrograms());
        model.addAttribute("currentCondition", r);
        return "graduation-conditions/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("graduationConditionRequest") GraduationConditionRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("conditionId", id);
            model.addAttribute("programs", getAllActivePrograms());
            return "graduation-conditions/form";
        }
        try {
            graduationConditionService.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật điều kiện xét tốt nghiệp thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("conditionId", id);
            model.addAttribute("programs", getAllActivePrograms());
            model.addAttribute("globalError", e.getMessage());
            return "graduation-conditions/form";
        }
        return "redirect:/admin/graduation-conditions";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            graduationConditionService.delete(id);
            redirect.addFlashAttribute("success", "Xóa điều kiện xét tốt nghiệp thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/graduation-conditions";
    }

    private List<TrainingProgramResponse> getAllActivePrograms() {
        return trainingProgramService.search(null, 0, Integer.MAX_VALUE)
                .getContent()
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .toList();
    }

    /** Chương trình đang hoạt động và chưa có điều kiện xét tốt nghiệp (dùng cho form thêm mới). */
    private List<TrainingProgramResponse> getActiveProgramsWithoutCondition() {
        List<TrainingProgramResponse> active = getAllActivePrograms();
        return active.stream()
                .filter(p -> graduationConditionService.getByProgramId(p.getProgramId()) == null)
                .toList();
    }
}
