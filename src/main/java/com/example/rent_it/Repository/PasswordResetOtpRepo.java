package com.example.rent_it.Repository;

import com.example.rent_it.Entity.PasswordResetOpt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetOtpRepo extends JpaRepository<PasswordResetOpt,Long> {
    Optional<PasswordResetOpt> findByEmail(String email);
    List<PasswordResetOpt> findAllByEmailOrderByIdDesc(String email);
    void deleteByEmail(String email);
}
