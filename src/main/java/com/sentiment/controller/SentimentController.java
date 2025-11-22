package com.sentiment.controller;

import com.sentiment.service.SentimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SentimentController {

    @Autowired
    private SentimentService sentimentService;

    @GetMapping("/sentiment")
    public ResponseEntity<Map<String, Object>> analyzeSentiment(
            @RequestParam String text) {

        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Text parameter is required"));
        }

        Map<String, Object> analysis = sentimentService.analyzeSentiment(text);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Russian Sentiment Analysis API",
                "version", "1.0.0"
        ));
    }

    @GetMapping("/model/info")
    public ResponseEntity<Map<String, Object>> getModelInfo() {
        return ResponseEntity.ok(sentimentService.getModelInfo());
    }

    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugAnalysis(
            @RequestParam String text) {

        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Text parameter is required"));
        }

        return ResponseEntity.ok(sentimentService.debugAnalysis(text));
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testSentiment() {
        String[] testTexts = {
                "Это отлично и прекрасно работает",
                "Ужасная ситуация, всё плохо",
                "Обычный день, нормальная погода",
                "Привет как дела"
        };

        Map<String, Object> results = Map.of(
                "test_positive", sentimentService.analyzeSentiment(testTexts[0]),
                "test_negative", sentimentService.analyzeSentiment(testTexts[1]),
                "test_neutral", sentimentService.analyzeSentiment(testTexts[2]),
                "test_unknown", sentimentService.analyzeSentiment(testTexts[3])
        );

        return ResponseEntity.ok(results);
    }
}