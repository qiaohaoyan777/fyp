package com.cmt322.usmsecondhand.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    public String generateDescription(String title, String condition) {
        // 换成最新的 gemini-2.0-flash 模型，速度极快且通道更宽
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt = String.format("You are an expert copywriter for a university student second-hand marketplace. " +
                "Write an attractive, honest, and highly engaging product description for the following item. " +
                "Item Title: %s. Condition: %s. " +
                "Keep it concise (around 50-80 words), highlight its value for students, use a friendly tone, and do not use hashtags.", 
                title, condition);

        String requestBody = "{\n" +
                "  \"contents\": [{\n" +
                "    \"parts\":[{\"text\": \"" + prompt.replace("\"", "\\\"") + "\"}]\n" +
                "  }]\n" +
                "}";

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
            JsonObject jsonObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
            
            if (jsonObject.has("candidates")) {
                return jsonObject.getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString().trim();
            } else {
                return "Google API returned an unexpected format: " + response.getBody();
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String errorMsg = e.getResponseBodyAsString();
            System.err.println("=== Google Gemini API Error ===");
            System.err.println(errorMsg);
            return "Google API Refused: " + errorMsg;
            
        } catch (Exception e) {
            System.err.println("=== Connection Error ===");
            e.printStackTrace();
            return "Network Connection Failed: " + e.getMessage();
        }
    }
}