package com.suviet.suviet_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;     // Thẻ bài JWT
    private String username;  // Tên hiển thị trên góc màn hình
    private String role;      // Quyền (Ví dụ: USER, ADMIN)
}