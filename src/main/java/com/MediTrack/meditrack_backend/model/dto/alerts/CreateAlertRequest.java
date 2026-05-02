package com.MediTrack.meditrack_backend.model.dto.alerts;

import com.MediTrack.meditrack_backend.util.enums.AlertSeverity;
import com.MediTrack.meditrack_backend.util.enums.AlertType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateAlertRequest {
    private AlertType type;
    private AlertSeverity severity;
    private String message;
    private Integer deviceId;
    private Integer userId;
    private String metadata;  // JSON string — flexible context
}
