package com.suviet.suviet_api.service;

import com.suviet.suviet_api.dto.AiChatRequest;
import com.suviet.suviet_api.dto.AiChatResponse;
import com.suviet.suviet_api.dto.AiFetchUrlRequest;
import com.suviet.suviet_api.dto.AiFetchUrlResponse;
import com.suviet.suviet_api.dto.AiIngestFileRequest;
import com.suviet.suviet_api.dto.AiIngestFileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.suviet.suviet_api.dto.AiDeleteFileRequest;
import com.suviet.suviet_api.dto.AiDeleteFileResponse;

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

        return restTemplate.postForObject(url, request, AiFetchUrlResponse.class);
    }

    public AiIngestFileResponse ingestFile(AiIngestFileRequest request) {
        String url = aiServiceUrl + "/admin/knowledge/ingest-file";

        return restTemplate.postForObject(url, request, AiIngestFileResponse.class);
    }
    public AiDeleteFileResponse deleteFile(AiDeleteFileRequest request) {
        String url = aiServiceUrl + "/admin/knowledge/delete-file";

        return restTemplate.postForObject(url, request, AiDeleteFileResponse.class);
    }
}