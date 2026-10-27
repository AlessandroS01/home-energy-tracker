package com.leetjourney.ingestion_service.controller;

import com.leetjourney.ingestion_service.dto.EnergyUsedDto;
import com.leetjourney.ingestion_service.service.IngestionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ingestion")
@AllArgsConstructor
public class IngestionController {

    private final IngestionService ingestionService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void ingestData(@RequestBody EnergyUsedDto energyUsedDto) {
        ingestionService.ingestEnergyUsage(energyUsedDto);
    }
}
