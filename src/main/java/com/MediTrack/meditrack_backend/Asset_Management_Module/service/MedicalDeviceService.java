package com.MediTrack.meditrack_backend.Asset_Management_Module.service;

import com.MediTrack.meditrack_backend.Asset_Management_Module.dto.MedicalDeviceDTO;
import com.MediTrack.meditrack_backend.Asset_Management_Module.enums.DeviceStatus;
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