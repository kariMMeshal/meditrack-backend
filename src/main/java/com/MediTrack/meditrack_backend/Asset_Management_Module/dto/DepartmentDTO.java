package com.MediTrack.meditrack_backend.Asset_Management_Module.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDTO {

    private Integer id;

    @NotBlank(message = "name is required")
    private String name;

    private String description;
}
