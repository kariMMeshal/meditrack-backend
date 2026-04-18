package com.MediTrack.meditrack_backend.service;

import com.MediTrack.meditrack_backend.model.dto.MedicalDeviceDTO;
import com.MediTrack.meditrack_backend.util.enums.DeviceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface MedicalDeviceService {

    MedicalDeviceDTO createDevice(MedicalDeviceDTO deviceDTO);

    MedicalDeviceDTO updateDevice(Integer id, MedicalDeviceDTO deviceDTO);

    void deleteDevice(Integer id);

    MedicalDeviceDTO getDeviceById(Integer id);

    Page<MedicalDeviceDTO> getAllDevices(
            DeviceStatus status,
            String name,
            Pageable pageable
    );
}