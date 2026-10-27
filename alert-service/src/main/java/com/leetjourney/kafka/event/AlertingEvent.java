package com.leetjourney.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertingEvent {
    Long userId;
    String message;
    String email;
    Double energyConsumed;
    Double energyThreshold;
}
