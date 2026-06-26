package com.suviet.suviet_api.service;

import com.suviet.suviet_api.dto.AiChatRequest;
import com.suviet.suviet_api.dto.AiChatResponse;
import com.suviet.suviet_api.dto.AiDeleteFileRequest;
import com.suviet.suviet_api.dto.AiDeleteFileResponse;
import com.suviet.suviet_api.dto.AiFetchUrlRequest;
import com.suviet.suviet_api.dto.AiFetchUrlResponse;
import com.suviet.suviet_api.dto.AiIngestFileRequest;
import com.suviet.suviet_api.dto.AiIngestFileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class AiServiceClient {

    private final RestTemplate restTemplate;

    @Value("${suviet.ai-service.url}")
    private String aiServiceUrl;

    public AiServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AiChatResponse ask(String question) {
        String url = aiServiceUrl + "/chat";

        AiChatRequest request = new AiChatRequest(question);

        return restTemplate.postForObject(url, request, AiChatResponse.class);
    }

    public AiFetchUrlResponse fetchUrl(AiFetchUrlRequest request) {
        String url = aiServiceUrl + "/admin/knowledge/fetch-url";

        System.out.println("[AI SERVICE] POST " + url);
        System.out.println("[AI SERVICE] Fetch URL: " + request.getUrl());

        return restTemplate.postForObject(url, request, AiFetchUrlResponse.class);
    }

    public AiIngestFileResponse ingestFile(AiIngestFileRequest request) {
        String url = aiServiceUrl + "/admin/knowledge/ingest-file";

        System.out.println("[AI SERVICE] POST " + url);
        System.out.println("[AI SERVICE] File path gửi sang FastAPI: " + request.getFilePath());

        AiIngestFileResponse response = restTemplate.postForObject(
                url,
                request,
                AiIngestFileResponse.class
        );

        if (response != null) {
            System.out.println("[AI SERVICE] Ingest success: " + response.getSuccess());
            System.out.println("[AI SERVICE] Chunks added: " + response.getChunksAdded());
            System.out.println("[AI SERVICE] Total chunks: " + response.getTotalChunks());
            System.out.println("[AI SERVICE] Source URL: " + response.getSourceUrl());
        } else {
            System.out.println("[AI SERVICE] FastAPI trả về null khi ingest.");
        }

        return response;
    }

    public AiDeleteFileResponse deleteFile(AiDeleteFileRequest request) {
        String url = aiServiceUrl + "/admin/knowledge/delete-file";

        return restTemplate.postForObject(url, request, AiDeleteFileResponse.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> listKnowledgeSources() {
        String url = aiServiceUrl + "/admin/knowledge/sources";

        return restTemplate.getForObject(url, Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> listKnowledgeChunks(String sourceUrl) {
        String url = UriComponentsBuilder
                .fromUriString(aiServiceUrl + "/admin/knowledge/sources/chunks")
                .queryParam("sourceUrl", sourceUrl)
                .toUriString();

        return restTemplate.getForObject(url, Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> deleteKnowledgeSource(String sourceUrl) {
        String url = UriComponentsBuilder
                .fromUriString(aiServiceUrl + "/admin/knowledge/sources")
                .queryParam("sourceUrl", sourceUrl)
                .toUriString();

        return restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                null,
                Map.class
        ).getBody();
    }
}
