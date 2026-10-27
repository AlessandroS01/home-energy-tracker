package com.leetjourney.ingestion_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.Instant;

@Builder
public record EnergyUsedDto (

    Long deviceId,
    Double energyConsumed,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant timestamp ) {}

