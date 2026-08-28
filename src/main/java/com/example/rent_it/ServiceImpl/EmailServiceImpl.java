package com.example.rent_it.ServiceImpl;

import com.example.rent_it.Service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    @Override
    public void sendEmail(String to, String subject, String message) {
      SimpleMailMessage mailMessage = new SimpleMailMessage();
      mailMessage.setFrom("dhanrajsapkale22@gmail.com");
      mailMessage.setTo(to);
      mailMessage.setSubject(subject);
      mailMessage.setText(message);
      mailSender.send(mailMessage);
    }
}
