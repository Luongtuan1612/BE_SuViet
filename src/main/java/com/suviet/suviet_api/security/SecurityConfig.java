package com.suviet.suviet_api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    // 1. Cấu hình bộ luật chính: Ai được vào đâu?
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF vì backend dùng REST API + JWT token
                .csrf(csrf -> csrf.disable())

                // Bật CORS để ReactJS gọi API không bị chặn
                .cors(cors -> cors.configure(http))

                // Quy định quyền truy cập cho từng API
                .authorizeHttpRequests(auth -> auth

                        // =========================
                        // API PUBLIC - Ai cũng gọi được
                        // =========================

                        // Đăng ký / đăng nhập
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Chatbot AI yêu cầu đăng nhập
                        .requestMatchers("/api/v1/chat/**").authenticated()

                        // Lịch sử: bài viết, thời kỳ, nhân vật
                        .requestMatchers("/api/v1/history/articles/**").permitAll()
                        .requestMatchers("/api/v1/history/periods/**").permitAll()
                        .requestMatchers("/api/v1/periods/**").permitAll()
                        .requestMatchers("/api/v1/history/figures/**").permitAll()

                        // Quiz: cho phép GET danh sách chủ đề/câu hỏi
                        .requestMatchers(HttpMethod.GET, "/api/v1/quizzes/**").permitAll()

                        // =========================
                        // API CẦN ĐĂNG NHẬP
                        // =========================

                        // Nộp bài quiz cần đăng nhập
                        .requestMatchers(HttpMethod.POST, "/api/v1/quizzes/submit").authenticated()

                        // Xem lịch sử làm bài cần đăng nhập
                        .requestMatchers(HttpMethod.GET, "/api/v1/quizzes/history").authenticated()

                        // Các API còn lại bắt buộc đăng nhập
                        .anyRequest().authenticated()
                )

                // Không lưu session trên server vì dùng JWT
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Khai báo AuthenticationProvider
                .authenticationProvider(authenticationProvider())

                // Đặt JWT filter trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 2. Công cụ mã hóa mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 3. Hệ thống xác thực
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    // 4. Công cụ hỗ trợ đăng nhập
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}