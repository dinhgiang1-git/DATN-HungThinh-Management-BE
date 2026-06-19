package dinhgiang.dev.hungthinh.components;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationEmitterStore {
    private final Map<Long, List<SseEmitter>> emitters = new HashMap<>();

    public void add(Long residentId, SseEmitter emitter) {
        emitters.computeIfAbsent(residentId, k -> new ArrayList<>()).add(emitter);
    }

    public void remove(Long residentId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(residentId);
        if (list != null) {
            list.remove(emitter);
        }
    }

    public List<SseEmitter> get(Long residentId) {
        return emitters.getOrDefault(residentId, List.of());
    }
}
