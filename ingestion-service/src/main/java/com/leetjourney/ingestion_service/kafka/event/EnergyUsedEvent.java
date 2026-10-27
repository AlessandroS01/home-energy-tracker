package com.leetjourney.ingestion_service.kafka.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.Instant;

@Builder
public record EnergyUsedEvent  (
        Long deviceId,
        Double energyConsumed,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp ) {}
