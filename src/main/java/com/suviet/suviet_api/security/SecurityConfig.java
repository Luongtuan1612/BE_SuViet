package com.suviet.suviet_api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    // 1. Cấu hình BỘ LUẬT CHÍNH: Ai được vào đâu?
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt tính năng chặn form (vì chúng ta dùng ReactJS và Token)
                .cors(cors -> cors.configure(http)) // Bật CORS

                // QUY ĐỊNH CÁC ĐƯỜNG LINK (Đã sửa requestPathMatchers thành requestMatchers)
                .authorizeHttpRequests(auth -> auth
                        // CÁC ĐƯỜNG LINK MỞ (Ai cũng vào được)
                        .requestMatchers("/api/v1/auth/**").permitAll() // Cho phép Đăng ký / Đăng nhập
                        .requestMatchers("/api/v1/history/articles/**").permitAll() // Cho phép xem Sự kiện
                        .requestMatchers("/api/v1/periods/**").permitAll() // Cho phép xem Triều đại
                        .requestMatchers("/api/v1/history/figures/**").permitAll() // Cho phép xem Nhân vật
                        .requestMatchers("/api/v1/quizzes/**").permitAll() // Cho phép lấy câu hỏi Trắc nghiệm
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/quizzes/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/quizzes/submit").authenticated()//Chỉ cho phép XEM tự do (GET), còn nộp bài (POST) thì phải khóa lại
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/quizzes/history").authenticated()
                        // CÁC ĐƯỜNG LINK CÒN LẠI BẮT BUỘC ĐĂNG NHẬP
                        .anyRequest().authenticated()
                )

                // Khai báo: Không lưu phiên đăng nhập (Session) trên Server
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Kết nối Hệ thống xác thực
                .authenticationProvider(authenticationProvider())

                // Xếp Bác bảo vệ (JwtFilter) đứng TRƯỚC cửa chính
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
        // Đưa thẳng userDetailsService vào trong ngoặc tròn lúc khởi tạo
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);

        // (DÒNG 71 CŨ ĐÃ BỊ XÓA BỎ HOÀN TOÀN)

        // Cài đặt PasswordEncoder bình thường (Dòng này phần mềm của bạn không báo lỗi)
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    // 4. Công cụ hỗ trợ Đăng nhập (Đã import đầy đủ thư viện)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}