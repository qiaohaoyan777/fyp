package com.cmt322.usmsecondhand.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    // ✅ 使用 v1beta 版本的标准模型路径
    // ✅ 换成最稳的 gemini-pro 模型，这个路径在 99% 的情况下都不会报 404
private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=";
    public String generateDescription(String itemName, String condition) {
        System.out.println("DEBUG: Sending request to Gemini for -> " + itemName);
        
        RestTemplate restTemplate = new RestTemplate();

        // 1. 构造发给 AI 的指令
        String prompt = String.format(
            "你是一个校园二手交易平台的助手。用户想要卖掉一件名为【%s】的物品，成色大概是【%s】。" +
            "请你帮他写一段吸引人的二手商品描述，字数在 100-150 字左右。语气要真诚、友好，" +
            "可以适当强调适合学生党，并带上几个 emoji。", 
            itemName, condition
        );

        // 2. 构造符合 Google 标准的请求体
        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();
        List<Map<String, Object>> parts = new ArrayList<>();
        Map<String, Object> part = new HashMap<>();
        
        part.put("text", prompt);
        parts.add(part);
        content.put("parts", parts);
        contents.add(content);
        requestBody.put("contents", contents);

        // 3. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // 4. 发送 POST 请求
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL + apiKey, entity, Map.class);
            
            // 5. 解析返回结果
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> resContent = (Map<String, Object>) candidate.get("content");
                    List<Map<String, Object>> resParts = (List<Map<String, Object>>) resContent.get("parts");
                    return (String) resParts.get(0).get("text");
                }
            }
            return "抱歉，AI 暂时无法生成描述，请手动输入。";
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            // 🚨 专门捕获 HTTP 错误（如 404, 400），并在控制台打印详细的 JSON 报错原因
            System.err.println("❌ Google API 返回了错误状态码: " + e.getStatusCode());
            System.err.println("❌ 详细错误内容: " + e.getResponseBodyAsString());
            return "网络开小差了，请手动输入描述。";
        } catch (Exception e) {
            // 🚨 捕获其他网络超时或连接错误
            System.err.println("❌ 网络请求发生异常: " + e.getMessage());
            return "连接 Google AI 失败，请检查网络设置。";
        }
    }
}