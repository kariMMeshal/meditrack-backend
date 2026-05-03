package com.MediTrack.meditrack_backend.Asset_Management_Module.repository;

import com.MediTrack.meditrack_backend.Asset_Management_Module.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
}