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

import java.util.List;

@RestController
@RequestMapping("/api/v1/quizzes")
@CrossOrigin(origins = "*") // Cực kỳ quan trọng: Cho phép ReactJS gọi API không bị lỗi CORS
public class QuizController {

    @Autowired
    private QuizTopicRepository topicRepository;

    @Autowired
    private QuizQuestionRepository questionRepository;

    // THÊM 2 CẦU NỐI DATABASE MỚI ĐỂ LƯU ĐIỂM
    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private UserRepository userRepository;

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

    // 3. API Nộp bài và lưu điểm số (Bắt buộc phải đăng nhập có Token)
    @PostMapping("/submit")
    public ResponseEntity<?> submitQuiz(@RequestBody QuizSubmitRequest request) {
        // Lấy thông tin người dùng đang đăng nhập từ hệ thống bảo mật (Spring Security Context)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String currentUsername = authentication.getName(); // Rút tên Username từ Token ra

        // Tìm User trong Database
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng hiện tại!"));

        // Tìm Chủ đề trắc nghiệm
        QuizTopic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new RuntimeException("Chủ đề trắc nghiệm không tồn tại!"));

        // Tạo đối tượng lịch sử làm bài và tiến hành lưu
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setTopic(topic);
        attempt.setScore(request.getScore());
        attempt.setTotalQuestions(request.getTotalQuestions());

        quizAttemptRepository.save(attempt); // Ghi điểm số xuống MySQL!

        return ResponseEntity.ok("Đã lưu kết quả bài kiểm tra thành công!");
    }
    // 4. API Lấy lịch sử làm bài của người dùng đang đăng nhập (Bắt buộc có Token)
    @GetMapping("/history")
    public ResponseEntity<?> getUserHistory() {
        // Lấy tên người dùng từ Token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Tìm User trong DB
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng hiện tại!"));

        // Lấy danh sách các bài thi đã làm (Sắp xếp theo thời gian mới nhất lên đầu)
        List<QuizAttempt> attempts = quizAttemptRepository.findByUserIdOrderByCompletedAtDesc(user.getId());

        // Để tránh trả về mật khẩu của User ra ngoài, chúng ta chỉ map các thông tin cần thiết
        List<java.util.Map<String, Object>> response = attempts.stream().map(attempt -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", attempt.getId());
            map.put("score", attempt.getScore());
            map.put("totalQuestions", attempt.getTotalQuestions());
            map.put("completedAt", attempt.getCompletedAt());
            map.put("topicTitle", attempt.getTopic().getTitle());
            map.put("topicEmoji", attempt.getTopic().getEmoji());
            return map;
        }).toList();

        return ResponseEntity.ok(response);
    }
}