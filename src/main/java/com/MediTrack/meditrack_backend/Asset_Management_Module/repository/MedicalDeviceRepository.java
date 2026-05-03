package com.MediTrack.meditrack_backend.Asset_Management_Module.repository;

import com.MediTrack.meditrack_backend.Asset_Management_Module.entity.MedicalDevice;
import com.MediTrack.meditrack_backend.Asset_Management_Module.enums.DeviceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalDeviceRepository extends JpaRepository<MedicalDevice, Integer> {
    List<MedicalDevice> findByDepartmentId(Integer departmentId);
    Page<MedicalDevice> findByStatus(DeviceStatus status, Pageable pageable);

    Page<MedicalDevice> findByDepartmentId(Integer departmentId, Pageable pageable);

    Page<MedicalDevice> findByStatusAndDepartmentId(DeviceStatus status, Integer departmentId, Pageable pageable);

    Page<MedicalDevice> findByNameContainingIgnoreCase(String name, Pageable pageable);
}