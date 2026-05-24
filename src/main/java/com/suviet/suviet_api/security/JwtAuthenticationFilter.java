package com.suviet.suviet_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService; // Hồ sơ người dùng (Chúng ta sẽ làm ở bước sau)

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Khách đến cửa, bác bảo vệ yêu cầu xem Header "Authorization"
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 2. Nếu không có Header, hoặc Header không bắt đầu bằng chữ "Bearer " -> Cho đi tiếp (Luật của Spring sẽ tự chặn lại nếu vùng đó cấm)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Tách lấy vé thật (Cắt bỏ 7 ký tự "Bearer " ở đầu)
        jwt = authHeader.substring(7);

        // 4. Nhờ JwtService soi vé để lấy tên người dùng
        username = jwtService.extractUsername(jwt);

        // 5. Nếu có tên người dùng, và người này chưa được cấp thẻ đi lại trong hệ thống
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Lục hồ sơ người dùng từ Database lên
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 6. Soi xem vé có hợp lệ với người này không
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // Vé xịn -> Cấp cho cái thẻ thông hành (AuthenticationToken)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Lưu thẻ thông hành vào ngữ cảnh (Cho phép đi lại thoải mái trong API)
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Mời đi tiếp vào bên trong
        filterChain.doFilter(request, response);
    }
}