package com.suviet.suviet_api.controller;

import com.suviet.suviet_api.dto.AdminAiSourceRequest;
import com.suviet.suviet_api.dto.AiFetchUrlRequest;
import com.suviet.suviet_api.dto.AiFetchUrlResponse;
import com.suviet.suviet_api.dto.AiIngestFileRequest;
import com.suviet.suviet_api.dto.AiIngestFileResponse;
import com.suviet.suviet_api.dto.AiDeleteFileRequest;
import com.suviet.suviet_api.dto.AiDeleteFileResponse;
import com.suviet.suviet_api.entity.AiSource;
import com.suviet.suviet_api.entity.HistoricalPeriod;
import com.suviet.suviet_api.entity.User;
import com.suviet.suviet_api.repository.AiSourceRepository;
import com.suviet.suviet_api.repository.HistoricalPeriodRepository;
import com.suviet.suviet_api.repository.UserRepository;
import com.suviet.suviet_api.service.AiServiceClient;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/ai-sources")
@CrossOrigin(origins = "*")
public class AdminAiSourceController {

    private final AiSourceRepository aiSourceRepository;
    private final AiServiceClient aiServiceClient;
    private final UserRepository userRepository;
    private final HistoricalPeriodRepository historicalPeriodRepository;

    public AdminAiSourceController(
            AiSourceRepository aiSourceRepository,
            AiServiceClient aiServiceClient,
            UserRepository userRepository,
            HistoricalPeriodRepository historicalPeriodRepository
    ) {
        this.aiSourceRepository = aiSourceRepository;
        this.aiServiceClient = aiServiceClient;
        this.userRepository = userRepository;
        this.historicalPeriodRepository = historicalPeriodRepository;
    }

    @GetMapping
    public List<AiSource> getAllSources() {
        return aiSourceRepository.findAllByOrderByIdDesc();
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("totalSources", aiSourceRepository.count());
        stats.put("message", "Thống kê nguồn tri thức AI");

        return stats;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AiSource createSource(@RequestBody AdminAiSourceRequest request) {
        validateCreateRequest(request);

        String url = cleanRequired(request.getUrl());

        if (aiSourceRepository.existsByUrl(url)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Link nguồn này đã tồn tại trong hệ thống."
            );
        }

        String periodName = clean(request.getPeriod());
        Long periodId = request.getPeriodId();

        if (periodId != null) {
            HistoricalPeriod period = historicalPeriodRepository.findById(periodId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Giai đoạn lịch sử không tồn tại."
                    ));

            // Nếu frontend chỉ gửi periodId mà chưa gửi tên giai đoạn,
            // backend tự lấy tên giai đoạn để giữ metadata gửi sang AI Service.
            if (isBlank(periodName)) {
                periodName = period.getName();
            }
        }

        AiSource source = new AiSource();

        source.setTitle(cleanRequired(request.getTitle()));
        source.setUrl(url);
        source.setPeriod(periodName);
        source.setPeriodId(periodId);
        source.setCategory(clean(request.getCategory()));
        source.setCreatedByUserId(getCurrentUserId());
        source.setStatus("PENDING");
        source.setChunksAdded(0);
        source.setTotalChunks(0);
        source.setContentLength(0);

        return aiSourceRepository.save(source);
    }

    @PostMapping("/{id}/fetch")
    public AiSource fetchSource(@PathVariable Long id) {
        AiSource source = getSourceById(id);

        source.setStatus("PROCESSING");
        source.setErrorMessage(null);
        aiSourceRepository.save(source);

        try {
            AiFetchUrlRequest aiRequest = new AiFetchUrlRequest(
                    source.getTitle(),
                    source.getUrl(),
                    source.getPeriod(),
                    source.getCategory()
            );

            AiFetchUrlResponse aiResponse = aiServiceClient.fetchUrl(aiRequest);

            if (aiResponse == null || !Boolean.TRUE.equals(aiResponse.getSuccess())) {
                source.setStatus("FAILED");
                source.setErrorMessage("AI Service không trả về kết quả lấy nội dung thành công.");
                return aiSourceRepository.save(source);
            }

            source.setStatus("FETCHED");
            source.setLocalFilePath(aiResponse.getFilePath());
            source.setContentPreview(aiResponse.getContentPreview());
            source.setContentLength(aiResponse.getContentLength());
            source.setErrorMessage(null);

            return aiSourceRepository.save(source);
        } catch (Exception ex) {
            source.setStatus("FAILED");
            source.setErrorMessage(ex.getMessage());

            return aiSourceRepository.save(source);
        }
    }

    @PostMapping("/{id}/ingest")
    public AiSource ingestSource(@PathVariable Long id) {
        AiSource source = getSourceById(id);

        if (isBlank(source.getLocalFilePath())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nguồn này chưa có file TXT. Hãy bấm 'Lấy nội dung' trước."
            );
        }

        source.setStatus("PROCESSING");
        source.setErrorMessage(null);
        aiSourceRepository.save(source);

        try {
            System.out.println("\n========== SPRING BOOT INGEST SOURCE ==========");
            System.out.println("Source ID: " + source.getId());
            System.out.println("Title: " + source.getTitle());
            System.out.println("URL: " + source.getUrl());
            System.out.println("Local file path: " + source.getLocalFilePath());

            AiIngestFileRequest aiRequest = new AiIngestFileRequest(
                    source.getLocalFilePath()
            );

            AiIngestFileResponse aiResponse = aiServiceClient.ingestFile(aiRequest);

            if (aiResponse == null || !Boolean.TRUE.equals(aiResponse.getSuccess())) {
                source.setStatus("FAILED");
                source.setErrorMessage("AI Service không trả về kết quả nạp dữ liệu thành công.");
                return aiSourceRepository.save(source);
            }

            Integer chunksAdded = aiResponse.getChunksAdded() == null ? 0 : aiResponse.getChunksAdded();
            Integer totalChunks = aiResponse.getTotalChunks() == null ? 0 : aiResponse.getTotalChunks();

            System.out.println("AI response message: " + aiResponse.getMessage());
            System.out.println("Document ID: " + aiResponse.getDocumentId());
            System.out.println("Source URL in Chroma: " + aiResponse.getSourceUrl());
            System.out.println("Chunks added: " + chunksAdded);
            System.out.println("Total chunks: " + totalChunks);
            System.out.println("==============================================\n");

            if (chunksAdded <= 0 && !Boolean.TRUE.equals(aiResponse.getSkipped())) {
                source.setStatus("FAILED");
                source.setChunksAdded(chunksAdded);
                source.setTotalChunks(totalChunks);
                source.setErrorMessage(
                        "AI Service đã phản hồi nhưng không có chunk nào được thêm vào ChromaDB."
                );

                return aiSourceRepository.save(source);
            }

            source.setStatus("INGESTED");
            source.setChunksAdded(chunksAdded);
            source.setTotalChunks(totalChunks);

            if (Boolean.TRUE.equals(aiResponse.getSkipped())) {
                source.setErrorMessage("Tài liệu đã được nạp trước đó, hệ thống đã bỏ qua.");
            } else {
                source.setErrorMessage(null);
            }

            return aiSourceRepository.save(source);
        } catch (Exception ex) {
            source.setStatus("FAILED");
            source.setErrorMessage(ex.getMessage());

            return aiSourceRepository.save(source);
        }
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteSource(@PathVariable Long id) {
        AiSource source = getSourceById(id);

        boolean deletedFromAiService = false;
        boolean deletedFile = false;
        String aiDeleteMessage = null;

        if (!isBlank(source.getLocalFilePath())) {
            try {
                AiDeleteFileRequest aiRequest = new AiDeleteFileRequest(
                        source.getLocalFilePath(),
                        true
                );

                AiDeleteFileResponse aiResponse = aiServiceClient.deleteFile(aiRequest);

                if (aiResponse != null && Boolean.TRUE.equals(aiResponse.getSuccess())) {
                    deletedFromAiService = Boolean.TRUE.equals(aiResponse.getDeletedFromChroma());
                    deletedFile = Boolean.TRUE.equals(aiResponse.getDeletedFile());
                    aiDeleteMessage = aiResponse.getMessage();
                }
            } catch (Exception ex) {
                aiDeleteMessage = "Không xóa được dữ liệu trong AI Service: " + ex.getMessage();
            }
        }

        aiSourceRepository.delete(source);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "Đã xóa nguồn AI khỏi danh sách quản trị.");
        result.put("deletedId", id);
        result.put("deletedFromAiService", deletedFromAiService);
        result.put("deletedFile", deletedFile);
        result.put("aiDeleteMessage", aiDeleteMessage);

        return result;
    }

    @GetMapping("/knowledge-sources")
    public Map<String, Object> getKnowledgeSources() {
        try {
            return aiServiceClient.listKnowledgeSources();
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Không lấy được danh sách source_url từ AI Service: " + ex.getMessage()
            );
        }
    }

    @GetMapping("/knowledge-sources/chunks")
    public Map<String, Object> getKnowledgeChunks(@RequestParam String sourceUrl) {
        if (isBlank(sourceUrl)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "sourceUrl không được để trống."
            );
        }

        try {
            return aiServiceClient.listKnowledgeChunks(sourceUrl);
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Không lấy được danh sách chunk từ AI Service: " + ex.getMessage()
            );
        }
    }

    @DeleteMapping("/knowledge-sources")
    public Map<String, Object> deleteKnowledgeSource(@RequestParam String sourceUrl) {
        if (isBlank(sourceUrl)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "sourceUrl không được để trống."
            );
        }

        try {
            return aiServiceClient.deleteKnowledgeSource(sourceUrl);
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Không xóa được source_url trong AI Service: " + ex.getMessage()
            );
        }
    }

    private AiSource getSourceById(Long id) {
        return aiSourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy nguồn AI có ID: " + id
                ));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || isBlank(authentication.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Không xác định được quản trị viên hiện tại."
            );
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Không tìm thấy tài khoản quản trị viên hiện tại."
                ));
    }

    private void validateCreateRequest(AdminAiSourceRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dữ liệu nguồn AI không được để trống."
            );
        }

        if (isBlank(request.getTitle())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tiêu đề nguồn không được để trống."
            );
        }

        if (isBlank(request.getUrl())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Link nguồn không được để trống."
            );
        }

        String url = request.getUrl().trim();

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Link nguồn phải bắt đầu bằng http:// hoặc https://."
            );
        }
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }

    private String cleanRequired(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
