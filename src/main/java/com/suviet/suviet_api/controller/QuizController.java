package com.suviet.suviet_api.controller;

import com.suviet.suviet_api.dto.QuizSubmitRequest;
import com.suviet.suviet_api.entity.QuizAttempt;
import com.suviet.suviet_api.entity.QuizQuestion;
import com.suviet.suviet_api.entity.QuizTopic;
import com.suviet.suviet_api.entity.User;
import com.suviet.suviet_api.repository.QuizAttemptRepository;
import com.suviet.suviet_api.repository.QuizQuestionRepository;
import com.suviet.suviet_api.repository.QuizTopicRepository;
import com.suviet.suviet_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quizzes")
@CrossOrigin(origins = "*")
public class QuizController {

    @Autowired
    private QuizTopicRepository topicRepository;

    @Autowired
    private QuizQuestionRepository questionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. Lấy toàn bộ chủ đề quiz
    @GetMapping("/topics")
    public ResponseEntity<List<QuizTopic>> getAllTopics() {
        List<QuizTopic> topics = topicRepository.findAll();
        return ResponseEntity.ok(topics);
    }

    // 2. Lấy câu hỏi theo chủ đề và độ khó
    // Ví dụ:
    // /api/v1/quizzes/topics/1/questions?difficulty=EASY
    // /api/v1/quizzes/topics/1/questions?difficulty=MEDIUM
    // /api/v1/quizzes/topics/1/questions?difficulty=HARD
    @GetMapping("/topics/{topicId}/questions")
    public ResponseEntity<List<QuizQuestion>> getQuestionsByTopic(
            @PathVariable Long topicId,
            @RequestParam(required = false) String difficulty
    ) {
        List<QuizQuestion> questions;

        if (difficulty == null || difficulty.isBlank()) {
            questions = questionRepository.findByTopicIdOrderByIdAsc(topicId);
        } else {
            String normalizedDifficulty = normalizeDifficulty(difficulty);
            questions = questionRepository.findByTopicIdAndDifficultyOrderByIdAsc(
                    topicId,
                    normalizedDifficulty
            );
        }

        return ResponseEntity.ok(questions);
    }

    // 3. Nộp bài quiz và lưu lịch sử
    @PostMapping("/submit")
    public ResponseEntity<?> submitQuiz(@RequestBody QuizSubmitRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String currentUsername = authentication.getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng hiện tại!"));

        QuizTopic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new RuntimeException("Chủ đề trắc nghiệm không tồn tại!"));

        String difficulty = normalizeDifficulty(request.getDifficulty());

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setTopic(topic);
        attempt.setDifficulty(difficulty);
        attempt.setScore(request.getScore());
        attempt.setTotalQuestions(request.getTotalQuestions());

        quizAttemptRepository.save(attempt);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đã lưu kết quả bài kiểm tra thành công!");
        response.put("topicId", topic.getId());
        response.put("topicTitle", topic.getTitle());
        response.put("difficulty", difficulty);
        response.put("score", attempt.getScore());
        response.put("totalQuestions", attempt.getTotalQuestions());
        response.put("completedAt", attempt.getCompletedAt());

        return ResponseEntity.ok(response);
    }

    // 4. Lấy lịch sử làm bài của user đang đăng nhập
    @GetMapping("/history")
    public ResponseEntity<?> getUserHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng hiện tại!"));

        List<QuizAttempt> attempts =
                quizAttemptRepository.findByUserIdOrderByCompletedAtDesc(user.getId());

        List<Map<String, Object>> response = attempts.stream().map(attempt -> {
            Map<String, Object> map = new HashMap<>();

            map.put("id", attempt.getId());
            map.put("score", attempt.getScore());
            map.put("totalQuestions", attempt.getTotalQuestions());
            map.put("difficulty", attempt.getDifficulty());
            map.put("completedAt", attempt.getCompletedAt());

            if (attempt.getTopic() != null) {
                map.put("topicId", attempt.getTopic().getId());
                map.put("topicTitle", attempt.getTopic().getTitle());
                map.put("topicEmoji", attempt.getTopic().getEmoji());
            }

            return map;
        }).toList();

        return ResponseEntity.ok(response);
    }

    private String normalizeDifficulty(String difficulty) {
        if (difficulty == null || difficulty.isBlank()) {
            return "EASY";
        }

        String value = difficulty.trim().toUpperCase();

        return switch (value) {
            case "EASY", "MEDIUM", "HARD" -> value;
            default -> "EASY";
        };
    }
}