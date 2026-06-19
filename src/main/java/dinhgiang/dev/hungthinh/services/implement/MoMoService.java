package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Lớp dịch vụ tích hợp thanh toán MoMo.
 * Tạo request và xử lý callback trả về từ cổng MoMo.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
public class MoMoService {

    @Value("${MOMO_PARTNER_CODE}")
    private String partnerCode;

    @Value("${MOMO_ACCESS_KEY}")
    private String accessKey;

    @Value("${MOMO_SECRET_KEY}")
    private String secretKey;

    @Value("${MOMO_API_URL}")
    private String apiUrl;

    @Value("${MOMO_REDIRECT_URL}")
    private String redirectUrl;

    @Value("${MOMO_IPN_URL}")
    private String ipnUrl;

    @Value("${MOMO_PUBLIC_IPN_URL:}")
    private String publicIpnUrl;

    @Value("${RESIDENT_RETURN_URL:http://localhost:5174/payment-result}")
    private String residentRedirectUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Set<String> ALLOWED_RETURN_HOSTS = Set.of(
            "localhost",
            "127.0.0.1",
            "dinhgiang1.xyz",
            "www.dinhgiang1.xyz"
    );

    public String createPaymentURL(Long invoiceId, BigDecimal amount, String source, String orderInfo, String extraData, String returnUrl) {
        try {
            String fallbackRedirectUrl = "resident".equalsIgnoreCase(source) ? residentRedirectUrl : redirectUrl;
            String actualRedirectUrl = resolveRedirectUrl(returnUrl, fallbackRedirectUrl);
            String actualIpnUrl = resolveIpnUrl(actualRedirectUrl);

            String requestId = UUID.randomUUID().toString();
            String orderId = invoiceId + "_" + System.currentTimeMillis();
            long amountLong = amount.longValue();
            if (extraData == null) extraData = "";
            String requestType = "captureWallet";

            // Build rawSignature theo format MoMo
            String rawSignature = "accessKey=" + accessKey
                    + "&amount=" + amountLong
                    + "&extraData=" + extraData
                    + "&ipnUrl=" + actualIpnUrl
                    + "&orderId=" + orderId
                    + "&orderInfo=" + orderInfo
                    + "&partnerCode=" + partnerCode
                    + "&redirectUrl=" + actualRedirectUrl
                    + "&requestId=" + requestId
                    + "&requestType=" + requestType;

            String signature = hmacSHA256(secretKey, rawSignature);

            // Build JSON body
            String jsonBody = "{"
                    + "\"partnerCode\":\"" + partnerCode + "\","
                    + "\"accessKey\":\"" + accessKey + "\","
                    + "\"requestId\":\"" + requestId + "\","
                    + "\"amount\":" + amountLong + ","
                    + "\"orderId\":\"" + orderId + "\","
                    + "\"orderInfo\":\"" + orderInfo + "\","
                    + "\"redirectUrl\":\"" + actualRedirectUrl + "\","
                    + "\"ipnUrl\":\"" + actualIpnUrl + "\","
                    + "\"extraData\":\"" + extraData + "\","
                    + "\"requestType\":\"" + requestType + "\","
                    + "\"signature\":\"" + signature + "\","
                    + "\"lang\":\"vi\""
                    + "}";

            // Call MoMo API
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream(),
                            StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            JsonNode jsonResponse = objectMapper.readTree(response.toString());
            int resultCode = jsonResponse.has("resultCode") ? jsonResponse.get("resultCode").asInt() : -1;

            if (resultCode == 0) {
                return jsonResponse.get("payUrl").asText();
            } else {
                String message = jsonResponse.has("message") ? jsonResponse.get("message").asText() : "Unknown error";
                throw new RuntimeException("MoMo API error: " + message + " (resultCode: " + resultCode + ")");
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo MoMo Payment URL", e);
        }
    }

    private String resolveRedirectUrl(String requestedReturnUrl, String fallbackRedirectUrl) {
        String candidate = requestedReturnUrl == null || requestedReturnUrl.isBlank()
                ? fallbackRedirectUrl
                : requestedReturnUrl.trim();
        URI uri = parseAndValidateReturnUrl(candidate);
        return uri.toString();
    }

    private String resolveIpnUrl(String actualRedirectUrl) {
        if (publicIpnUrl != null && !publicIpnUrl.isBlank()) {
            return publicIpnUrl.trim();
        }
        URI redirectUri = URI.create(actualRedirectUrl);
        String port = redirectUri.getPort() == -1 ? "" : ":" + redirectUri.getPort();
        return redirectUri.getScheme() + "://" + redirectUri.getHost() + port + "/api/v1/payment/momo-ipn";
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
            throw new UserMessageException("URL chuyển hướng MoMo không hợp lệ");
        }
    }

    public boolean isValidCallbackSignature(Map<String, String> params) {
        String providedSignature = params.get("signature");
        if (providedSignature == null || providedSignature.isBlank()) {
            return false;
        }

        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + valueOf(params, "amount")
                + "&extraData=" + valueOf(params, "extraData")
                + "&message=" + valueOf(params, "message")
                + "&orderId=" + valueOf(params, "orderId")
                + "&orderInfo=" + valueOf(params, "orderInfo")
                + "&orderType=" + valueOf(params, "orderType")
                + "&partnerCode=" + valueOf(params, "partnerCode")
                + "&payType=" + valueOf(params, "payType")
                + "&requestId=" + valueOf(params, "requestId")
                + "&responseTime=" + valueOf(params, "responseTime")
                + "&resultCode=" + valueOf(params, "resultCode")
                + "&transId=" + valueOf(params, "transId");

        String expectedSignature = hmacSHA256(secretKey, rawSignature);
        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                providedSignature.getBytes(StandardCharsets.UTF_8)
        );
    }

    public boolean isExpectedPartner(String callbackPartnerCode) {
        return partnerCode.equals(callbackPartnerCode);
    }

    private String valueOf(Map<String, String> params, String key) {
        return params.getOrDefault(key, "");
    }

    public static String hmacSHA256(String key, String data) {
        try {
            Mac hmac256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac256.init(secretKeySpec);
            byte[] bytes = hmac256.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa HMAC SHA256", e);
        }
    }
}
