package com.hotel.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.dto.ChatRequest;
import com.hotel.dto.ChatResponse;
import com.hotel.service.ChatService;
import com.hotel.utils.Resp;

import jakarta.validation.Valid;

/** Authenticated customer chatbot endpoint. */
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public Resp<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return Resp.success(chatService.answer(request));
    }
}
