package com.leetjourney.usage_service.controller;

import com.leetjourney.usage_service.dto.UsageDto;
import com.leetjourney.usage_service.service.UsageService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usage")
@AllArgsConstructor
public class UsageController {

    private final UsageService usageService;

    @GetMapping("/{userId}")
    public ResponseEntity<UsageDto> getUserDeviceUsage(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "3") Integer days
    ) {
        final UsageDto usageData = usageService.getXDaysUsageForUser(userId, days);
        return ResponseEntity.ok(usageData);
    }
}
