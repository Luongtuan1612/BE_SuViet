package com.suviet.suviet_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username; // Tên đăng nhập (phải là duy nhất)

    @Column(nullable = false, length = 255)
    private String password; // Mật khẩu (sau này sẽ dùng BCrypt để mã hóa, không lưu mật khẩu thô)

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "full_name", length = 100)
    private String fullName; // Tên hiển thị trên màn hình

    @Column(nullable = false, length = 20)
    private String role; // Quyền của User: "ROLE_USER" hoặc "ROLE_ADMIN"

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Ngày tạo tài khoản

    // Hàm này tự động chạy trước khi lưu User mới vào DB để set ngày giờ hiện tại
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (role == null) {
            role = "ROLE_USER"; // Mặc định ai đăng ký cũng là USER bình thường
        }
    }
}