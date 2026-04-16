package com.MediTrack.meditrack_backend.repository;

import com.MediTrack.meditrack_backend.model.enitity.MedicalDevice;
import com.MediTrack.meditrack_backend.util.enums.DeviceStatus;
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