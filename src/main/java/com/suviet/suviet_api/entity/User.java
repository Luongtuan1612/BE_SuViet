package com.suviet.suviet_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(length = 20)
    private String role; // Ví dụ: "USER" hoặc "ADMIN"

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- CÁC HÀM BẮT BUỘC CỦA SPRING SECURITY ---

    // 1. Trả về quyền hạn (Role) của người dùng
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    // 2. Trả về mật khẩu
    @Override
    public String getPassword() {
        return password;
    }

    // 3. Trả về tên đăng nhập
    @Override
    public String getUsername() {
        return username;
    }

    // Các hàm kiểm tra tài khoản có bị khóa/hết hạn không (Mặc định cứ cho true hết)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}