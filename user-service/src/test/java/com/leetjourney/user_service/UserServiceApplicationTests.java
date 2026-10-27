package com.leetjourney.user_service;

import com.leetjourney.user_service.entity.User;
import com.leetjourney.user_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class UserServiceApplicationTests {

	public static final int USER_COUNT = 10;

	@Autowired
	private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

	@Disabled
	@Test
	void createUsers() {
		for (int i=1; i<=USER_COUNT; i++) {
			var user = User.builder()
					.name("User-" + i)
					.surname("Surname-" + i)
					.email("user" + i + "@example.com")
					.address("Address-" + i)
					.alerting(i % 2 == 0)
					.energyAlertingThreshold(i % 2 == 0 ? (100.0 + i) : 0.0)
					.build();

			userRepository.save(user);
		}

		log.info("Created {} users", USER_COUNT);
	}

}
