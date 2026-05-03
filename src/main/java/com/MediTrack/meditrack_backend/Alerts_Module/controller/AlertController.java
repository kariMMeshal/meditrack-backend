package com.MediTrack.meditrack_backend.Alerts_Module.controller;




import com.MediTrack.meditrack_backend.Alerts_Module.dto.AlertDTO;
import com.MediTrack.meditrack_backend.Alerts_Module.dto.AlertReportDTO;
import com.MediTrack.meditrack_backend.Alerts_Module.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    // SSE emitters — one per connected client
    // CopyOnWriteArrayList is thread-safe for concurrent add/remove
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // ── GET endpoints ─────────────────────────────────────────────────

    /**
     * Role-aware alert list.
     * ADMIN → all, BIOMED → device+AI, USER → own alerts
     */
    @GetMapping
    public ResponseEntity<Page<AlertDTO>> getAlerts(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(alertService.getAlertsByCurrentUser(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getById(id));
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<Page<AlertDTO>> getByDevice(
            @PathVariable Integer deviceId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(alertService.getByDevice(deviceId, pageable));
    }

    // ── Status transitions ────────────────────────────────────────────

    @PutMapping("/{id}/read")
    public ResponseEntity<AlertDTO> markAsRead(@PathVariable Long id) {
        AlertDTO updated = alertService.markAsRead(id);
        pushToSseClients(updated);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/acknowledge")
    public ResponseEntity<AlertDTO> acknowledge(@PathVariable Long id) {
        AlertDTO updated = alertService.acknowledgeAlert(id);
        pushToSseClients(updated);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<AlertDTO> resolve(@PathVariable Long id) {
        AlertDTO updated = alertService.resolveAlert(id);
        pushToSseClients(updated);
        return ResponseEntity.ok(updated);
    }

    // ── AI Report ─────────────────────────────────────────────────────

    @GetMapping("/report")
    public ResponseEntity<AlertReportDTO> generateReport(
            @RequestParam(required = false) Integer deviceId) {
        return ResponseEntity.ok(alertService.generateReport(deviceId));
    }

    // ── SSE — Real-time alert stream ──────────────────────────────────

    /**
     * Frontend connects to this endpoint to receive real-time alert updates.
     *
     * Usage (JavaScript):
     *   const source = new EventSource('/api/alerts/stream', { withCredentials: true });
     *   source.onmessage = (e) => console.log(JSON.parse(e.data));
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(()    -> emitters.remove(emitter));
        emitter.onError(e  ->    emitters.remove(emitter));

        return emitter;
    }

    // ── SSE Push Helper ───────────────────────────────────────────────

    /**
     * Broadcasts an alert update to all connected SSE clients.
     * Dead emitters are cleaned up silently.
     */
    public void pushToSseClients(AlertDTO alert) {
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("alert-update")
                        .data(alert));
            } catch (IOException ex) {
                emitters.remove(emitter);
            }
        });
    }
}
