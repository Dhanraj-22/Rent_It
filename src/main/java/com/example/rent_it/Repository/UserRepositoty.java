package com.example.rent_it.Repository;

import com.example.rent_it.DTO.UserDto;
import com.example.rent_it.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepositoty extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User>findAll();
    boolean existsByEmail(String email);
}
