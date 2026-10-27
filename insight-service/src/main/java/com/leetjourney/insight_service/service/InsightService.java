package com.leetjourney.insight_service.service;

import com.leetjourney.insight_service.client.UsageClient;
import com.leetjourney.insight_service.dto.DeviceDto;
import com.leetjourney.insight_service.dto.InsightDto;
import com.leetjourney.insight_service.dto.UsageDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class InsightService {

    private final UsageClient usageClient;
    private OllamaChatModel ollamaChatModel;

    public InsightDto getOverview(Long userId) {
        // fetch usage data from user service

        final UsageDto usageData = usageClient.getXDaysUsageForUser(userId, 3);

        double totalUsage = usageData.devices().stream()
                .mapToDouble(DeviceDto::energyConsumed)
                .sum();

        log.info("Calling Ollama API to get insights for userId: {}, totalUsage: {}",
                userId, totalUsage);

        // Call Ollama API to get insights based on totalUsage

        String prompt = "Analyse the following energy usage data and provide a " +
                "concise overview with actionable insights." +
                "This data is the aggregated data for the past 3 days." +
                "Usage data: \n" +
                usageData.devices();

        ChatResponse response = ollamaChatModel.call(
                Prompt.builder()
                        .content(prompt).build());

        return InsightDto.builder()
                .userId(userId)
                .tips(response.getResult().getOutput().getText())
                .energyUsage(totalUsage)
                .build();
    }

    public InsightDto getSavingTips(Long userId) {
        // fetch usage data from user service

        final UsageDto usageData = usageClient.getXDaysUsageForUser(userId, 1);

        double totalUsage = usageData.devices().stream()
                .mapToDouble(DeviceDto::energyConsumed)
                .sum();

        log.info("Calling Ollama API to get insights for userId: {}, totalUsage: {}",
                userId, totalUsage);

        // Call Ollama API to get insights based on totalUsage

        String prompt = "Analyse the following energy usage data and provide a " +
                "concise overview with actionable insights." +
                "This data is the aggregated data for the past 1 day." +
                "Usage data: \n" +
                usageData.devices();

        ChatResponse response = ollamaChatModel.call(
                Prompt.builder()
                        .content(prompt).build());

        return InsightDto.builder()
                .userId(userId)
                .tips(response.getResult().getOutput().getText())
                .energyUsage(totalUsage)
                .build();
    }


}
