package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.components.NotificationEmitterStore;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications/stream/{residentId}")
public class SseNotificationController {
    private final NotificationEmitterStore notificationEmitterStore;

    @GetMapping()
    public SseEmitter stream(@PathVariable Long residentId) {
        SseEmitter emitter = new SseEmitter(0L);
        notificationEmitterStore.add(residentId, emitter);

        emitter.onCompletion(() -> notificationEmitterStore.remove(residentId, emitter));
        emitter.onTimeout(() -> notificationEmitterStore.remove(residentId, emitter));
        emitter.onError(e -> notificationEmitterStore.remove(residentId, emitter));

        return emitter;
    }



}
