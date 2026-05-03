package com.MediTrack.meditrack_backend.Asset_Management_Module.service;

import com.MediTrack.meditrack_backend.Asset_Management_Module.dto.DepartmentDTO;
import com.MediTrack.meditrack_backend.Asset_Management_Module.dto.MedicalDeviceDTO;
import com.MediTrack.meditrack_backend.Asset_Management_Module.entity.Department;
import com.MediTrack.meditrack_backend.Asset_Management_Module.entity.MedicalDevice;
import com.MediTrack.meditrack_backend.Asset_Management_Module.repository.DepartmentRepository;
import com.MediTrack.meditrack_backend.Asset_Management_Module.repository.MedicalDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final MedicalDeviceRepository medicalDeviceRepository;

    @Override
    @Transactional
    public DepartmentDTO createDepartment(DepartmentDTO departmentDTO) {
        Department department = Department.builder()
                .name(departmentDTO.getName())
                .description(departmentDTO.getDescription())
                .build();

        Department saved = departmentRepository.save(department);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public DepartmentDTO updateDepartment(Integer id, DepartmentDTO departmentDTO) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        department.setName(departmentDTO.getName());
        department.setDescription(departmentDTO.getDescription());

        Department updated = departmentRepository.save(department);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteDepartment(Integer id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        departmentRepository.delete(department);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDTO getDepartmentById(Integer id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        return mapToDTO(department);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalDeviceDTO> getDevicesByDepartmentId(Integer departmentId) {
        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        return medicalDeviceRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToDeviceDTO)
                .collect(Collectors.toList());
    }

    private DepartmentDTO mapToDTO(Department department) {
        return DepartmentDTO.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .build();
    }

    private MedicalDeviceDTO mapToDeviceDTO(MedicalDevice device) {
        return MedicalDeviceDTO.builder()
                .id(device.getId())
                .assetTag(device.getAssetTag())
                .name(device.getName())
                .model(device.getModel())
                .manufacturer(device.getManufacturer())
                .serialNumber(device.getSerialNumber())
                .status(device.getStatus())
                .conditionDescription(device.getConditionDescription())
                .departmentId(device.getDepartment() != null ? device.getDepartment().getId() : null)
                .departmentName(device.getDepartment() != null ? device.getDepartment().getName() : null)
                .location(device.getLocation())
                .supplier(device.getSupplier())
                .purchasePrice(device.getPurchasePrice())
                .purchaseDate(device.getPurchaseDate())
                .warrantyExpiryDate(device.getWarrantyExpiryDate())
                .lastMaintenanceDate(device.getLastMaintenanceDate())
                .nextMaintenanceDate(device.getNextMaintenanceDate())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }
}
