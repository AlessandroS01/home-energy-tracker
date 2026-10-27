package com.leetjourney.usage_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceEnergy {
    private Long deviceId;
    private Double energyConsumed;
    private Long userId;
}