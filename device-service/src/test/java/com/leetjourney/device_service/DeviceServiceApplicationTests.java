package com.leetjourney.device_service;

import com.leetjourney.device_service.entity.Device;
import com.leetjourney.device_service.model.DeviceType;
import com.leetjourney.device_service.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class DeviceServiceApplicationTests {

	public static final int DEVICE_COUNT = 200;
	public static final int USER_COUNT = 10;

	@Autowired
	private DeviceRepository deviceRepository;

	@Test
	void contextLoads() {
	}

	@Disabled
	@Test
	void createDevices() {
		for (int i=1; i<=DEVICE_COUNT; i++) {
			var device = Device.builder()
					.name("Device-" + i)
					.type(DeviceType.values()[i % DeviceType.values().length])
					.location("Location-" + (i % 5 + 1))
					.userId((long) (i % USER_COUNT + 1))
					.build();

			deviceRepository.save(device);
		}
		log.info("Created {} devices", DEVICE_COUNT);
	}

}
