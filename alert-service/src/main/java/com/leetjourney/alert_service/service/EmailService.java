package com.leetjourney.alert_service.service;

import com.leetjourney.alert_service.entity.Alert;
import com.leetjourney.alert_service.repository.AlertRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@AllArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final AlertRepository alertRepository;


    public void sendEmail(
            String to,
            String subject,
            String body,
            Long userId) {
        log.info("Sending email to: {}, subject: {}, body: {}", to, subject, body);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom("noreply@energytracker.com");
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);

            // save it to the database
            final Alert alertEntity = Alert.builder()
                    .userId(userId)
                    .sent(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            alertRepository.saveAndFlush(alertEntity);

        } catch (Exception e) {
            log.error("Failed to send email to userId: {}. Error: {}",
                    userId, e.getMessage());

            // save the failed email attempt to the database
            final Alert alertEntity = Alert.builder()
                    .userId(userId)
                    .sent(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            alertRepository.saveAndFlush(alertEntity);
        }
        log.info("Email sent successfully to userId: {}", userId);
    }
}
