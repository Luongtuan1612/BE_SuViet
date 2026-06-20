package com.suviet.suviet_api.repository;

import com.suviet.suviet_api.entity.AiSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiSourceRepository extends JpaRepository<AiSource, Long> {

    List<AiSource> findAllByOrderByIdDesc();

    boolean existsByUrl(String url);
}