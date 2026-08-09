package com.hotel.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

/** Server-side Gemini adapter. The Android app never receives the API key. */
@Component
public class GeminiClient {

    private static final String SYSTEM_INSTRUCTION = """
            You are the concise StayFlow hotel assistant. Use the LIVE STAYFLOW
            DATA below for every hotel, rating, price, and availability claim.
            Never invent hotels, rooms, prices, ratings, policies, discounts or
            booking confirmations. You cannot create, modify, or cancel bookings.
            Keep answers friendly, under 90 words, and never mention raw data.
            """;

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiClient(
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String model
    ) {
        this.restClient = RestClient.create();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model == null || model.isBlank()
                ? "gemini-2.5-flash" : model.trim();
    }

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    /** Gemini only phrases the safe result of the deterministic DB search. */
    public Optional<String> generateReply(String message, String liveData) {
        if (!isConfigured()) return Optional.empty();

        Map<String, Object> request = Map.of(
                "system_instruction", Map.of("parts", List.of(
                        Map.of("text", SYSTEM_INSTRUCTION))),
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", "Customer message: "
                                + message + "\n\nLIVE STAYFLOW DATA:\n" + liveData)))),
                "generationConfig", Map.of("temperature", 0.25,
                        "maxOutputTokens", 180));

        try {
            JsonNode response = restClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/"
                            + model + ":generateContent")
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);
            String text = response == null ? "" : response.path("candidates")
                    .path(0).path("content").path("parts").path(0)
                    .path("text").asText("").trim();
            return StringUtils.hasText(text) ? Optional.of(text) : Optional.empty();
        } catch (RuntimeException ignored) {
            // Database-backed fallback keeps the assistant useful on quota/API errors.
            return Optional.empty();
        }
    }
}
