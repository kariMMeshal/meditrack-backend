package com.MediTrack.meditrack_backend.service;

import com.MediTrack.meditrack_backend.model.dto.MedicalDeviceDTO;

import java.util.List;

public interface MedicalDeviceService {

    MedicalDeviceDTO createDevice(MedicalDeviceDTO deviceDTO);

    MedicalDeviceDTO updateDevice(Integer id, MedicalDeviceDTO deviceDTO);

    void deleteDevice(Integer id);

    MedicalDeviceDTO getDeviceById(Integer id);

    List<MedicalDeviceDTO> getAllDevices();
}