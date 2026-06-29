package com.suviet.suviet_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "quiz_questions")
@Data
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_id", nullable = false)
    @JsonIgnoreProperties("questions")
    private QuizTopic topic;

    @Column(name = "difficulty", nullable = false, length = 20)
    private String difficulty = "EASY"; // EASY, MEDIUM, HARD

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "optiona", nullable = false, columnDefinition = "TEXT")
    private String optionA;

    @Column(name = "optionb", nullable = false, columnDefinition = "TEXT")
    private String optionB;

    @Column(name = "optionc", nullable = false, columnDefinition = "TEXT")
    private String optionC;

    @Column(name = "optiond", nullable = false, columnDefinition = "TEXT")
    private String optionD;

    // 0 = A, 1 = B, 2 = C, 3 = D
    @Column(name = "correct_answer", nullable = false)
    private Integer correctAnswer;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;
}