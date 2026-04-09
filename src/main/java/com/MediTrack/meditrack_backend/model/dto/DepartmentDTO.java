package com.MediTrack.meditrack_backend.model.dto;

import com.MediTrack.meditrack_backend.model.enitity.MedicalDevice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentDTO {
    private Integer id;
    private String name;
    private String description;
    private List<MedicalDevice> devices;
}
