package com.example.stduents_management.payment.controller;

import com.example.stduents_management.payment.dto.PaymentRequest;
import com.example.stduents_management.payment.dto.PaymentResponse;
import com.example.stduents_management.payment.entity.PaymentMethod;
import com.example.stduents_management.payment.entity.PaymentStatus;
import com.example.stduents_management.payment.service.PaymentService;
import com.example.stduents_management.studenttuition.entity.StudentTuition;
import com.example.stduents_management.studenttuition.dto.StudentTuitionResponse;
import com.example.stduents_management.studenttuition.repository.StudentTuitionRepository;
import com.example.stduents_management.studenttuition.service.StudentTuitionService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
public class PaymentDashboardController {

    private final PaymentService paymentService;
    private final StudentTuitionService studentTuitionService;
    private final StudentTuitionRepository studentTuitionRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        Page<PaymentResponse> items = paymentService.search(keyword, status, page, size);
        long completedCount = items.getContent().stream()
                .filter(i -> i.status() == PaymentStatus.COMPLETED).count();
        long pendingCount = items.getContent().stream()
                .filter(i -> i.status() == PaymentStatus.PENDING).count();
        long cancelledCount = items.getContent().stream()
                .filter(i -> i.status() == PaymentStatus.CANCELLED).count();

        model.addAttribute("items", items);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("statusFilter", status == null ? "" : status);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("statuses", PaymentStatus.values());
        return "payments/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        PaymentRequest req = new PaymentRequest();
        req.setPaymentDate(LocalDateTime.now());
        model.addAttribute("mode", "create");
        model.addAttribute("paymentRequest", req);
        loadSelectData(model);
        return "payments/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("paymentRequest") PaymentRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {

        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            loadSelectData(model);
            return "payments/form";
        }
        try {
            paymentService.create(req);
            redirect.addFlashAttribute("success", "Thêm giao dịch thanh toán thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "create");
            loadSelectData(model);
            model.addAttribute("globalError", e.getMessage());
            return "payments/form";
        }
        return "redirect:/admin/payments";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PaymentResponse r = paymentService.getById(id);
        PaymentRequest req = new PaymentRequest();
        req.setStudentTuitionId(r.studentTuitionId());
        req.setAmount(r.amount());
        req.setPaymentMethod(r.paymentMethod());
        req.setTransactionCode(r.transactionCode());
        req.setPaymentDate(r.paymentDate());
        req.setStatus(r.status());

        model.addAttribute("mode", "edit");
        model.addAttribute("paymentId", id);
        model.addAttribute("paymentRequest", req);
        loadSelectData(model);
        model.addAttribute("currentItem", r);
        return "payments/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("paymentRequest") PaymentRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {

        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("paymentId", id);
            loadSelectData(model);
            return "payments/form";
        }
        try {
            paymentService.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật giao dịch thanh toán thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("paymentId", id);
            loadSelectData(model);
            model.addAttribute("globalError", e.getMessage());
            return "payments/form";
        }
        return "redirect:/admin/payments";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            paymentService.delete(id);
            redirect.addFlashAttribute("success", "Xóa giao dịch thanh toán thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/payments";
    }

    @GetMapping("/print")
    public String print(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            Model model) {
        List<PaymentResponse> all = paymentService.getAll();
        long completedCount = all.stream().filter(i -> i.status() == PaymentStatus.COMPLETED).count();
        long pendingCount = all.stream().filter(i -> i.status() == PaymentStatus.PENDING).count();
        long cancelledCount = all.stream().filter(i -> i.status() == PaymentStatus.CANCELLED).count();
        model.addAttribute("items", all);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("statusFilter", status == null ? "" : status);
        return "payments/print";
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        paymentService.exportExcel(response);
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect) {
        try {
            int count = paymentService.importExcel(file);
            redirect.addFlashAttribute("success",
                    "Import thành công " + count + " giao dịch thanh toán");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import thất bại: " + e.getMessage());
        }
        return "redirect:/admin/payments";
    }

    // ─── API: TÓM TẮT HỌC PHÍ HỌC KỲ (liên quan tuition_fees) ─────────────────

    @GetMapping("/api/tuition-summary")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> tuitionSummary(@RequestParam UUID studentTuitionId) {
        Map<String, Object> result = new HashMap<>();
        try {
            StudentTuitionResponse st = studentTuitionService.getById(studentTuitionId);
            StudentTuition tuition = studentTuitionRepository.findById(studentTuitionId).orElse(null);
            Object feePerCredit = null;
            if (tuition != null) {
                feePerCredit = studentTuitionService.findActiveFeePerCreditForStudent(tuition.getStudent());
            }
            result.put("totalCredits", st.totalCredits());
            result.put("totalAmount", st.totalAmount());
            result.put("amountPaid", st.amountPaid());
            result.put("remainingAmount", st.remainingAmount());
            result.put("feePerCredit", feePerCredit);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("totalCredits", 0);
            result.put("totalAmount", 0);
            result.put("amountPaid", 0);
            result.put("remainingAmount", 0);
            result.put("feePerCredit", null);
            return ResponseEntity.badRequest().body(result);
        }
    }

    private void loadSelectData(Model model) {
        List<StudentTuitionResponse> tuitions = studentTuitionService.getAll();
        model.addAttribute("studentTuitions", tuitions);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("statuses", PaymentStatus.values());
    }
}
