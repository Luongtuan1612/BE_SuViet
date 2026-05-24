package com.suviet.suviet_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Lấy con dấu bí mật từ application.properties
    @Value("${jwt.secret}")
    private String secretKey;

    // Lấy hạn sử dụng của thẻ
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // 1. Tạo thẻ bài (Token) cho người dùng sau khi đăng nhập thành công
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(userDetails.getUsername()) // Lưu tên đăng nhập vào thẻ
                .setIssuedAt(new Date(System.currentTimeMillis())) // Ngày cấp
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Ngày hết hạn
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Đóng dấu bảo mật
                .compact();
    }

    // 2. Trích xuất tên người dùng (Username) từ cái thẻ bài họ gửi lên
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 3. Kiểm tra xem thẻ bài này còn hạn và có phải của người này không
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // --- CÁC HÀM TIỆN ÍCH HỖ TRỢ TRÍCH XUẤT THÔNG TIN BÊN DƯỚI ---

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}