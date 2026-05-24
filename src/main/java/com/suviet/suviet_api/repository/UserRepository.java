package com.suviet.suviet_api.repository;

import com.suviet.suviet_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Hàm cực kỳ quan trọng: Tìm người dùng theo tên đăng nhập
    Optional<User> findByUsername(String username);
}