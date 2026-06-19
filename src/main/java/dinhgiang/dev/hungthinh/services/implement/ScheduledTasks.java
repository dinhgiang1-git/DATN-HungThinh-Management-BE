package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.models.entities.bases.Contract;
import dinhgiang.dev.hungthinh.models.entities.bases.Invoice;
import dinhgiang.dev.hungthinh.models.entities.enums.ApartmentStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.ContractStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.InvoiceStatus;
import dinhgiang.dev.hungthinh.repositories.ApartmentRepository;
import dinhgiang.dev.hungthinh.repositories.ContractRepository;
import dinhgiang.dev.hungthinh.repositories.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Lớp dịch vụ chạy các tác vụ lên lịch tự động (Scheduled Tasks).
 * Ví dụ: Tự động ghi chỉ số điện nước cuối tháng, nhắc nợ, v.v.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final ContractRepository contractRepository;
    private final InvoiceRepository invoiceRepository;
    private final ApartmentRepository apartmentRepository;
    private final SystemNotificationService systemNotificationService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FMT = NumberFormat.getInstance(new Locale("vi", "VN"));

    /**
     * Chạy mỗi ngày lúc 00:05 AM
     * 1. Cập nhật hợp đồng hết hạn -> EXPIRED
     * 2. Cập nhật hóa đơn quá hạn -> OVERDUE + gửi thông báo cho cư dân
     */
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void dailyStatusUpdate() {
        LocalDate today = LocalDate.now();

        // 1. Hợp đồng hết hạn
        List<Contract> expiredContracts = contractRepository
                .findByContractStatusAndEndDateBefore(ContractStatus.ACTIVE, today);
        if (!expiredContracts.isEmpty()) {
            expiredContracts.forEach(contract -> contract.setContractStatus(ContractStatus.EXPIRED));
            contractRepository.saveAll(expiredContracts);
            expiredContracts.stream()
                    .map(Contract::getApartment)
                    .filter(Objects::nonNull)
                    .distinct()
                    .forEach(apartment -> {
                        var currentContracts = contractRepository.findActiveContractsForApartmentAtDate(
                                apartment.getId(),
                                ContractStatus.ACTIVE,
                                today
                        );
                        if (!currentContracts.isEmpty()) {
                            Contract currentContract = currentContracts.get(0);
                            apartment.setOwnerId(currentContract.getResident() != null ? currentContract.getResident().getId() : null);
                            apartment.setApartmentStatus(ApartmentStatus.OCCUPIED);
                        } else {
                            apartment.setOwnerId(null);
                            if (apartment.getApartmentStatus() != ApartmentStatus.UNDER_MAINTENANCE) {
                                apartment.setApartmentStatus(ApartmentStatus.VACANT);
                            }
                        }
                        apartmentRepository.save(apartment);
                    });
            log.info("Đã cập nhật {} hợp đồng sang trạng thái EXPIRED", expiredContracts.size());
        }

        // 2. Hóa đơn quá hạn + gửi thông báo cho cư dân
        List<Invoice> overdueInvoices = invoiceRepository
                .findByInvoiceStatusAndDueDateBefore(InvoiceStatus.UNPAID, today);
        if (!overdueInvoices.isEmpty()) {
            overdueInvoices.forEach(invoice -> invoice.setInvoiceStatus(InvoiceStatus.OVERDUE));
            invoiceRepository.saveAll(overdueInvoices);
            log.info("Đã cập nhật {} hóa đơn sang trạng thái OVERDUE", overdueInvoices.size());

            // Gửi thông báo cho cư dân từng căn hộ có hoá đơn quá hạn
            for (Invoice invoice : overdueInvoices) {
                try {
                    if (invoice.getApartment() != null) {
                        String amountStr = invoice.getTotalAmount() != null
                                ? CURRENCY_FMT.format(invoice.getTotalAmount()) + " VNĐ"
                                : "chưa xác định";
                        String dueDateStr = invoice.getDueDate() != null
                                ? invoice.getDueDate().format(DATE_FMT)
                                : "không rõ";
                        String period = invoice.getBillingPeriod() != null
                                ? invoice.getBillingPeriod()
                                : "";

                        String title = "⚠️ Hóa đơn quá hạn thanh toán";
                        String content = String.format(
                                "Hóa đơn %s%s với số tiền %s đã quá hạn thanh toán (hạn: %s). " +
                                "Vui lòng thanh toán sớm để tránh phát sinh phí trễ hạn.",
                                invoice.getInvoiceNumber(),
                                period.isEmpty() ? "" : " (kỳ " + period + ")",
                                amountStr,
                                dueDateStr
                        );

                        systemNotificationService.notifyApartment(
                                title, content, invoice.getApartment().getId()
                        );
                    }
                } catch (Exception e) {
                    log.error("Lỗi gửi thông báo quá hạn cho hóa đơn {}: {}",
                            invoice.getInvoiceNumber(), e.getMessage());
                }
            }
        }
    }
}
