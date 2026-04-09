package com.MediTrack.meditrack_backend.repository;

import com.MediTrack.meditrack_backend.model.enitity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
}