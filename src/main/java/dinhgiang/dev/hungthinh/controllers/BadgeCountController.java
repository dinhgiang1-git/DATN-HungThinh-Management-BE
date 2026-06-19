package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.entities.enums.FeedbackStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.InvoiceStatus;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.repositories.FeedbackRepository;
import dinhgiang.dev.hungthinh.repositories.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/badge-counts")
@RequiredArgsConstructor
public class BadgeCountController extends BaseController {

    private final FeedbackRepository feedbackRepository;
    private final InvoiceRepository invoiceRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getCounts() {
        long unansweredFeedbacks = feedbackRepository.countUnansweredFeedbacks();
        long unpaidInvoices = invoiceRepository.countByInvoiceStatus(InvoiceStatus.UNPAID);
        long overdueInvoices = invoiceRepository.countByInvoiceStatus(InvoiceStatus.OVERDUE);

        Map<String, Long> counts = Map.of(
                "pendingFeedbacks", unansweredFeedbacks,
                "unpaidInvoices", unpaidInvoices + overdueInvoices
        );

        return success(counts, "OK");
    }
}
