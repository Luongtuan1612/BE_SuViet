package com.suviet.suviet_api.controller;

import com.suviet.suviet_api.dto.AuthResponse;
import com.suviet.suviet_api.dto.LoginRequest;
import com.suviet.suviet_api.dto.RegisterRequest;
import com.suviet.suviet_api.entity.User;
import com.suviet.suviet_api.repository.UserRepository;
import com.suviet.suviet_api.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    // 1. API Đăng ký tài khoản
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Kiểm tra xem tên đăng nhập đã có ai dùng chưa
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Tên đăng nhập đã tồn tại, vui lòng chọn tên khác!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        // Mã hóa mật khẩu trước khi lưu vào Database
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole("USER"); // Mặc định cấp quyền người dùng bình thường

        userRepository.save(user); // Lưu vào MySQL

        return ResponseEntity.ok("Đăng ký tài khoản thành công!");
    }

    // 2. API Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        // In Thẻ bài (Token)
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole()));
    }
}