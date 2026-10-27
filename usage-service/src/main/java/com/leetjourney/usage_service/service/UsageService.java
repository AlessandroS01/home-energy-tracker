package com.leetjourney.usage_service.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.leetjourney.kafka.event.AlertingEvent;
import com.leetjourney.kafka.event.EnergyUsageEvent;
import com.leetjourney.usage_service.client.DeviceClient;
import com.leetjourney.usage_service.client.UserClient;
import com.leetjourney.usage_service.dto.DeviceDto;
import com.leetjourney.usage_service.dto.UsageDto;
import com.leetjourney.usage_service.dto.UserDto;
import com.leetjourney.usage_service.model.Device;
import com.leetjourney.usage_service.model.DeviceEnergy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UsageService {

    private final InfluxDBClient influxDBClient;
    private final DeviceClient deviceClient;
    private final UserClient userClient;

    @Value("${influx.bucket}")
    private String influxBucket;

    @Value("${influx.org}")
    private String influxOrg;

    private final KafkaTemplate<String, AlertingEvent> kafkaTemplate;

    public UsageService(InfluxDBClient influxDBClient,
                        DeviceClient deviceClient,
                        UserClient userClient,
                        KafkaTemplate<String, AlertingEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.deviceClient = deviceClient;
        this.userClient = userClient;
        this.influxDBClient = influxDBClient;
    }

    @KafkaListener(topics = "energy-usage", groupId = "usage-service")
    public void energyUsageEvent(EnergyUsageEvent energyUsageEvent) {
        // log.info("Received energy usage event: {}", energyUsageEvent);

        Point point = Point.measurement("energy_usage")
                .addTag("deviceId", String.valueOf(energyUsageEvent.deviceId()))
                .addField("energyConsumed", energyUsageEvent.energyConsumed())
                .time(energyUsageEvent.timestamp(), WritePrecision.MS);

        influxDBClient.getWriteApiBlocking().writePoint(influxBucket, influxOrg, point);
    }


    @Scheduled(cron = "*/10 * * * * *") // Every 10 seconds
    public void aggregateDeviceEnergyUsage() {
        final Instant now = Instant.now();
        final Instant oneHourAgo = now.minusSeconds(3600);

        String fluxQuery = String.format("""
            from(bucket: "%s")
                |> range(start: %s, stop: %s)
                |> filter(fn: (r) => r["_measurement"] == "energy_usage")
                |> filter(fn: (r) => r["_field"] == "energyConsumed")
                |> group(columns: ["deviceId"])
                |> sum(column: "_value")
            """, influxBucket, oneHourAgo.toString(), now);

        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery, influxOrg);

        List<DeviceEnergy> deviceEnergies = new ArrayList<>();

        for (FluxTable table: tables) {
            for (FluxRecord record: table.getRecords()) {
                String deviceId = (String) record.getValueByKey("deviceId");
                Double energyConsumed = record.getValueByKey("_value") instanceof Number ?
                        ((Number) record.getValueByKey("_value")).doubleValue() : 0.0;

                deviceEnergies.add(
                        DeviceEnergy.builder()
                                .deviceId(Long.valueOf(deviceId))
                                .energyConsumed(energyConsumed)
                                .build()
                );

            }
        }

        log.info("Aggregated device energy usage for the last hour: {}", deviceEnergies);

        for (DeviceEnergy deviceEnergy: deviceEnergies) {
            try {
                final DeviceDto deviceResponse = deviceClient.getDeviceById(deviceEnergy.getDeviceId());

                if (deviceResponse == null || deviceResponse.id() == null) {
                    log.warn("Device with ID {} not found. Skipping.", deviceEnergy.getDeviceId());
                    continue;
                }

                deviceEnergy.setUserId(deviceResponse.userId());
            } catch (Exception e) {
                log.error("Error fetching device with ID {}: {}", deviceEnergy.getDeviceId(), e.getMessage());
            }

        }

        // remove devices with null userId
        deviceEnergies.removeIf(de -> de.getUserId() == null);

        // get user-device mapping and aggregate per user
        Map<Long, List<DeviceEnergy>> userDeviceMap =
                deviceEnergies.stream()
                .collect(Collectors.groupingBy(DeviceEnergy::getUserId));

        log.info("Aggregated device energy usage per user for the last hour: {}", userDeviceMap);

        // get users energy usage consumption and log it
        List<Long> userIds = new ArrayList<>(userDeviceMap.keySet());
        final Map<Long, Double> userThresholdMap = new HashMap<>();
        final Map<Long, String> userEmailMap = new HashMap<>();

        for (final Long userId: userIds) {
            try {
                UserDto user = userClient.getUserById(userId);
                if (user == null || user.id() == null || !user.alerting()) {
                    log.warn("User with ID {} not alerting. Skipping.", userId);
                    continue;
                }

                userThresholdMap.put(userId, user.energyAlertingThreshold());
                userEmailMap.put(userId, user.email());
            } catch (Exception e) {
                log.error("Error fetching user with ID {}: {}", userId, e.getMessage());
            }
        }

        log.info("User energy alerting thresholds: {}", userThresholdMap);

        // check if any user has exceeded their energy alerting threshold

        final List<Long> alertedUsers = new ArrayList<>(userThresholdMap.keySet());

        for (final Long userId: alertedUsers) {
            final Double threshold = userThresholdMap.get(userId);
            final List<DeviceEnergy> devices = userDeviceMap.get(userId);

            final Double totalConsumption = devices.stream()
                    .mapToDouble(DeviceEnergy::getEnergyConsumed)
                    .sum();

            if (totalConsumption > threshold) {
                log.info("User with ID {} has exceeded their energy alerting threshold. " +
                                "Total consumption: {}, Threshold: {}, Email: {}",
                        userId, totalConsumption, threshold, userEmailMap.get(userId));

                // Put message to Kafka topic for alerting service

                final AlertingEvent alertingEvent = AlertingEvent.builder()
                        .userId(userId)
                        .message("User has exceeded their energy alerting threshold.")
                        .email(userEmailMap.get(userId))
                        .energyConsumed(totalConsumption)
                        .energyThreshold(threshold)
                        .build();

                kafkaTemplate.send("energy-alerts", alertingEvent);
            } else {
                log.info("User with ID {} has NOT exceeded their energy alerting threshold. " +
                                "Total consumption: {}, Threshold: {}, Email: {}",
                        userId, totalConsumption, threshold, userEmailMap.get(userId));
            }
        }
    }

    public UsageDto getXDaysUsageForUser(Long userId, Integer days) {
        log.info("Fetching usage data for userId: {} for the last {} days", userId, days);
        final List<DeviceDto> userDevices = deviceClient.getAllDevicesForUser(userId);

        final List<Device> devices = new ArrayList<>();
        for (DeviceDto deviceDto: userDevices) {
            devices.add(Device.builder()
                    .id(deviceDto.id())
                    .userId(deviceDto.userId())
                    .name(deviceDto.name())
                    .type(deviceDto.type())
                    .location(deviceDto.location())
                    .build());
        }

        if (userDevices.isEmpty()) {
            return UsageDto.builder()
                    .userId(userId)
                    .devices(null)
                    .build();
        }

        // build set of device IDs
        List<String> deviceIdStrings = devices.stream()
                .map(Device::getId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList();

        final Instant now = Instant.now();
        final Instant start = now.minusSeconds((long) days * 24 * 3600);

        final String deviceFilter = deviceIdStrings.stream()
                .map(idStr -> String.format("r[\"deviceId\"] == \"%s\"", idStr))
                .collect(Collectors.joining(" or "));

        String fluxQuery = String.format("""
            from(bucket: "%s")
                |> range(start: %s, stop: %s)
                |> filter(fn: (r) => r["_measurement"] == "energy_usage")
                |> filter(fn: (r) => r["_field"] == "energyConsumed")
                |> filter(fn: (r) => %s)
                |> group(columns: ["deviceId"])
                |> sum(column: "_value")
            """, influxBucket, start.toString(), now, deviceFilter);

        final Map<Long, Double> aggregatedMap = new HashMap<>();

        try {
            QueryApi queryApi = influxDBClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(fluxQuery, influxOrg);

            for (FluxTable table: tables) {
                for (FluxRecord record: table.getRecords()) {
                    Object deviceIdObj = record.getValueByKey("deviceId");
                    String deviceIdStr = deviceIdObj != null ? deviceIdObj.toString() : null;

                    if (deviceIdStr == null) continue;

                    Double energyConsumed = record.getValueByKey("_value") instanceof Number
                            ? ((Number) record.getValueByKey("_value")).doubleValue()
                            : 0.0;

                    try {
                        Long deviceId = Long.valueOf(deviceIdStr);
                        aggregatedMap.put(
                                deviceId,
                                aggregatedMap.getOrDefault(deviceId, 0.0) + energyConsumed);
                    } catch (NumberFormatException e) {
                        log.warn("Invalid deviceId format in InfluxDB record: {}. Skipping.", deviceIdStr);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error querying InfluxDB: {}", e.getMessage());

            devices.forEach(device -> device.setEnergyConsumed(0.0));
            return UsageDto.builder().
                    userId(userId).
                    devices(userDevices).
                    build();
        }

        // populate aggregated energy consumption into devices

        for (Device device: devices) {
            if (device == null || device.getId() == null) continue;
            device.setEnergyConsumed(aggregatedMap.getOrDefault(device.getId(), 0.0));
        }

        log.info("Aggregated energy consumption for userId {}: {}", userId, aggregatedMap);

        final List<DeviceDto> resultDevices = devices.stream()
                .map(device -> DeviceDto.builder()
                        .id(device.getId())
                        .userId(device.getUserId())
                        .name(device.getName())
                        .type(device.getType())
                        .location(device.getLocation())
                        .energyConsumed(device.getEnergyConsumed())
                        .build())
                .toList();

        return UsageDto.builder()
                .userId(userId)
                .devices(resultDevices)
                .build();
    }

}
