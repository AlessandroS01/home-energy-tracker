package com.leetjourney.device_service.controller;

import com.leetjourney.device_service.dto.DeviceDto;
import com.leetjourney.device_service.service.DeviceService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
@AllArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;


    @GetMapping("/{id}")
    public ResponseEntity<DeviceDto> getDeviceById(@PathVariable Long id) {
        DeviceDto deviceDto = deviceService.getDeviceById(id);
        return ResponseEntity.ok(deviceDto);
    }

    @PostMapping("/create")
    public ResponseEntity<DeviceDto> createDevice(@RequestBody DeviceDto deviceDto) {
        DeviceDto createdDevice = deviceService.createDevice(deviceDto);
        return ResponseEntity.ok(createdDevice);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceDto> updateDevice(
            @PathVariable Long id,
            @RequestBody DeviceDto deviceDto) {
        DeviceDto updatedDevice = deviceService.updateDevice(id, deviceDto);
        return ResponseEntity.ok(updatedDevice);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

}
