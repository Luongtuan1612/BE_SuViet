package com.suviet.suviet_api.controller;

import com.suviet.suviet_api.dto.ChatFeedbackRequest;
import com.suviet.suviet_api.dto.ChatMessageResponse;
import com.suviet.suviet_api.dto.ChatRequest;
import com.suviet.suviet_api.dto.ChatResponse;
import com.suviet.suviet_api.dto.ChatSessionResponse;
import com.suviet.suviet_api.dto.EditChatMessageRequest;
import com.suviet.suviet_api.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // API gửi câu hỏi đến chatbot AI
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(request);
        return ResponseEntity.ok(response);
    }

    // API lấy danh sách cuộc trò chuyện của user đang đăng nhập
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionResponse>> getCurrentUserSessions() {
        return ResponseEntity.ok(chatService.getCurrentUserSessions());
    }

    // API lấy tin nhắn trong một cuộc trò chuyện
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getMessagesBySession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(chatService.getMessagesBySession(sessionId));
    }

    // API sửa câu hỏi của người dùng và tạo lại câu trả lời AI
    @PutMapping("/messages/{messageId}/edit")
    public ResponseEntity<List<ChatMessageResponse>> editMessageAndRegenerate(
            @PathVariable Long messageId,
            @RequestBody EditChatMessageRequest request
    ) {
        return ResponseEntity.ok(chatService.editUserMessageAndRegenerate(messageId, request));
    }

    // API đánh giá câu trả lời AI
    @PostMapping("/feedback")
    public ResponseEntity<?> submitFeedback(@RequestBody ChatFeedbackRequest request) {
        chatService.submitFeedback(request);
        return ResponseEntity.ok("Đã ghi nhận đánh giá của bạn!");
    }

    // API xóa một cuộc trò chuyện
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<?> deleteSession(@PathVariable Long sessionId) {
        chatService.deleteSession(sessionId);
        return ResponseEntity.ok("Đã xóa cuộc trò chuyện!");
    }
}