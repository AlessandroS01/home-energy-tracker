package com.leetjourney.kafka.event;

import lombok.Builder;

@Builder
public record AlertingEvent(
        Long userId,
        String message,
        String email,
        Double energyConsumed,
        Double energyThreshold
) {
}
