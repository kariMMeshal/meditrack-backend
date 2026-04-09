package com.MediTrack.meditrack_backend.service;

import com.MediTrack.meditrack_backend.model.dto.DepartmentDTO;
import com.MediTrack.meditrack_backend.model.dto.MedicalDeviceDTO;

import java.util.List;

public interface DepartmentService {

    DepartmentDTO createDepartment(DepartmentDTO departmentDTO);

    DepartmentDTO updateDepartment(Integer id, DepartmentDTO departmentDTO);

    void deleteDepartment(Integer id);

    DepartmentDTO getDepartmentById(Integer id);

    List<DepartmentDTO> getAllDepartments();

    List<MedicalDeviceDTO> getDevicesByDepartmentId(Integer departmentId);
}
