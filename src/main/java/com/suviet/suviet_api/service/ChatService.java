package com.suviet.suviet_api.service;

import com.suviet.suviet_api.dto.*;
import com.suviet.suviet_api.entity.ChatFeedback;
import com.suviet.suviet_api.entity.ChatMessage;
import com.suviet.suviet_api.entity.ChatSession;
import com.suviet.suviet_api.entity.User;
import com.suviet.suviet_api.repository.ChatFeedbackRepository;
import com.suviet.suviet_api.repository.ChatMessageRepository;
import com.suviet.suviet_api.repository.ChatSessionRepository;
import com.suviet.suviet_api.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatFeedbackRepository chatFeedbackRepository;
    private final UserRepository userRepository;
    private final AiServiceClient aiServiceClient;

    public ChatService(
            ChatSessionRepository chatSessionRepository,
            ChatMessageRepository chatMessageRepository,
            ChatFeedbackRepository chatFeedbackRepository,
            UserRepository userRepository,
            AiServiceClient aiServiceClient
    ) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatFeedbackRepository = chatFeedbackRepository;
        this.userRepository = userRepository;
        this.aiServiceClient = aiServiceClient;
    }

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            throw new RuntimeException("Câu hỏi không được để trống!");
        }

        User currentUser = getCurrentUser();

        ChatSession session = getOrCreateSession(request, currentUser);

        String question = request.getQuestion().trim();

        saveMessage(session, "USER", question, null);

        AiChatResponse aiResponse = aiServiceClient.ask(question);

        if (aiResponse == null) {
            String errorMessage = "Không nhận được phản hồi từ AI Service.";

            ChatMessage aiErrorMessage = saveMessage(session, "AI", errorMessage, "[]");

            session.setUpdatedAt(LocalDateTime.now());
            chatSessionRepository.save(session);

            return new ChatResponse(
                    session.getId(),
                    aiErrorMessage.getId(),
                    errorMessage,
                    Collections.emptyList()
            );
        }

        String sourcesJson = convertSourcesToJson(aiResponse.getSources());

        ChatMessage aiMessage = saveMessage(session, "AI", aiResponse.getAnswer(), sourcesJson);

        session.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        return new ChatResponse(
                session.getId(),
                aiMessage.getId(),
                aiResponse.getAnswer(),
                aiResponse.getSources()
        );
    }

    public List<ChatSessionResponse> getCurrentUserSessions() {
        User currentUser = getCurrentUser();

        return chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(currentUser.getId())
                .stream()
                .map(session -> new ChatSessionResponse(
                        session.getId(),
                        session.getTitle(),
                        session.getCreatedAt(),
                        session.getUpdatedAt()
                ))
                .toList();
    }

    public List<ChatMessageResponse> getMessagesBySession(Long sessionId) {
        User currentUser = getCurrentUser();

        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện!"));

        if (session.getUser() == null || !session.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền xem cuộc trò chuyện này!");
        }

        return buildMessageResponses(sessionId);
    }

    @Transactional
    public List<ChatMessageResponse> editUserMessageAndRegenerate(
            Long messageId,
            EditChatMessageRequest request
    ) {
        String newQuestion = extractEditedQuestion(request);

        User currentUser = getCurrentUser();

        ChatMessage userMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin nhắn cần sửa!"));

        if (!"USER".equalsIgnoreCase(userMessage.getSender())) {
            throw new RuntimeException("Chỉ được sửa câu hỏi của người dùng!");
        }

        ChatSession session = userMessage.getSession();

        if (session.getUser() == null || !session.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền sửa tin nhắn này!");
        }

        Long sessionId = session.getId();

        boolean isFirstMessage = isFirstMessageInSession(sessionId, userMessage.getId());

        /*
         * Lấy toàn bộ tin nhắn sau câu hỏi được sửa.
         * Khi sửa một câu hỏi, các câu trả lời và câu hỏi phía sau không còn chắc đúng theo nhánh mới.
         */
        List<ChatMessage> messagesToDelete =
                chatMessageRepository.findBySessionIdAndIdGreaterThanOrderByIdAsc(
                        sessionId,
                        userMessage.getId()
                );

        List<Long> messageIdsToDelete = messagesToDelete
                .stream()
                .map(ChatMessage::getId)
                .toList();

        /*
         * Xóa feedback trước để tránh lỗi khóa ngoại.
         */
        if (!messageIdsToDelete.isEmpty()) {
            chatFeedbackRepository.deleteByMessageIds(messageIdsToDelete);
            chatMessageRepository.deleteBySessionIdAndIdGreaterThan(sessionId, userMessage.getId());
        }

        /*
         * Cập nhật lại câu hỏi của người dùng.
         */
        userMessage.setMessage(newQuestion);
        userMessage.setSources(null);
        chatMessageRepository.save(userMessage);

        /*
         * Gọi lại AI Service để tạo câu trả lời mới.
         */
        AiChatResponse aiResponse = null;

        try {
            aiResponse = aiServiceClient.ask(newQuestion);
        } catch (Exception e) {
            // Không ném lỗi ra ngoài để tránh rollback việc sửa câu hỏi.
            aiResponse = null;
        }

        String answer;
        String sourcesJson;
        List<SourceDto> sources;

        if (aiResponse == null) {
            answer = "Không nhận được phản hồi từ AI Service.";
            sources = Collections.emptyList();
            sourcesJson = "[]";
        } else {
            answer = aiResponse.getAnswer();
            sources = aiResponse.getSources() == null
                    ? Collections.emptyList()
                    : aiResponse.getSources();
            sourcesJson = convertSourcesToJson(sources);
        }

        saveMessage(session, "AI", answer, sourcesJson);

        /*
         * Nếu sửa câu hỏi đầu tiên thì cập nhật lại tiêu đề cuộc trò chuyện.
         */
        if (isFirstMessage) {
            session.setTitle(buildSessionTitle(newQuestion));
        }

        session.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        return buildMessageResponses(sessionId);
    }

    @Transactional
    public void submitFeedback(ChatFeedbackRequest request) {
        User currentUser = getCurrentUser();

        if (request.getMessageId() == null) {
            throw new RuntimeException("Thiếu ID tin nhắn cần đánh giá!");
        }

        if (request.getRating() == null || request.getRating().isBlank()) {
            throw new RuntimeException("Thiếu loại đánh giá!");
        }

        String rating = request.getRating().toUpperCase();

        if (!rating.equals("LIKE") && !rating.equals("DISLIKE")) {
            throw new RuntimeException("Đánh giá chỉ được là LIKE hoặc DISLIKE!");
        }

        ChatMessage message = chatMessageRepository.findById(request.getMessageId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin nhắn cần đánh giá!"));

        if (!"AI".equalsIgnoreCase(message.getSender())) {
            throw new RuntimeException("Chỉ có thể đánh giá câu trả lời của AI!");
        }

        ChatSession session = message.getSession();

        if (session.getUser() == null || !session.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền đánh giá tin nhắn này!");
        }

        ChatFeedback feedback = chatFeedbackRepository
                .findByUserIdAndMessageId(currentUser.getId(), message.getId())
                .orElse(new ChatFeedback());

        feedback.setUser(currentUser);
        feedback.setMessage(message);
        feedback.setRating(rating);
        feedback.setComment(request.getComment());

        chatFeedbackRepository.save(feedback);
    }

    @Transactional
    public void deleteSession(Long sessionId) {
        User currentUser = getCurrentUser();

        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện!"));

        if (session.getUser() == null || !session.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa cuộc trò chuyện này!");
        }

        chatFeedbackRepository.deleteBySessionId(sessionId);
        chatMessageRepository.deleteBySessionId(sessionId);
        chatSessionRepository.delete(session);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Bạn cần đăng nhập để sử dụng chatbot AI!");
        }

        String currentUsername = authentication.getName();

        if ("anonymousUser".equals(currentUsername)) {
            throw new RuntimeException("Bạn cần đăng nhập để sử dụng chatbot AI!");
        }

        return userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng hiện tại!"));
    }

    private ChatSession getOrCreateSession(ChatRequest request, User user) {
        if (request.getSessionId() != null) {
            ChatSession session = chatSessionRepository.findById(request.getSessionId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện!"));

            if (session.getUser() == null || !session.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Bạn không có quyền sử dụng cuộc trò chuyện này!");
            }

            return session;
        }

        return createNewSession(request.getQuestion(), user);
    }

    private ChatSession createNewSession(String question, User user) {
        ChatSession session = new ChatSession();

        session.setUser(user);
        session.setTitle(buildSessionTitle(question));

        return chatSessionRepository.save(session);
    }

    private ChatMessage saveMessage(ChatSession session, String sender, String message, String sources) {
        ChatMessage chatMessage = new ChatMessage();

        chatMessage.setSession(session);
        chatMessage.setSender(sender);
        chatMessage.setMessage(message);
        chatMessage.setSources(sources);

        return chatMessageRepository.save(chatMessage);
    }

    private String extractEditedQuestion(EditChatMessageRequest request) {
        if (request == null) {
            throw new RuntimeException("Thiếu nội dung câu hỏi cần sửa!");
        }

        String value = request.getContent();

        if (value == null || value.trim().isEmpty()) {
            value = request.getQuestion();
        }

        if (value == null || value.trim().isEmpty()) {
            value = request.getMessage();
        }

        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Nội dung câu hỏi không được để trống!");
        }

        return value.trim();
    }

    private boolean isFirstMessageInSession(Long sessionId, Long messageId) {
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByIdAsc(sessionId);

        if (messages.isEmpty()) {
            return false;
        }

        return messages.get(0).getId().equals(messageId);
    }

    private String buildSessionTitle(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "Cuộc trò chuyện mới";
        }

        String trimmed = question.trim();

        return trimmed.length() > 50
                ? trimmed.substring(0, 50) + "..."
                : trimmed;
    }

    private List<ChatMessageResponse> buildMessageResponses(Long sessionId) {
        return chatMessageRepository.findBySessionIdOrderByIdAsc(sessionId)
                .stream()
                .map(this::toChatMessageResponse)
                .toList();
    }

    private ChatMessageResponse toChatMessageResponse(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getSender(),
                message.getMessage(),
                message.getSources(),
                message.getCreatedAt()
        );
    }

    private String convertSourcesToJson(List<SourceDto> sources) {
        if (sources == null || sources.isEmpty()) {
            return "[]";
        }

        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < sources.size(); i++) {
            SourceDto source = sources.get(i);

            json.append("{");
            json.append("\"title\":\"").append(escapeJson(source.getTitle())).append("\",");
            json.append("\"source\":\"").append(escapeJson(source.getSource())).append("\",");
            json.append("\"period\":\"").append(escapeJson(source.getPeriod())).append("\",");
            json.append("\"url\":\"").append(escapeJson(source.getUrl())).append("\",");
            json.append("\"file_name\":\"").append(escapeJson(source.getFileName())).append("\",");

            if (source.getChunkIndex() == null) {
                json.append("\"chunk_index\":null");
            } else {
                json.append("\"chunk_index\":").append(source.getChunkIndex());
            }

            json.append("}");

            if (i < sources.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");

        return json.toString();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}