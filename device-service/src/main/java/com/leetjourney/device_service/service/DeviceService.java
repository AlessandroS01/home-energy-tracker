package com.leetjourney.device_service.service;

import com.leetjourney.device_service.dto.DeviceDto;
import com.leetjourney.device_service.entity.Device;
import com.leetjourney.device_service.exception.DeviceNotFoundException;
import com.leetjourney.device_service.repository.DeviceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceDto getDeviceById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));
        return mapToDto(device);
    }

    public DeviceDto createDevice(DeviceDto input) {
        Device device = Device.builder()
                .name(input.getName())
                .type(input.getType())
                .location(input.getLocation())
                .userId(input.getUserId())
                .build();

        final Device savedDevice = deviceRepository.save(device);
        return mapToDto(savedDevice);
    }

    public DeviceDto updateDevice(Long id, DeviceDto deviceDto) {
        Device existingDevice = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));

        existingDevice.setName(deviceDto.getName());
        existingDevice.setType(deviceDto.getType());
        existingDevice.setLocation(deviceDto.getLocation());
        existingDevice.setUserId(deviceDto.getUserId());

        final Device updatedDevice = deviceRepository.save(existingDevice);
        return mapToDto(updatedDevice);
    }

    public void deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new DeviceNotFoundException("Device not found with id: " + id);
        }
        deviceRepository.deleteById(id);
    }


    private DeviceDto mapToDto(Device device) {
        return DeviceDto.builder()
                .id(device.getId())
                .name(device.getName())
                .type(device.getType())
                .location(device.getLocation())
                .userId(device.getUserId())
                .build();
    }
}