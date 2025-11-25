package com.example.aichatsystem.controller;

import com.example.aichatsystem.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {
    
    private final ExportService exportService;
    
    @GetMapping("/conversation/{conversationId}/text")
    public ResponseEntity<byte[]> exportAsText(
            @PathVariable Long conversationId,
            Authentication authentication) {
        
        byte[] data = exportService.exportConversationAsText(conversationId, authentication.getName());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "conversation_" + conversationId + ".txt");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }
    
    @GetMapping("/conversation/{conversationId}/json")
    public ResponseEntity<byte[]> exportAsJson(
            @PathVariable Long conversationId,
            Authentication authentication) {
        
        byte[] data = exportService.exportConversationAsJson(conversationId, authentication.getName());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "conversation_" + conversationId + ".json");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }
    
    @GetMapping("/conversation/{conversationId}/markdown")
    public ResponseEntity<byte[]> exportAsMarkdown(
            @PathVariable Long conversationId,
            Authentication authentication) {
        
        byte[] data = exportService.exportConversationAsMarkdown(conversationId, authentication.getName());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_MARKDOWN);
        headers.setContentDispositionFormData("attachment", "conversation_" + conversationId + ".md");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }
}
