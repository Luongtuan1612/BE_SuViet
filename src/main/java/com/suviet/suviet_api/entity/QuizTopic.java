package com.suviet.suviet_api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "quiz_topics")
@Data
public class QuizTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 255)
    private String description;

    @Column(length = 10)
    private String emoji;
}