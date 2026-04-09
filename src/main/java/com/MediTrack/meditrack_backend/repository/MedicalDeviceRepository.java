package com.MediTrack.meditrack_backend.repository;

import com.MediTrack.meditrack_backend.model.enitity.MedicalDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalDeviceRepository extends JpaRepository<MedicalDevice, Integer> {
    List<MedicalDevice> findByDepartmentId(Integer departmentId);
}