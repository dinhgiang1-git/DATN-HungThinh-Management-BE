package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

@Service
public class VNPayService {

    @Value("${VNPAY_TMN_CODE:}")
    private String tmnCode;

    @Value("${VNPAY_HASH_SECRET:}")
    private String hashSecret;

    @Value("${VNPAY_PAY_URL:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String payUrl;

    @Value("${VNPAY_RETURN_URL:http://localhost:5173/payment-result}")
    private String returnUrl;

    @Value("${VNPAY_RESIDENT_RETURN_URL:http://localhost:5174/payment-result}")
    private String residentReturnUrl;

    @Value("${VNPAY_ALLOW_REQUEST_RETURN_URL:false}")
    private boolean allowRequestReturnUrl;

    private static final Set<String> ALLOWED_RETURN_HOSTS = Set.of(
            "localhost",
            "127.0.0.1",
            "dinhgiang1.xyz",
            "www.dinhgiang1.xyz"
    );

    public String createPaymentURL(
            Long invoiceId,
            BigDecimal amount,
            String source,
            String orderInfo,
            String txnRef,
            String requestedReturnUrl,
            String clientIp
    ) {
        assertConfigured();

        String fallbackReturnUrl = "resident".equalsIgnoreCase(source) ? residentReturnUrl : returnUrl;
        String actualReturnUrl = resolveReturnUrl(
                allowRequestReturnUrl ? requestedReturnUrl : null,
                fallbackReturnUrl
        );
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(calendar.getTime());
        calendar.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(calendar.getTime());

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(amount.longValue() * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", actualReturnUrl);
        params.put("vnp_IpAddr", normalizeIp(clientIp));
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);

        String hashData = buildHashData(params);
        String queryString = buildQueryString(params);
        String secureHash = hmacSHA512(hashSecret, hashData);
        return payUrl + "?" + queryString + "&vnp_SecureHash=" + secureHash;
    }

    public boolean isValidCallbackSignature(Map<String, String> params) {
        assertConfigured();
        String providedHash = params.get("vnp_SecureHash");
        if (providedHash == null || providedHash.isBlank()) {
            return false;
        }

        Map<String, String> signedParams = new TreeMap<>();
        params.forEach((key, value) -> {
            if (key != null
                    && value != null
                    && !key.equals("vnp_SecureHash")
                    && !key.equals("vnp_SecureHashType")) {
                signedParams.put(key, value);
            }
        });

        String expectedHash = hmacSHA512(hashSecret, buildHashData(signedParams));
        return MessageDigest.isEqual(
                expectedHash.getBytes(StandardCharsets.UTF_8),
                providedHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    private void assertConfigured() {
        if (tmnCode == null || tmnCode.isBlank() || hashSecret == null || hashSecret.isBlank()) {
            throw new UserMessageException("Chưa cấu hình thông tin thanh toán VNPay");
        }
    }

    private String resolveReturnUrl(String requestedReturnUrl, String fallbackReturnUrl) {
        String candidate = requestedReturnUrl == null || requestedReturnUrl.isBlank()
                ? fallbackReturnUrl
                : requestedReturnUrl.trim();
        URI uri = parseAndValidateReturnUrl(candidate);
        return uri.toString();
    }

    private URI parseAndValidateReturnUrl(String value) {
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || host == null
                    || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    || !ALLOWED_RETURN_HOSTS.contains(host.toLowerCase())) {
                throw new IllegalArgumentException();
            }
            return uri;
        } catch (IllegalArgumentException e) {
            throw new UserMessageException("URL chuyển hướng VNPay không hợp lệ");
        }
    }

    private String normalizeIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank() || "0:0:0:0:0:0:0:1".equals(clientIp)) {
            return "127.0.0.1";
        }
        return clientIp;
    }

    /**
     * Build hash data: key (raw) + "=" + value (URL-encoded)
     * This matches VNPay official sample code.
     */
    private String buildHashData(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        params.forEach((key, value) -> {
            if (value == null || value.isBlank()) return;
            if (!builder.isEmpty()) {
                builder.append("&");
            }
            builder.append(key).append("=").append(urlEncode(value));
        });
        return builder.toString();
    }

    /**
     * Build query string: key (URL-encoded) + "=" + value (URL-encoded)
     */
    private String buildQueryString(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        params.forEach((key, value) -> {
            if (value == null || value.isBlank()) return;
            if (!builder.isEmpty()) {
                builder.append("&");
            }
            builder.append(urlEncode(key)).append("=").append(urlEncode(value));
        });
        return builder.toString();
    }

    /**
     * Build hash fields for callback verification (raw keys and raw values).
     */
    private String buildHashFields(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        params.forEach((key, value) -> {
            if (value == null || value.isBlank()) return;
            if (!builder.isEmpty()) {
                builder.append("&");
            }
            builder.append(key).append("=").append(value);
        });
        return builder.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKeySpec);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b & 0xff));
            }
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa HMAC SHA512", e);
        }
    }
}
