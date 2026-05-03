package com.MediTrack.meditrack_backend.Auth_Module.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDTO {
    private Integer id;

    @NotBlank(message = "role is required")
    private String role;
}
