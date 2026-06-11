package com.suviet.suviet_api.service;

import com.suviet.suviet_api.dto.AiChatRequest;
import com.suviet.suviet_api.dto.AiChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
}