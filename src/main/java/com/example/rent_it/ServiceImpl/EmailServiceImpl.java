package com.example.rent_it.ServiceImpl;

import com.example.rent_it.Service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.mail.from:no-reply@rent-it.local}")
    private String fromAddress;

    @Override
    public void sendEmail(String to, String subject, String message) {
      if (!emailEnabled) {
        log.info("Email delivery is disabled; skipped message to {}", to);
        return;
      }

      SimpleMailMessage mailMessage = new SimpleMailMessage();
      mailMessage.setFrom(fromAddress);
      mailMessage.setTo(to);
      mailMessage.setSubject(subject);
      mailMessage.setText(message);
      try {
        mailSender.send(mailMessage);
      } catch (MailException ex) {
        log.warn("Unable to deliver email to {}: {}", to, ex.getMessage());
      }
    }
}
