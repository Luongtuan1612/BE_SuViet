package com.suviet.suviet_api.controller;

import com.suviet.suviet_api.dto.AdminQuizImportResult;
import com.suviet.suviet_api.dto.AdminQuizQuestionRequest;
import com.suviet.suviet_api.dto.AdminQuizTopicRequest;
import com.suviet.suviet_api.entity.QuizQuestion;
import com.suviet.suviet_api.entity.QuizTopic;
import com.suviet.suviet_api.repository.QuizAttemptRepository;
import com.suviet.suviet_api.repository.QuizQuestionRepository;
import com.suviet.suviet_api.repository.QuizTopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/quizzes")
@CrossOrigin(origins = "*")
public class AdminQuizController {

    @Autowired
    private QuizTopicRepository topicRepository;

    @Autowired
    private QuizQuestionRepository questionRepository;

    @Autowired
    private QuizAttemptRepository attemptRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("topicCount", topicRepository.count());
        stats.put("questionCount", questionRepository.count());
        stats.put("attemptCount", attemptRepository.count());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/topics")
    public ResponseEntity<List<QuizTopic>> getTopics() {
        return ResponseEntity.ok(topicRepository.findAll());
    }

    @PostMapping("/topics")
    public ResponseEntity<?> createTopic(@RequestBody AdminQuizTopicRequest request) {
        validateTopicRequest(request);

        QuizTopic topic = new QuizTopic();
        applyTopicRequest(topic, request);

        return ResponseEntity.ok(topicRepository.save(topic));
    }

    @PutMapping("/topics/{id}")
    public ResponseEntity<?> updateTopic(
            @PathVariable Long id,
            @RequestBody AdminQuizTopicRequest request
    ) {
        validateTopicRequest(request);

        QuizTopic topic = topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chủ đề quiz!"));

        applyTopicRequest(topic, request);

        return ResponseEntity.ok(topicRepository.save(topic));
    }

    @DeleteMapping("/topics/{id}")
    public ResponseEntity<?> deleteTopic(@PathVariable Long id) {
        QuizTopic topic = topicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chủ đề quiz!"));

        long questionCount = questionRepository.countByTopicId(id);
        if (questionCount > 0) {
            return ResponseEntity.badRequest().body(
                    "Không thể xóa chủ đề đang có câu hỏi. Hãy xóa câu hỏi trước."
            );
        }

        long attemptCount = attemptRepository.countByTopicId(id);
        if (attemptCount > 0) {
            return ResponseEntity.badRequest().body(
                    "Không thể xóa chủ đề đã có lịch sử làm bài của học viên."
            );
        }

        topicRepository.delete(topic);
        return ResponseEntity.ok(Map.of("message", "Đã xóa chủ đề quiz thành công!"));
    }

    @GetMapping("/questions")
    public ResponseEntity<List<QuizQuestion>> getQuestions(
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) String difficulty
    ) {
        List<QuizQuestion> questions = questionRepository.findAll()
                .stream()
                .filter(question -> topicId == null ||
                        (question.getTopic() != null && question.getTopic().getId().equals(topicId)))
                .filter(question -> difficulty == null || difficulty.isBlank() ||
                        normalizeDifficulty(difficulty).equals(question.getDifficulty()))
                .toList();

        return ResponseEntity.ok(questions);
    }

    @PostMapping("/questions")
    public ResponseEntity<?> createQuestion(@RequestBody AdminQuizQuestionRequest request) {
        QuizQuestion question = new QuizQuestion();
        applyQuestionRequest(question, request);
        return ResponseEntity.ok(questionRepository.save(question));
    }

    @PutMapping("/questions/{id}")
    public ResponseEntity<?> updateQuestion(
            @PathVariable Long id,
            @RequestBody AdminQuizQuestionRequest request
    ) {
        QuizQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi quiz!"));

        applyQuestionRequest(question, request);
        return ResponseEntity.ok(questionRepository.save(question));
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        QuizQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi quiz!"));

        questionRepository.delete(question);
        return ResponseEntity.ok(Map.of("message", "Đã xóa câu hỏi quiz thành công!"));
    }

    @PostMapping("/questions/import")
    public ResponseEntity<AdminQuizImportResult> importQuestions(@RequestParam("file") MultipartFile file) {
        AdminQuizImportResult result = new AdminQuizImportResult();

        if (file == null || file.isEmpty()) {
            result.setFailedRows(1);
            result.getErrors().add("File import đang trống.");
            return ResponseEntity.badRequest().body(result);
        }

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<String> lines = content.lines()
                    .filter(line -> line != null && !line.trim().isEmpty())
                    .toList();

            if (lines.size() < 2) {
                result.setFailedRows(1);
                result.getErrors().add("File CSV cần có dòng tiêu đề và ít nhất một dòng dữ liệu.");
                return ResponseEntity.badRequest().body(result);
            }

            List<String> headers = parseCsvLine(removeBom(lines.get(0)));
            Map<String, Integer> headerIndex = buildHeaderIndex(headers);
            validateRequiredHeaders(headerIndex);

            for (int lineIndex = 1; lineIndex < lines.size(); lineIndex++) {
                int rowNumber = lineIndex + 1;
                result.setTotalRows(result.getTotalRows() + 1);

                try {
                    List<String> row = parseCsvLine(lines.get(lineIndex));
                    QuizQuestion question = buildQuestionFromCsvRow(headerIndex, row);
                    questionRepository.save(question);
                    result.setSuccessRows(result.getSuccessRows() + 1);
                } catch (Exception rowError) {
                    result.setFailedRows(result.getFailedRows() + 1);
                    result.getErrors().add("Dòng " + rowNumber + ": " + rowError.getMessage());
                }
            }

            return ResponseEntity.ok(result);
        } catch (Exception error) {
            result.setFailedRows(Math.max(1, result.getFailedRows()));
            result.getErrors().add("Không thể import file: " + error.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    private void validateTopicRequest(AdminQuizTopicRequest request) {
        if (request == null || isBlank(request.getTitle())) {
            throw new RuntimeException("Tên chủ đề quiz không được để trống!");
        }
    }

    private void applyTopicRequest(QuizTopic topic, AdminQuizTopicRequest request) {
        topic.setTitle(request.getTitle().trim());
        topic.setDescription(defaultIfBlank(request.getDescription(), "Chủ đề kiểm tra lịch sử Việt Nam."));
        topic.setEmoji(defaultIfBlank(request.getEmoji(), "📚"));
    }

    private void applyQuestionRequest(QuizQuestion question, AdminQuizQuestionRequest request) {
        validateQuestionRequest(request);

        QuizTopic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new RuntimeException("Chủ đề quiz không tồn tại!"));

        question.setTopic(topic);
        question.setDifficulty(normalizeDifficulty(request.getDifficulty()));
        question.setQuestionText(request.getQuestionText().trim());
        question.setOptionA(request.getOptionA().trim());
        question.setOptionB(request.getOptionB().trim());
        question.setOptionC(request.getOptionC().trim());
        question.setOptionD(request.getOptionD().trim());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setExplanation(defaultIfBlank(request.getExplanation(), "Chưa có lời giải thích chi tiết."));
    }

    private void validateQuestionRequest(AdminQuizQuestionRequest request) {
        if (request == null) {
            throw new RuntimeException("Dữ liệu câu hỏi không hợp lệ!");
        }

        if (request.getTopicId() == null) {
            throw new RuntimeException("Vui lòng chọn chủ đề quiz!");
        }

        if (isBlank(request.getQuestionText()) || isBlank(request.getOptionA()) ||
                isBlank(request.getOptionB()) || isBlank(request.getOptionC()) || isBlank(request.getOptionD())) {
            throw new RuntimeException("Câu hỏi và 4 đáp án không được để trống!");
        }

        if (request.getCorrectAnswer() == null || request.getCorrectAnswer() < 0 || request.getCorrectAnswer() > 3) {
            throw new RuntimeException("Đáp án đúng phải nằm trong khoảng 0 đến 3 tương ứng A đến D!");
        }
    }

    private QuizQuestion buildQuestionFromCsvRow(Map<String, Integer> headerIndex, List<String> row) {
        String topicTitle = getRequiredValue(headerIndex, row, "topicTitle");
        String topicDescription = getOptionalValue(headerIndex, row, "topicDescription");
        String topicEmoji = getOptionalValue(headerIndex, row, "topicEmoji");

        QuizTopic topic = topicRepository.findByTitleIgnoreCase(topicTitle.trim())
                .orElseGet(() -> {
                    QuizTopic newTopic = new QuizTopic();
                    newTopic.setTitle(topicTitle.trim());
                    newTopic.setDescription(defaultIfBlank(topicDescription, "Chủ đề kiểm tra lịch sử Việt Nam."));
                    newTopic.setEmoji(defaultIfBlank(topicEmoji, "📚"));
                    return topicRepository.save(newTopic);
                });

        String explanation = getOptionalValue(headerIndex, row, "explanation");
        String sourceTitle = getOptionalValue(headerIndex, row, "sourceTitle");
        String sourceUrl = getOptionalValue(headerIndex, row, "sourceUrl");
        String mergedExplanation = mergeSourceIntoExplanation(explanation, sourceTitle, sourceUrl);

        QuizQuestion question = new QuizQuestion();
        question.setTopic(topic);
        question.setDifficulty(normalizeDifficulty(getOptionalValue(headerIndex, row, "difficulty")));
        question.setQuestionText(getRequiredValue(headerIndex, row, "questionText"));
        question.setOptionA(getRequiredValue(headerIndex, row, "optionA"));
        question.setOptionB(getRequiredValue(headerIndex, row, "optionB"));
        question.setOptionC(getRequiredValue(headerIndex, row, "optionC"));
        question.setOptionD(getRequiredValue(headerIndex, row, "optionD"));
        question.setCorrectAnswer(parseCorrectAnswer(getRequiredValue(headerIndex, row, "correctAnswer")));
        question.setExplanation(defaultIfBlank(mergedExplanation, "Chưa có lời giải thích chi tiết."));

        return question;
    }

    private String mergeSourceIntoExplanation(String explanation, String sourceTitle, String sourceUrl) {
        String base = defaultIfBlank(explanation, "");
        String title = defaultIfBlank(sourceTitle, "");
        String url = defaultIfBlank(sourceUrl, "");

        if (title.isBlank() && url.isBlank()) {
            return base;
        }

        String sourceText;
        if (!title.isBlank() && !url.isBlank()) {
            sourceText = "Nguồn tham khảo: " + title + " - " + url;
        } else if (!title.isBlank()) {
            sourceText = "Nguồn tham khảo: " + title;
        } else {
            sourceText = "Nguồn tham khảo: " + url;
        }

        return base.isBlank() ? sourceText : base + "\n" + sourceText;
    }

    private void validateRequiredHeaders(Map<String, Integer> headerIndex) {
        List<String> requiredHeaders = List.of(
                "topicTitle", "questionText", "optionA", "optionB", "optionC", "optionD", "correctAnswer"
        );

        List<String> missingHeaders = requiredHeaders.stream()
                .filter(header -> !headerIndex.containsKey(header.toLowerCase()))
                .toList();

        if (!missingHeaders.isEmpty()) {
            throw new RuntimeException("Thiếu cột bắt buộc: " + String.join(", ", missingHeaders));
        }
    }

    private Map<String, Integer> buildHeaderIndex(List<String> headers) {
        Map<String, Integer> headerIndex = new HashMap<>();

        for (int index = 0; index < headers.size(); index++) {
            headerIndex.put(headers.get(index).trim().toLowerCase(), index);
        }

        return headerIndex;
    }

    private String getRequiredValue(Map<String, Integer> headerIndex, List<String> row, String header) {
        String value = getOptionalValue(headerIndex, row, header);

        if (isBlank(value)) {
            throw new RuntimeException("Cột " + header + " không được để trống.");
        }

        return value.trim();
    }

    private String getOptionalValue(Map<String, Integer> headerIndex, List<String> row, String header) {
        Integer index = headerIndex.get(header.toLowerCase());

        if (index == null || index >= row.size()) {
            return "";
        }

        return row.get(index).trim();
    }

    private Integer parseCorrectAnswer(String value) {
        String normalized = value.trim().toUpperCase();

        return switch (normalized) {
            case "A", "0" -> 0;
            case "B", "1" -> 1;
            case "C", "2" -> 2;
            case "D", "3" -> 3;
            default -> throw new RuntimeException("Đáp án đúng phải là A, B, C, D hoặc 0, 1, 2, 3.");
        };
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

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);

            if (character == '"') {
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (character == ',' && !insideQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }

        values.add(current.toString());
        return values;
    }

    private String removeBom(String text) {
        if (text != null && text.startsWith("\uFEFF")) {
            return text.substring(1);
        }

        return text;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value.trim();
    }
}
