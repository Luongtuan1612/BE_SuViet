package com.suviet.suviet_api.controller;

import com.suviet.suviet_api.entity.QuizQuestion;
import com.suviet.suviet_api.entity.QuizTopic;
import com.suviet.suviet_api.repository.QuizQuestionRepository;
import com.suviet.suviet_api.repository.QuizTopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quizzes")
@CrossOrigin(origins = "*") // Cực kỳ quan trọng: Cho phép ReactJS gọi API không bị lỗi CORS
public class QuizController {

    @Autowired
    private QuizTopicRepository topicRepository;

    @Autowired
    private QuizQuestionRepository questionRepository;

    // 1. Lấy toàn bộ danh sách các chủ đề Trắc nghiệm
    @GetMapping("/topics")
    public ResponseEntity<List<QuizTopic>> getAllTopics() {
        List<QuizTopic> topics = topicRepository.findAll();
        return ResponseEntity.ok(topics);
    }

    // 2. Lấy danh sách câu hỏi của một chủ đề cụ thể (dựa vào ID chủ đề)
    @GetMapping("/topics/{topicId}/questions")
    public ResponseEntity<List<QuizQuestion>> getQuestionsByTopic(@PathVariable Long topicId) {
        List<QuizQuestion> questions = questionRepository.findByTopicId(topicId);
        return ResponseEntity.ok(questions);
    }
}