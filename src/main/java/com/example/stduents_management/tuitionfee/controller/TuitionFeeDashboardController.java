package com.example.stduents_management.tuitionfee.controller;

import com.example.stduents_management.trainingprogram.dto.TrainingProgramResponse;
import com.example.stduents_management.trainingprogram.service.TrainingProgramService;
import com.example.stduents_management.tuitionfee.dto.TuitionFeeRequest;
import com.example.stduents_management.tuitionfee.dto.TuitionFeeResponse;
import com.example.stduents_management.tuitionfee.entity.TuitionFeeStatus;
import com.example.stduents_management.tuitionfee.service.TuitionFeeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/tuition-fees")
@RequiredArgsConstructor
public class TuitionFeeDashboardController {

    private final TuitionFeeService tuitionFeeService;
    private final TrainingProgramService trainingProgramService;

    // ─── INDEX ────────────────────────────────────────────────────────────────

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        Page<TuitionFeeResponse> items = tuitionFeeService.search(keyword, status, page, size);
        long activeCount   = items.getContent().stream()
                .filter(i -> i.status() == TuitionFeeStatus.ACTIVE).count();
        long inactiveCount = items.getContent().stream()
                .filter(i -> i.status() == TuitionFeeStatus.INACTIVE).count();
        model.addAttribute("items", items);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("statusFilter", status == null ? "" : status);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("statuses", TuitionFeeStatus.values());
        return "tuition-fees/index";
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("tuitionFeeRequest", new TuitionFeeRequest());
        model.addAttribute("statuses", TuitionFeeStatus.values());
        model.addAttribute("programs", getAllActivePrograms());
        return "tuition-fees/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("tuitionFeeRequest") TuitionFeeRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {

        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("statuses", TuitionFeeStatus.values());
            model.addAttribute("programs", getAllActivePrograms());
            return "tuition-fees/form";
        }
        try {
            tuitionFeeService.create(req);
            redirect.addFlashAttribute("success", "Thêm mức học phí thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "create");
            model.addAttribute("statuses", TuitionFeeStatus.values());
            model.addAttribute("programs", getAllActivePrograms());
            model.addAttribute("globalError", e.getMessage());
            return "tuition-fees/form";
        }
        return "redirect:/admin/tuition-fees";
    }

    // ─── EDIT ─────────────────────────────────────────────────────────────────

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        TuitionFeeResponse r = tuitionFeeService.getById(id);

        TuitionFeeRequest req = new TuitionFeeRequest();
        req.setProgramId(r.programId());
        req.setFeePerCredit(r.feePerCredit());
        req.setEffectiveDate(r.effectiveDate());
        req.setStatus(r.status());
        req.setNote(r.note());

        model.addAttribute("mode", "edit");
        model.addAttribute("feeId", id);
        model.addAttribute("tuitionFeeRequest", req);
        model.addAttribute("statuses", TuitionFeeStatus.values());
        model.addAttribute("programs", getAllActivePrograms());
        model.addAttribute("currentFee", r);
        return "tuition-fees/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("tuitionFeeRequest") TuitionFeeRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {

        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("feeId", id);
            model.addAttribute("statuses", TuitionFeeStatus.values());
            model.addAttribute("programs", getAllActivePrograms());
            return "tuition-fees/form";
        }
        try {
            tuitionFeeService.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật mức học phí thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("feeId", id);
            model.addAttribute("statuses", TuitionFeeStatus.values());
            model.addAttribute("programs", getAllActivePrograms());
            model.addAttribute("globalError", e.getMessage());
            return "tuition-fees/form";
        }
        return "redirect:/admin/tuition-fees";
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        try {
            tuitionFeeService.delete(id);
            redirect.addFlashAttribute("success", "Xóa mức học phí thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tuition-fees";
    }

    // ─── TOGGLE STATUS ────────────────────────────────────────────────────────

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable UUID id, RedirectAttributes redirect) {
        try {
            tuitionFeeService.toggleStatus(id);
            redirect.addFlashAttribute("success", "Cập nhật trạng thái thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tuition-fees";
    }

    // ─── PRINT ────────────────────────────────────────────────────────────────

    @GetMapping("/print")
    public String print(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            Model model) {
        List<TuitionFeeResponse> all = tuitionFeeService.getAll();
        long activeCount   = all.stream().filter(i -> i.status() == TuitionFeeStatus.ACTIVE).count();
        long inactiveCount = all.stream().filter(i -> i.status() == TuitionFeeStatus.INACTIVE).count();
        model.addAttribute("items", all);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("inactiveCount", inactiveCount);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("statusFilter", status == null ? "" : status);
        return "tuition-fees/print";
    }

    // ─── EXPORT EXCEL ─────────────────────────────────────────────────────────

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        tuitionFeeService.exportExcel(response);
    }

    // ─── IMPORT EXCEL ─────────────────────────────────────────────────────────

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect) {
        try {
            int count = tuitionFeeService.importExcel(file);
            redirect.addFlashAttribute("success",
                    "Import thành công " + count + " mức học phí");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import thất bại: " + e.getMessage());
        }
        return "redirect:/admin/tuition-fees";
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private List<TrainingProgramResponse> getAllActivePrograms() {
        return trainingProgramService.search(null, 0, Integer.MAX_VALUE)
                .getContent()
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .toList();
    }
}
