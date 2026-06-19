package dinhgiang.dev.hungthinh.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, List<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public NotificationWebSocketHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long residentId = extractResidentId(session);
        if (residentId != null) {
            sessions.computeIfAbsent(residentId, k -> new CopyOnWriteArrayList<>()).add(session);
            log.info("[WebSocket] Resident {} connected. Session: {}", residentId, session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long residentId = extractResidentId(session);
        if (residentId != null) {
            List<WebSocketSession> list = sessions.get(residentId);
            if (list != null) {
                list.remove(session);
                if (list.isEmpty()) {
                    sessions.remove(residentId);
                }
            }
            log.info("[WebSocket] Resident {} disconnected. Status: {}", residentId, status);
        }
    }

    public void sendToResident(Long residentId, Object data) {
        List<WebSocketSession> list = sessions.get(residentId);
        if (list == null || list.isEmpty()) return;

        try {
            String json = objectMapper.writeValueAsString(data);
            TextMessage message = new TextMessage(json);
            for (WebSocketSession session : list) {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            }
            log.info("[WebSocket] Sent notification to resident {}", residentId);
        } catch (Exception e) {
            log.error("[WebSocket] Error sending to resident {}: {}", residentId, e.getMessage());
        }
    }

    private Long extractResidentId(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri != null) {
                String path = uri.getPath();
                // /ws/notifications/{residentId}
                String[] parts = path.split("/");
                return Long.parseLong(parts[parts.length - 1]);
            }
        } catch (Exception e) {
            log.error("[WebSocket] Cannot extract residentId: {}", e.getMessage());
        }
        return null;
    }
}
