package com.MediTrack.meditrack_backend.Asset_Management_Module.service;

import com.MediTrack.meditrack_backend.Asset_Management_Module.dto.MedicalDeviceDTO;
import com.MediTrack.meditrack_backend.Asset_Management_Module.entity.Department;
import com.MediTrack.meditrack_backend.Asset_Management_Module.entity.MedicalDevice;
import com.MediTrack.meditrack_backend.Asset_Management_Module.repository.DepartmentRepository;
import com.MediTrack.meditrack_backend.Asset_Management_Module.repository.MedicalDeviceRepository;
import com.MediTrack.meditrack_backend.Asset_Management_Module.enums.DeviceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MedicalDeviceServiceImpl implements MedicalDeviceService {

    private final MedicalDeviceRepository deviceRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public MedicalDeviceDTO createDevice(MedicalDeviceDTO dto) {
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        MedicalDevice device = MedicalDevice.builder()
                .name(dto.getName())
                .model(dto.getModel())
                .manufacturer(dto.getManufacturer())
                .serialNumber(dto.getSerialNumber())
                .assetTag(dto.getAssetTag())
                .status(dto.getStatus())
                .conditionDescription(dto.getConditionDescription())
                .department(department)
                .purchaseDate(dto.getPurchaseDate())
                .warrantyExpiryDate(dto.getWarrantyExpiryDate())
                .location(dto.getLocation())
                .supplier(dto.getSupplier())
                .purchasePrice(dto.getPurchasePrice())
                .lastMaintenanceDate(dto.getLastMaintenanceDate())
                .nextMaintenanceDate(dto.getNextMaintenanceDate())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        MedicalDevice saved = deviceRepository.save(device);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public MedicalDeviceDTO updateDevice(Integer id, MedicalDeviceDTO dto) {
        MedicalDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            device.setDepartment(department);
        }

        device.setName(dto.getName());
        device.setModel(dto.getModel());
        device.setManufacturer(dto.getManufacturer());
        device.setSerialNumber(dto.getSerialNumber());
        device.setAssetTag(dto.getAssetTag());
        device.setStatus(dto.getStatus());
        device.setConditionDescription(dto.getConditionDescription());
        device.setLocation(dto.getLocation());
        device.setSupplier(dto.getSupplier());
        device.setPurchasePrice(dto.getPurchasePrice());
        device.setPurchaseDate(dto.getPurchaseDate());
        device.setWarrantyExpiryDate(dto.getWarrantyExpiryDate());
        device.setLastMaintenanceDate(dto.getLastMaintenanceDate());
        device.setNextMaintenanceDate(dto.getNextMaintenanceDate());
        device.setUpdatedAt(LocalDateTime.now());

        MedicalDevice updated = deviceRepository.save(device);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteDevice(Integer id) {
        deviceRepository.deleteById(id);
    }

    @Override
    public MedicalDeviceDTO getDeviceById(Integer id) {
        MedicalDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found"));
        return mapToDTO(device);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicalDeviceDTO> getAllDevices(
            DeviceStatus status,
            String name,
            Pageable pageable
    ) {

        Page<MedicalDevice> devices;

          if (status != null) {
            devices = deviceRepository.findByStatus(status, pageable);

        } else if (name != null && !name.isEmpty()) {
            devices = deviceRepository.findByNameContainingIgnoreCase(name, pageable);

        } else {
            devices = deviceRepository.findAll(pageable);
        }

        return devices.map(this::mapToDTO);
    }

    private MedicalDeviceDTO mapToDTO(MedicalDevice device) {
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