package com.example.rent_it.Repository;

import com.example.rent_it.Entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailOtpRepo extends JpaRepository<EmailOtp, Long> {
    Optional<EmailOtp> findByEmail(String email);
}
