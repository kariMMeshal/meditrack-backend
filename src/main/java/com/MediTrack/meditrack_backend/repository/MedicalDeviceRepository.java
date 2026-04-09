package com.MediTrack.meditrack_backend.repository;

import com.MediTrack.meditrack_backend.model.enitity.MedicalDevice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalDeviceRepository extends JpaRepository<MedicalDevice, Integer> {
}