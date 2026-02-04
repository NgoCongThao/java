package com.s2o.backend_api.controller;

import com.s2o.backend_api.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/ask")
    public ResponseEntity<?> askBot(@RequestBody Map<String, Object> req) {
        String message = (String) req.get("message");
        
        Long restaurantId = null;
        if (req.get("restaurantId") != null) {
            try {
                restaurantId = Long.valueOf(req.get("restaurantId").toString());
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        String reply = chatService.chatWithAI(message, restaurantId);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}