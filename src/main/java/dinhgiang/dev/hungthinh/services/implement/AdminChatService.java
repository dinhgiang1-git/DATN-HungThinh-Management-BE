package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.dtos.adminchat.AdminChatRequest;
import dinhgiang.dev.hungthinh.models.dtos.adminchat.AdminChatResponse;
import dinhgiang.dev.hungthinh.models.entities.bases.Apartment;
import dinhgiang.dev.hungthinh.models.entities.bases.Contract;
import dinhgiang.dev.hungthinh.models.entities.bases.Device;
import dinhgiang.dev.hungthinh.models.entities.bases.Feedback;
import dinhgiang.dev.hungthinh.models.entities.bases.Invoice;
import dinhgiang.dev.hungthinh.models.entities.bases.Maintenance;
import dinhgiang.dev.hungthinh.models.entities.bases.Payment;
import dinhgiang.dev.hungthinh.models.entities.bases.Resident;
import dinhgiang.dev.hungthinh.models.entities.bases.Vehicle;
import dinhgiang.dev.hungthinh.models.entities.enums.ApartmentStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.ContractStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.ContractType;
import dinhgiang.dev.hungthinh.models.entities.enums.DeviceStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.DeviceType;
import dinhgiang.dev.hungthinh.models.entities.enums.FeedbackStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.FeedbackType;
import dinhgiang.dev.hungthinh.models.entities.enums.InvoiceStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.MaintenanceStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.PaymentMethod;
import dinhgiang.dev.hungthinh.models.entities.enums.PaymentStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.RelationshipType;
import dinhgiang.dev.hungthinh.models.entities.enums.UserRole;
import dinhgiang.dev.hungthinh.models.entities.enums.VehicleType;
import dinhgiang.dev.hungthinh.repositories.ApartmentRepository;
import dinhgiang.dev.hungthinh.repositories.ContractRepository;
import dinhgiang.dev.hungthinh.repositories.DeviceRepository;
import dinhgiang.dev.hungthinh.repositories.FeedbackRepository;
import dinhgiang.dev.hungthinh.repositories.InvoiceRepository;
import dinhgiang.dev.hungthinh.repositories.MaintenanceRepository;
import dinhgiang.dev.hungthinh.repositories.PaymentRepository;
import dinhgiang.dev.hungthinh.repositories.ResidentRepository;
import dinhgiang.dev.hungthinh.repositories.UserRepository;
import dinhgiang.dev.hungthinh.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminChatService {
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";
    private static final int MAX_HISTORY_MESSAGES = 8;
    private static final int MAX_TOOL_TURNS = 3;

    private final ApartmentRepository apartmentRepository;
    private final ResidentRepository residentRepository;
    private final InvoiceRepository invoiceRepository;
    private final FeedbackRepository feedbackRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final DeviceRepository deviceRepository;
    private final ContractRepository contractRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    public AdminChatResponse ask(String message) {
        return ask(message, List.of());
    }

    public AdminChatResponse ask(String message, List<AdminChatRequest.ChatMessage> history) {
        if (!hasText(apiKey)) {
            throw new UserMessageException("Chưa cấu hình GEMINI_API_KEY cho backend");
        }
        if (!hasText(message)) {
            throw new UserMessageException("Nội dung câu hỏi không được để trống");
        }

        List<Map<String, Object>> contents = buildContents(message, history);
        Map<String, Object> response = callGemini(contents, true);

        for (int turn = 0; turn < MAX_TOOL_TURNS; turn++) {
            List<ToolCall> toolCalls = extractToolCalls(response);
            if (toolCalls.isEmpty()) {
                String answer = extractText(response);
                if (!hasText(answer)) {
                    throw new UserMessageException("Gemini chưa trả về nội dung phản hồi");
                }
                return AdminChatResponse.builder().answer(answer.trim()).build();
            }

            List<Map<String, Object>> modelParts = extractParts(response);
            if (modelParts.isEmpty()) {
                throw new UserMessageException("Gemini yêu cầu gọi công cụ nhưng thiếu dữ liệu functionCall");
            }

            contents.add(content("model", modelParts));
            contents.add(content("function", toolCalls.stream()
                    .map(call -> {
                        Map<String, Object> functionResponse = new LinkedHashMap<>();
                        functionResponse.put("name", call.name());
                        if (hasText(call.id())) {
                            functionResponse.put("id", call.id());
                        }
                        functionResponse.put("response", Map.of("result", executeTool(call)));
                        Map<String, Object> part = new LinkedHashMap<>();
                        part.put("functionResponse", functionResponse);
                        return part;
                    })
                    .toList()));

            response = callGemini(contents, true);
        }

        String answer = extractText(response);
        if (!hasText(answer)) {
            throw new UserMessageException("AI cần gọi quá nhiều công cụ và chưa tổng hợp được câu trả lời");
        }
        return AdminChatResponse.builder().answer(answer.trim()).build();
    }

    private Map<String, Object> callGemini(List<Map<String, Object>> contents, boolean includeTools) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("systemInstruction", Map.of("parts", List.of(Map.of("text", buildSystemInstruction()))));
        payload.put("contents", contents);
        payload.put("generationConfig", Map.of(
                "temperature", 0.25,
                "maxOutputTokens", 1400
        ));
        if (includeTools) {
            payload.put("tools", List.of(Map.of("functionDeclarations", toolDeclarations())));
        }

        try {
            Map<String, Object> response = RestClient.builder()
                    .build()
                    .post()
                    .uri(GEMINI_URL, model)
                    .header("x-goog-api-key", apiKey)
                    .body(payload)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return response != null ? response : Map.of();
        } catch (Exception e) {
            String detail = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            throw new UserMessageException("Lỗi khi gọi Gemini API: " + detail);
        }
    }

    private String buildSystemInstruction() {
        return """
                Bạn là trợ lý AI vận hành cho ban quản trị chung cư Hưng Thịnh.
                Ngày hệ thống hiện tại: %s.
                Trả lời bằng tiếng Việt, thực tế, ngắn gọn nhưng đủ số liệu. Tuyệt đối không dùng Markdown: không dùng dấu **, không dùng bullet bằng dấu * hoặc -, không tạo bảng Markdown.
                Khi câu hỏi liên quan đến số liệu hệ thống, hóa đơn, căn hộ, cư dân, hợp đồng, phương tiện, thiết bị, phản ánh hoặc bảo trì, bắt buộc gọi công cụ phù hợp trước khi trả lời.
                Khi admin hỏi chi tiết một căn hộ, hãy gọi getApartmentDeepDive. Nếu chỉ nói "căn đó" hoặc "phòng đó", hãy dùng lịch sử hội thoại để suy ra căn hộ đã nhắc gần nhất.
                Nếu không xác định được căn hộ, hãy hỏi lại số căn hộ hoặc block/khu cần xem. Không tự bịa số liệu ngoài dữ liệu công cụ trả về.
                Khi có rủi ro vận hành như hóa đơn quá hạn, phản ánh chưa xử lý, thiết bị hỏng hoặc bảo trì đang mở, hãy nêu nhận xét và gợi ý hành động cho admin.
                """.formatted(LocalDate.now());
    }

    private List<Map<String, Object>> buildContents(String message, List<AdminChatRequest.ChatMessage> history) {
        List<Map<String, Object>> contents = new ArrayList<>();
        List<AdminChatRequest.ChatMessage> cleanHistory = Optional.ofNullable(history)
                .orElse(List.of())
                .stream()
                .filter(item -> item != null && hasText(item.getContent()))
                .toList();
        int start = Math.max(0, cleanHistory.size() - MAX_HISTORY_MESSAGES);
        List<AdminChatRequest.ChatMessage> window = new ArrayList<>(cleanHistory.subList(start, cleanHistory.size()));
        while (!window.isEmpty() && "assistant".equalsIgnoreCase(window.get(0).getRole())) {
            window.remove(0);
        }
        for (AdminChatRequest.ChatMessage item : window) {
            String role = "assistant".equalsIgnoreCase(item.getRole()) ? "model" : "user";
            contents.add(content(role, List.of(textPart(item.getContent()))));
        }
        contents.add(content("user", List.of(textPart(message))));
        return contents;
    }

    private List<Map<String, Object>> toolDeclarations() {
        return List.of(
                functionDeclaration(
                        "getSystemOverview",
                        "Lấy tổng quan hiện tại của toàn bộ hệ thống: căn hộ, cư dân, hóa đơn, phản ánh, bảo trì, thiết bị, hợp đồng và phương tiện.",
                        Map.of(),
                        List.of()
                ),
                functionDeclaration(
                        "getInvoiceStats",
                        "Thống kê hóa đơn theo tháng/năm. Nếu người dùng hỏi tháng này, dùng tháng/năm của ngày hệ thống.",
                        Map.of(
                                "month", property("integer", "Tháng cần thống kê, từ 1 đến 12. Bỏ trống nếu là tháng hiện tại."),
                                "year", property("integer", "Năm cần thống kê. Bỏ trống nếu là năm hiện tại.")
                        ),
                        List.of()
                ),
                functionDeclaration(
                        "getRecentIssues",
                        "Lấy các phản ánh và công việc bảo trì đang mở, cần ưu tiên xử lý.",
                        Map.of(
                                "limit", property("integer", "Số bản ghi tối đa cho mỗi nhóm, mặc định 8, tối đa 20.")
                        ),
                        List.of()
                ),
                functionDeclaration(
                        "getApartmentDeepDive",
                        "Phân tích sâu một căn hộ: thông tin căn hộ, chủ hộ/cư dân, hóa đơn gần đây, công nợ, thanh toán, hợp đồng, phương tiện, thiết bị, phản ánh và bảo trì liên quan.",
                        Map.of(
                                "apartmentId", property("integer", "ID căn hộ nếu người dùng cung cấp."),
                                "apartmentNumber", property("string", "Số căn hộ/phòng, ví dụ A101, B205."),
                                "block", property("string", "Block/tòa nhà nếu cần phân biệt căn hộ trùng số."),
                                "complexName", property("string", "Tên khu/chung cư nếu cần phân biệt."),
                                "invoiceLimit", property("integer", "Số hóa đơn gần đây cần xem, mặc định 12, tối đa 24.")
                        ),
                        List.of()
                )
        );
    }

    private Map<String, Object> functionDeclaration(
            String name,
            String description,
            Map<String, Object> properties,
            List<String> required
    ) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", properties);
        if (required != null && !required.isEmpty()) {
            parameters.put("required", required);
        }

        Map<String, Object> declaration = new LinkedHashMap<>();
        declaration.put("name", name);
        declaration.put("description", description);
        declaration.put("parameters", parameters);
        return declaration;
    }

    private Map<String, Object> property(String type, String description) {
        return Map.of(
                "type", type,
                "description", description
        );
    }

    private Object executeTool(ToolCall call) {
        try {
            return switch (call.name()) {
                case "getSystemOverview" -> getSystemOverview();
                case "getInvoiceStats" -> getInvoiceStats(call.args());
                case "getRecentIssues" -> getRecentIssues(call.args());
                case "getApartmentDeepDive" -> getApartmentDeepDive(call.args());
                default -> Map.of("error", "Công cụ không được hỗ trợ: " + call.name());
            };
        } catch (Exception e) {
            String detail = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return Map.of("error", detail);
        }
    }

    private Map<String, Object> getSystemOverview() {
        var apartments = apartmentRepository.findAll();
        var residents = residentRepository.findAll();
        var invoices = invoiceRepository.findAll();
        var feedbacks = feedbackRepository.findAll();
        var maintenances = maintenanceRepository.findAll();
        var devices = deviceRepository.findAll();
        var contracts = contractRepository.findAll();
        var vehicles = vehicleRepository.findAll();
        var users = userRepository.findAll();

        BigDecimal totalReceivable = invoices.stream()
                .filter(invoice -> invoice.getInvoiceStatus() == InvoiceStatus.UNPAID || invoice.getInvoiceStatus() == InvoiceStatus.OVERDUE)
                .map(Invoice::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidRevenue = invoices.stream()
                .filter(invoice -> invoice.getInvoiceStatus() == InvoiceStatus.PAID)
                .map(Invoice::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("systemDate", LocalDate.now().toString());
        result.put("apartments", Map.of(
                "total", apartments.size(),
                "byStatus", countBy(apartments, apartment -> displayEnum(apartment.getApartmentStatus()))
        ));
        result.put("residents", Map.of(
                "total", residents.size(),
                "byRelationship", countBy(residents, resident -> displayEnum(resident.getRelationship()))
        ));
        result.put("users", Map.of(
                "total", users.size(),
                "byRole", countBy(users, user -> displayEnum(user.getUserRole()))
        ));
        result.put("invoices", Map.of(
                "total", invoices.size(),
                "byStatus", countBy(invoices, invoice -> displayEnum(invoice.getInvoiceStatus())),
                "paidRevenue", paidRevenue,
                "paidRevenueFormatted", formatMoney(paidRevenue),
                "receivable", totalReceivable,
                "receivableFormatted", formatMoney(totalReceivable)
        ));
        result.put("feedbacks", Map.of(
                "total", feedbacks.size(),
                "byStatus", countBy(feedbacks, feedback -> displayEnum(feedback.getFeedbackStatus()))
        ));
        result.put("maintenances", Map.of(
                "total", maintenances.size(),
                "byStatus", countBy(maintenances, maintenance -> displayEnum(maintenance.getMaintenanceStatus()))
        ));
        result.put("devices", Map.of(
                "total", devices.size(),
                "byType", countBy(devices, device -> displayEnum(device.getDeviceType())),
                "byStatus", countBy(devices, device -> displayEnum(device.getDeviceStatus()))
        ));
        result.put("contracts", Map.of(
                "total", contracts.size(),
                "byStatus", countBy(contracts, contract -> displayEnum(contract.getContractStatus()))
        ));
        result.put("vehicles", Map.of("total", vehicles.size()));
        result.put("recentFeedbacks", feedbackRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(this::feedbackBasicSummary)
                .toList());
        result.put("recentOverdueInvoices", invoiceRepository
                .findByInvoiceStatus(InvoiceStatus.OVERDUE, PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "dueDate")))
                .stream()
                .map(invoice -> invoiceSummary(invoice, null, false))
                .toList());
        return result;
    }

    private Map<String, Object> getInvoiceStats(Map<String, Object> args) {
        LocalDate today = LocalDate.now();
        int month = getIntArg(args, "month", today.getMonthValue());
        int year = getIntArg(args, "year", today.getYear());
        if (month < 1 || month > 12) {
            return Map.of("error", "Tháng phải nằm trong khoảng 1 đến 12");
        }

        YearMonth period = YearMonth.of(year, month);
        String billingPeriod = "%02d/%d".formatted(month, year);
        List<Invoice> invoices = invoiceRepository.findForBillingMonth(
                billingPeriod,
                period.atDay(1),
                period.atEndOfMonth()
        );

        BigDecimal paidAmount = sumInvoiceAmount(invoices, invoice -> invoice.getInvoiceStatus() == InvoiceStatus.PAID);
        BigDecimal unpaidAmount = sumInvoiceAmount(invoices, invoice -> invoice.getInvoiceStatus() == InvoiceStatus.UNPAID);
        BigDecimal overdueAmount = sumInvoiceAmount(invoices, invoice -> invoice.getInvoiceStatus() == InvoiceStatus.OVERDUE);
        BigDecimal totalAmount = invoices.stream()
                .map(Invoice::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("billingPeriod", billingPeriod);
        result.put("invoiceCount", invoices.size());
        result.put("statusCounts", countBy(invoices, invoice -> displayEnum(invoice.getInvoiceStatus())));
        result.put("totalAmount", totalAmount);
        result.put("totalAmountFormatted", formatMoney(totalAmount));
        result.put("paidAmount", paidAmount);
        result.put("paidAmountFormatted", formatMoney(paidAmount));
        result.put("unpaidAmount", unpaidAmount);
        result.put("unpaidAmountFormatted", formatMoney(unpaidAmount));
        result.put("overdueAmount", overdueAmount);
        result.put("overdueAmountFormatted", formatMoney(overdueAmount));
        result.put("outstandingAmount", unpaidAmount.add(overdueAmount));
        result.put("outstandingAmountFormatted", formatMoney(unpaidAmount.add(overdueAmount)));
        result.put("outstandingInvoices", invoices.stream()
                .filter(invoice -> invoice.getInvoiceStatus() == InvoiceStatus.UNPAID || invoice.getInvoiceStatus() == InvoiceStatus.OVERDUE)
                .limit(10)
                .map(invoice -> invoiceSummary(invoice, null, true))
                .toList());
        return result;
    }

    private Map<String, Object> getRecentIssues(Map<String, Object> args) {
        int limit = clamp(getIntArg(args, "limit", 8), 1, 20);
        List<Feedback> feedbacks = feedbackRepository.findOpenIssuesForAdminChat(
                List.of(FeedbackStatus.PENDING, FeedbackStatus.IN_PROGRESS),
                PageRequest.of(0, limit)
        );
        List<Maintenance> maintenances = maintenanceRepository.findOpenIssuesForAdminChat(
                List.of(MaintenanceStatus.SCHEDULED, MaintenanceStatus.IN_PROGRESS),
                PageRequest.of(0, limit)
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("systemDate", LocalDate.now().toString());
        result.put("openFeedbackCountInResult", feedbacks.size());
        result.put("openMaintenanceCountInResult", maintenances.size());
        result.put("feedbacks", feedbacks.stream().map(this::feedbackSummary).toList());
        result.put("maintenances", maintenances.stream().map(this::maintenanceSummary).toList());
        return result;
    }

    private Map<String, Object> getApartmentDeepDive(Map<String, Object> args) {
        int invoiceLimit = clamp(getIntArg(args, "invoiceLimit", 12), 1, 24);
        Apartment apartment = resolveApartment(args).orElse(null);
        if (apartment == null) {
            Map<String, Object> notFound = new LinkedHashMap<>();
            notFound.put("status", "not_found");
            notFound.put("message", "Không tìm thấy căn hộ theo thông tin được cung cấp");
            notFound.put("input", args);
            return notFound;
        }

        Long apartmentId = apartment.getId();
        List<Resident> residents = residentRepository.findByApartment_Id(apartmentId);
        List<Invoice> invoices = invoiceRepository.findByApartment_IdOrderByDueDateDescIdDesc(
                apartmentId,
                PageRequest.of(0, Math.max(invoiceLimit, 36))
        );
        List<Long> invoiceIds = invoices.stream().map(Invoice::getId).filter(Objects::nonNull).toList();
        List<Payment> payments = invoiceIds.isEmpty()
                ? List.of()
                : paymentRepository.findByInvoiceIdsForAdminChat(invoiceIds);
        Map<Long, BigDecimal> paidByInvoice = paidAmountByInvoice(payments);
        List<Contract> contracts = contractRepository.findByApartmentForAdminChat(apartmentId);
        List<Vehicle> vehicles = vehicleRepository.findByApartmentId(apartmentId);
        List<Device> devices = deviceRepository.findByApartmentId(apartmentId);
        List<Feedback> feedbacks = feedbackRepository.findByApartmentForAdminChat(
                apartmentId,
                PageRequest.of(0, 12)
        );
        List<Maintenance> maintenances = maintenanceRepository.findByApartmentForAdminChat(
                apartmentId,
                PageRequest.of(0, 12)
        );

        BigDecimal outstanding = invoices.stream()
                .filter(invoice -> invoice.getInvoiceStatus() == InvoiceStatus.UNPAID || invoice.getInvoiceStatus() == InvoiceStatus.OVERDUE)
                .map(Invoice::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal overdue = invoices.stream()
                .filter(invoice -> invoice.getInvoiceStatus() == InvoiceStatus.OVERDUE)
                .map(Invoice::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "found");
        result.put("apartment", apartmentSummary(apartment));
        result.put("owner", ownerSummary(apartment, residents));
        result.put("residents", residents.stream().map(this::residentSummary).toList());
        result.put("invoiceOverview", Map.of(
                "recentInvoicesChecked", invoices.size(),
                "statusCounts", countBy(invoices, invoice -> displayEnum(invoice.getInvoiceStatus())),
                "outstandingAmount", outstanding,
                "outstandingAmountFormatted", formatMoney(outstanding),
                "overdueAmount", overdue,
                "overdueAmountFormatted", formatMoney(overdue)
        ));
        result.put("recentInvoices", invoices.stream()
                .limit(invoiceLimit)
                .map(invoice -> invoiceSummary(invoice, paidByInvoice, false))
                .toList());
        result.put("recentPayments", payments.stream()
                .limit(8)
                .map(this::paymentSummary)
                .toList());
        result.put("contracts", contracts.stream().map(this::contractSummary).toList());
        result.put("vehicles", vehicles.stream().map(this::vehicleSummary).toList());
        result.put("devices", devices.stream().map(this::deviceSummary).toList());
        result.put("feedbacks", feedbacks.stream().map(this::feedbackSummary).toList());
        result.put("maintenances", maintenances.stream().map(this::maintenanceSummary).toList());
        return result;
    }

    private Optional<Apartment> resolveApartment(Map<String, Object> args) {
        Long apartmentId = getLongArg(args, "apartmentId");
        if (apartmentId != null) {
            return apartmentRepository.findById(apartmentId);
        }

        String apartmentNumber = normalize(getStringArg(args, "apartmentNumber"));
        if (!hasText(apartmentNumber)) {
            return Optional.empty();
        }

        String block = normalize(getStringArg(args, "block"));
        String complexName = normalize(getStringArg(args, "complexName"));
        List<Apartment> matches = apartmentRepository.findForAdminChat(apartmentNumber, block, complexName);
        if (matches.size() == 1) {
            return Optional.of(matches.get(0));
        }
        if (matches.size() > 1) {
            throw new UserMessageException("Có nhiều căn hộ trùng số " + apartmentNumber + ", cần cung cấp thêm block hoặc tên khu");
        }
        return Optional.empty();
    }

    private Map<Long, BigDecimal> paidAmountByInvoice(List<Payment> payments) {
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        for (Payment payment : payments) {
            if (payment.getPaymentStatus() != PaymentStatus.SUCCESS || payment.getInvoice() == null || payment.getInvoice().getId() == null) {
                continue;
            }
            result.merge(payment.getInvoice().getId(), safeAmount(payment.getAmount()), BigDecimal::add);
        }
        return result;
    }

    private BigDecimal sumInvoiceAmount(Collection<Invoice> invoices, Function<Invoice, Boolean> predicate) {
        return invoices.stream()
                .filter(invoice -> Boolean.TRUE.equals(predicate.apply(invoice)))
                .map(Invoice::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, Object> apartmentSummary(Apartment apartment) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", apartment.getId());
        map.put("complexName", apartment.getComplexName());
        map.put("block", apartment.getBlock());
        map.put("apartmentNumber", apartment.getApartmentNumber());
        map.put("floor", apartment.getFloor());
        map.put("area", apartment.getArea());
        map.put("ownerId", apartment.getOwnerId());
        map.put("status", displayEnum(apartment.getApartmentStatus()));
        return map;
    }

    private Map<String, Object> ownerSummary(Apartment apartment, List<Resident> residents) {
        Resident owner = residents.stream()
                .filter(resident -> Objects.equals(resident.getId(), apartment.getOwnerId()))
                .findFirst()
                .orElseGet(() -> residents.stream()
                        .filter(resident -> resident.getRelationship() == RelationshipType.OWNER)
                        .findFirst()
                        .orElse(null));
        if (owner == null) {
            return Map.of("status", "not_set");
        }
        return residentSummary(owner);
    }

    private Map<String, Object> residentSummary(Resident resident) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", resident.getId());
        map.put("fullName", resident.getFullName());
        map.put("username", resident.getUsername());
        map.put("phoneNumber", resident.getPhoneNumber());
        map.put("email", resident.getEmail());
        map.put("relationship", displayEnum(resident.getRelationship()));
        map.put("role", displayEnum(resident.getRole()));
        return map;
    }

    private Map<String, Object> invoiceSummary(Invoice invoice, Map<Long, BigDecimal> paidByInvoice, boolean includeApartment) {
        Map<String, Object> map = new LinkedHashMap<>();
        BigDecimal paidAmount = paidByInvoice != null ? paidByInvoice.getOrDefault(invoice.getId(), BigDecimal.ZERO) : null;
        BigDecimal remaining = paidAmount != null ? safeAmount(invoice.getTotalAmount()).subtract(paidAmount) : null;
        if (remaining != null && remaining.signum() < 0) {
            remaining = BigDecimal.ZERO;
        }

        map.put("id", invoice.getId());
        map.put("invoiceNumber", invoice.getInvoiceNumber());
        map.put("billingPeriod", invoice.getBillingPeriod());
        map.put("dueDate", formatDate(invoice.getDueDate()));
        map.put("status", displayEnum(invoice.getInvoiceStatus()));
        map.put("totalAmount", invoice.getTotalAmount());
        map.put("totalAmountFormatted", formatMoney(invoice.getTotalAmount()));
        if (includeApartment && invoice.getApartment() != null) {
            map.put("apartment", apartmentSummary(invoice.getApartment()));
        }
        map.put("fees", Map.of(
                "electricFee", safeAmount(invoice.getElectricFee()),
                "waterFee", safeAmount(invoice.getWaterFee()),
                "managementFee", safeAmount(invoice.getManagementFee()),
                "parkingFee", safeAmount(invoice.getParkingFee()),
                "otherFee", safeAmount(invoice.getOtherFee())
        ));
        map.put("usage", Map.of(
                "electricQuantity", safeAmount(invoice.getElectricQuantity()),
                "waterQuantity", safeAmount(invoice.getWaterQuantity())
        ));
        if (paidAmount != null) {
            map.put("paidAmount", paidAmount);
            map.put("paidAmountFormatted", formatMoney(paidAmount));
            map.put("remainingAmount", remaining);
            map.put("remainingAmountFormatted", formatMoney(remaining));
        }
        return map;
    }

    private Map<String, Object> feedbackBasicSummary(Feedback feedback) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", feedback.getId());
        map.put("title", feedback.getTitle());
        map.put("content", truncate(feedback.getContent(), 180));
        map.put("type", displayEnum(feedback.getFeedbackType()));
        map.put("status", displayEnum(feedback.getFeedbackStatus()));
        map.put("isRead", feedback.isRead());
        map.put("createdAt", formatDateTime(feedback.getCreatedAt()));
        return map;
    }

    private Map<String, Object> feedbackSummary(Feedback feedback) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", feedback.getId());
        map.put("title", feedback.getTitle());
        map.put("content", truncate(feedback.getContent(), 220));
        map.put("type", displayEnum(feedback.getFeedbackType()));
        map.put("status", displayEnum(feedback.getFeedbackStatus()));
        map.put("isRead", feedback.isRead());
        map.put("response", truncate(feedback.getResponse(), 180));
        map.put("createdAt", formatDateTime(feedback.getCreatedAt()));
        if (feedback.getApartment() != null) {
            map.put("apartment", apartmentSummary(feedback.getApartment()));
        }
        if (feedback.getSender() != null) {
            map.put("sender", residentSummary(feedback.getSender()));
        }
        if (feedback.getDevice() != null) {
            map.put("device", deviceSummary(feedback.getDevice()));
        }
        return map;
    }

    private Map<String, Object> maintenanceSummary(Maintenance maintenance) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", maintenance.getId());
        map.put("description", truncate(maintenance.getDescription(), 220));
        map.put("status", displayEnum(maintenance.getMaintenanceStatus()));
        map.put("startedDate", formatDate(maintenance.getStartedDate()));
        map.put("completedDate", formatDate(maintenance.getCompletedDate()));
        map.put("cost", maintenance.getCost());
        map.put("costFormatted", formatMoney(maintenance.getCost()));
        if (maintenance.getApartment() != null) {
            map.put("apartment", apartmentSummary(maintenance.getApartment()));
        }
        if (maintenance.getDevice() != null) {
            map.put("device", deviceSummary(maintenance.getDevice()));
        }
        return map;
    }

    private Map<String, Object> contractSummary(Contract contract) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", contract.getId());
        map.put("contractNumber", contract.getContractNumber());
        map.put("type", displayEnum(contract.getContractType()));
        map.put("status", displayEnum(contract.getContractStatus()));
        map.put("startDate", formatDate(contract.getStartDate()));
        map.put("endDate", formatDate(contract.getEndDate()));
        map.put("originalFileName", contract.getOriginalFileName());
        map.put("note", truncate(contract.getNote(), 180));
        if (contract.getResident() != null) {
            map.put("resident", residentSummary(contract.getResident()));
        }
        return map;
    }

    private Map<String, Object> vehicleSummary(Vehicle vehicle) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", vehicle.getId());
        map.put("licensePlate", vehicle.getLicensePlate());
        map.put("type", displayEnum(vehicle.getVehicleType()));
        map.put("vehicleName", vehicle.getVehicleName());
        return map;
    }

    private Map<String, Object> deviceSummary(Device device) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", device.getId());
        map.put("deviceName", device.getDeviceName());
        map.put("type", displayEnum(device.getDeviceType()));
        map.put("status", displayEnum(device.getDeviceStatus()));
        map.put("location", device.getLocation());
        map.put("installationDate", formatDate(device.getInstallationDate()));
        map.put("maintenanceCycleDay", device.getMaintenanceCycleDay());
        return map;
    }

    private Map<String, Object> paymentSummary(Payment payment) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", payment.getId());
        map.put("amount", payment.getAmount());
        map.put("amountFormatted", formatMoney(payment.getAmount()));
        map.put("paymentDateTime", formatDateTime(payment.getPaymentDateTime()));
        map.put("status", displayEnum(payment.getPaymentStatus()));
        map.put("method", displayEnum(payment.getPaymentMethod()));
        map.put("transactionNo", payment.getTransactionNo());
        map.put("txnRef", payment.getTxnRef());
        map.put("payerName", payment.getPayerName());
        map.put("payerUsername", payment.getPayerUsername());
        if (payment.getInvoice() != null) {
            map.put("invoiceId", payment.getInvoice().getId());
            map.put("invoiceNumber", payment.getInvoice().getInvoiceNumber());
        }
        return map;
    }

    private <T> Map<String, Long> countBy(Collection<T> items, Function<T, String> keyExtractor) {
        return items.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        item -> Optional.ofNullable(keyExtractor.apply(item)).filter(this::hasText).orElse("Chưa có"),
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    private String displayEnum(Enum<?> value) {
        if (value == null) return "Chưa có";
        if (value instanceof ApartmentStatus status) return status.getDisplayName();
        if (value instanceof ContractStatus status) return status.getDisplayName();
        if (value instanceof ContractType type) return type.getDisplayName();
        if (value instanceof DeviceStatus status) return status.getDisplayName();
        if (value instanceof FeedbackStatus status) return status.getDisplayName();
        if (value instanceof InvoiceStatus status) return status.getDisplayName();
        if (value instanceof MaintenanceStatus status) return status.getDisplayName();
        if (value instanceof PaymentStatus status) return status.getDisplayName();
        if (value instanceof RelationshipType type) return type.getDisplayName();
        if (value instanceof UserRole role) return role.getDisplayName();
        if (value instanceof VehicleType type) return type.getDisplayName();
        if (value instanceof DeviceType type) {
            return type == DeviceType.APARTMENT ? "Thiết bị căn hộ" : "Thiết bị dùng chung";
        }
        if (value instanceof FeedbackType type) {
            return switch (type) {
                case GENERAL -> "Phản ánh chung";
                case MAINTENANCE -> "Yêu cầu bảo trì";
                case SERVICE -> "Yêu cầu dịch vụ";
            };
        }
        if (value instanceof PaymentMethod method) {
            return switch (method) {
                case VNPAY -> "VNPay";
                case MOMO -> "MoMo";
                case CASH -> "Tiền mặt";
            };
        }
        return value.name();
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String formatMoney(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(safeAmount(value));
    }

    private String formatDate(LocalDate value) {
        return value != null ? value.toString() : null;
    }

    private String formatDateTime(LocalDateTime value) {
        return value != null ? value.toString() : null;
    }

    private String truncate(String value, int maxLength) {
        if (!hasText(value)) return null;
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private Long getLongArg(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && hasText(text)) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private int getIntArg(Map<String, Object> args, String key, int defaultValue) {
        Object value = args.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && hasText(text)) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private String getStringArg(Map<String, Object> args, String key) {
        Object value = args.get(key);
        return value != null ? String.valueOf(value) : null;
    }

    private String normalize(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private List<Map<String, Object>> extractParts(Map<String, Object> response) {
        List<Object> candidates = asList(response.get("candidates"));
        if (candidates.isEmpty()) {
            return List.of();
        }
        Map<String, Object> candidate = asMap(candidates.get(0));
        Map<String, Object> content = asMap(candidate.get("content"));
        return asList(content.get("parts")).stream()
                .map(this::asMap)
                .filter(part -> !part.isEmpty())
                .toList();
    }

    private List<ToolCall> extractToolCalls(Map<String, Object> response) {
        return extractParts(response).stream()
                .map(part -> asMap(part.get("functionCall")))
                .filter(call -> hasText(asString(call.get("name"))))
                .map(call -> new ToolCall(asString(call.get("name")), asString(call.get("id")), asMap(call.get("args"))))
                .toList();
    }

    private String extractText(Map<String, Object> response) {
        return extractParts(response).stream()
                .map(part -> asString(part.get("text")))
                .filter(this::hasText)
                .collect(Collectors.joining("\n"));
    }

    private Map<String, Object> content(String role, List<Map<String, Object>> parts) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("role", role);
        content.put("parts", parts);
        return content;
    }

    private Map<String, Object> textPart(String text) {
        return Map.of("text", text);
    }

    private List<Object> asList(Object value) {
        if (value instanceof List<?> list) {
            return new ArrayList<>(list);
        }
        return List.of();
    }

    private Map<String, Object> asMap(Object value) {
        if (!(value instanceof Map<?, ?> raw)) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        raw.forEach((key, item) -> {
            if (key != null) {
                result.put(String.valueOf(key), item);
            }
        });
        return result;
    }

    private String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record ToolCall(String name, String id, Map<String, Object> args) {}
}
