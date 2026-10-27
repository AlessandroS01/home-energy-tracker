package com.leetjourney.ingestion_service.service;

import com.leetjourney.ingestion_service.dto.EnergyUsedDto;
import com.leetjourney.ingestion_service.kafka.event.EnergyUsedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class IngestionService {

    private final KafkaTemplate<String, EnergyUsedEvent> kafkaTemplate;


    public void ingestEnergyUsage(EnergyUsedDto input) {
        EnergyUsedEvent event = EnergyUsedEvent.builder()
                .deviceId(input.deviceId())
                .energyConsumed(input.energyConsumed())
                .timestamp(input.timestamp())
                .build();

        // send to kafka topic
        log.info("Publishing energy usage event: {}", event);
        kafkaTemplate.send("energy-usage", event);
    }
}
