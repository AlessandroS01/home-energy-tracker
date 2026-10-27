package com.leetjourney.user_service.service;

import com.leetjourney.user_service.dto.UserDto;
import com.leetjourney.user_service.entity.User;
import com.leetjourney.user_service.exception.UserNotFoundException;
import com.leetjourney.user_service.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto createUser(UserDto input) {

        final User createdUser = User.builder()
                .name(input.getName())
                .surname(input.getSurname())
                .email(input.getEmail())
                .address(input.getAddress())
                .alerting(input.isAlerting())
                .energyAlertingThreshold(input.getEnergyAlertingThreshold())
                .build();

        User saved = userRepository.save(createdUser);

        return toDto(saved);
    }

    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }


    public void updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        user.setName(userDto.getName());
        user.setSurname(userDto.getSurname());
        user.setEmail(userDto.getEmail());
        user.setAddress(userDto.getAddress());
        user.setAlerting(userDto.isAlerting());
        user.setEnergyAlertingThreshold(userDto.getEnergyAlertingThreshold());

        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        userRepository.delete(user);
    }

    private UserDto toDto(User saved) {
        return UserDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .surname(saved.getSurname())
                .email(saved.getEmail())
                .address(saved.getAddress())
                .alerting(saved.isAlerting())
                .energyAlertingThreshold(saved.getEnergyAlertingThreshold())
                .build();
    }

}
