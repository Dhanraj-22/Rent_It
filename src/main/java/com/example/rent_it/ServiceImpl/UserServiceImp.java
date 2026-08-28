package com.example.rent_it.ServiceImpl;
import com.example.rent_it.DTO.UserDto;
import com.example.rent_it.Entity.EmailOtp;
import com.example.rent_it.Entity.PendingUser;
import com.example.rent_it.Entity.Role;
import com.example.rent_it.Entity.User;
import com.example.rent_it.Exception.ResourceNotFoundException;
import com.example.rent_it.Repository.EmailOtpRepo;
import com.example.rent_it.Repository.PendingUserRepository;
import com.example.rent_it.Repository.UserRepositoty;
import com.example.rent_it.Service.EmailService;
import com.example.rent_it.Service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


@Service
public class UserServiceImp implements UserService {

    @Autowired
    private UserRepositoty userRepositoty;

    @Autowired
    private PendingUserRepository pendingUserRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private EmailOtpRepo emailOtpRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private  PasswordEncoder passwordEncoder;

    @Override
    public UserDto createNewUser(UserDto userDto) {

        if (userRepositoty.existsByEmail(userDto.getEmail())) {
            throw new ResourceNotFoundException("User already exists with email = " + userDto.getEmail());
        }

        pendingUserRepository.findByEmail(userDto.getEmail()).ifPresent(existingPendingUser -> {
            if (existingPendingUser.getExpiryTime().isAfter(LocalDateTime.now())) {
                throw new ResourceNotFoundException("Otp is already sent on this email");
            }

            pendingUserRepository.delete(existingPendingUser);
        });

        String role = normalizeRole(userDto.getRole());
        if (userDto.getPassword() == null || userDto.getPassword().isBlank()) {
            throw new ResourceNotFoundException("Password is required");
        }

        String otp=generateOtp();
        PendingUser pendingUser=new PendingUser();
        pendingUser.setEmail(userDto.getEmail());
        pendingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        pendingUser.setName(userDto.getName());
        pendingUser.setRole(role);
        pendingUser.setPhone(userDto.getPhone());
        pendingUser.setOtp(otp);
        pendingUser.setExpiryTime(LocalDateTime.now().plusMinutes(3));
        pendingUserRepository.save(pendingUser);


        emailService.sendEmail(userDto.getEmail(),
                "Otp verification",
                "Registration for Rent_It webpage otp is: "+otp+" valid only for 3 minutes");
        UserDto response = new UserDto();
        response.setName(userDto.getName());
        response.setEmail(userDto.getEmail());
        response.setRole(role);
        response.setPhone(userDto.getPhone());
        response.setActive(false);
        response.setEmailVerified(false);
        return response;
    }


    @Override
    public UserDto findById(Long id) {

        User user = userRepositoty.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id = " + id));

        return toDto(user);
    }


    @Override
    public UserDto findByEmail(String email) {

        User user = userRepositoty.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email = " + email));

        return toDto(user);
    }


    @Override
    public List<UserDto> findAll() {

        List<User> users = userRepositoty.findAll();

        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public String generateOtp(){

        Random random = new Random();

        int otp = 100000 + random.nextInt(900000);

        return String.valueOf(otp);
    }


    public UserDto varifyOtp(String email, String otp) {

        PendingUser pendingUser = pendingUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email = " + email));

        if (pendingUser.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new ResourceNotFoundException("OTP is expired");
        }

        if (!pendingUser.getOtp().equals(otp)) {
            throw new ResourceNotFoundException("OTP is not valid");
        }

        User user = modelMapper.map(pendingUser, User.class);

        user.setId(null); // important
        user.setEmailVerified(true);
        user.setActive(true);
        user.setRole(Role.valueOf(normalizeRole(pendingUser.getRole())));

        userRepositoty.save(user);

        pendingUserRepository.delete(pendingUser);

        return toDto(user);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return Role.USER.name();
        }

        try {
            return Role.valueOf(role.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("Invalid role. Allowed roles are USER, OWNER, ADMIN");
        }
    }

    private UserDto toDto(User user) {
        UserDto dto = modelMapper.map(user, UserDto.class);
        dto.setPassword(null);
        dto.setRole(user.getRole() == null ? Role.USER.name() : user.getRole().name());
        return dto;
    }
}







