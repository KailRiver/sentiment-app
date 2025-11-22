package com.sentiment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class SentimentService {

    private final Map<String, Map<String, Double>> sentimentModel;
    private final ObjectMapper objectMapper;
    private final Pattern wordPattern = Pattern.compile("[^а-яёa-z0-9\\s]");

    public SentimentService() {
        this.objectMapper = new ObjectMapper();
        this.sentimentModel = loadSentimentModel();
    }

    private Map<String, Map<String, Double>> loadSentimentModel() {
        try {
            ClassPathResource resource = new ClassPathResource("sentiment-model.json");
            return objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<Map<String, Map<String, Double>>>() {}
            );
        } catch (IOException e) {
            // Если не удалось загрузить модель, создаем простую заглушку
            System.err.println("Failed to load sentiment model: " + e.getMessage());
            return createFallbackModel();
        }
    }

    private Map<String, Map<String, Double>> createFallbackModel() {
        Map<String, Map<String, Double>> fallbackModel = new HashMap<>();

        // Простая модель на случай ошибки загрузки
        Map<String, Double> positive = new HashMap<>();
        positive.put("отлично", 1.0);
        positive.put("хорошо", 0.8);
        positive.put("прекрасно", 1.0);

        Map<String, Double> negative = new HashMap<>();
        negative.put("плохо", 0.8);
        negative.put("ужасно", 1.0);
        negative.put("кошмар", 1.0);

        Map<String, Double> neutral = new HashMap<>();
        neutral.put("нормально", 0.5);
        neutral.put("обычно", 0.5);

        fallbackModel.put("positive", positive);
        fallbackModel.put("negative", negative);
        fallbackModel.put("neutral", neutral);

        return fallbackModel;
    }

    public Map<String, Object> analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return createResponse("neutral", 0.5, text);
        }

        // Очистка текста и приведение к нижнему регистру
        String cleanedText = preprocessText(text);
        String[] words = cleanedText.split("\\s+");

        double positiveScore = 0.0;
        double negativeScore = 0.0;
        double neutralScore = 0.0;
        int matchedWords = 0;

        // Анализ каждого слова
        for (String word : words) {
            if (word.length() < 2) continue;

            // Проверяем слово во всех категориях
            for (Map.Entry<String, Map<String, Double>> category : sentimentModel.entrySet()) {
                Double weight = category.getValue().get(word);
                if (weight != null) {
                    switch (category.getKey()) {
                        case "positive":
                            positiveScore += weight;
                            matchedWords++;
                            break;
                        case "negative":
                            negativeScore += weight;
                            matchedWords++;
                            break;
                        case "neutral":
                            neutralScore += weight;
                            matchedWords++;
                            break;
                    }
                }
            }
        }

        // Определение доминирующей тональности
        String sentiment;
        double confidence;

        if (positiveScore > negativeScore && positiveScore > neutralScore) {
            sentiment = "positive";
            confidence = normalizeConfidence(positiveScore, matchedWords);
        } else if (negativeScore > positiveScore && negativeScore > neutralScore) {
            sentiment = "negative";
            confidence = normalizeConfidence(negativeScore, matchedWords);
        } else {
            sentiment = "neutral";
            confidence = normalizeConfidence(neutralScore, matchedWords);
        }

        // Если нет найденных слов, возвращаем neutral с низкой уверенностью
        if (matchedWords == 0) {
            sentiment = "neutral";
            confidence = 0.1;
        }

        return createResponse(sentiment, Math.min(confidence, 1.0), text);
    }

    private double normalizeConfidence(double score, int wordCount) {
        if (wordCount == 0) return 0.1;
        return score / wordCount;
    }

    private String preprocessText(String text) {
        // Приведение к нижнему регистру и удаление знаков препинания
        return wordPattern.matcher(text.toLowerCase()).replaceAll(" ");
    }

    private Map<String, Object> createResponse(String sentiment, double confidence, String text) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sentiment", sentiment);
        response.put("confidence", Math.round(confidence * 100.0) / 100.0);
        response.put("text", text);
        response.put("timestamp", new Date());
        return response;
    }

    // Метод для получения информации о модели
    public Map<String, Object> getModelInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("positive_words", sentimentModel.get("positive").size());
        info.put("negative_words", sentimentModel.get("negative").size());
        info.put("neutral_words", sentimentModel.get("neutral").size());
        info.put("total_words",
                sentimentModel.get("positive").size() +
                        sentimentModel.get("negative").size() +
                        sentimentModel.get("neutral").size());
        info.put("model_loaded", !sentimentModel.isEmpty());
        return info;
    }

    // Метод для отладки - показать найденные слова в тексте
    public Map<String, Object> debugAnalysis(String text) {
        Map<String, Object> debugInfo = new LinkedHashMap<>();
        debugInfo.put("original_text", text);

        String cleanedText = preprocessText(text);
        debugInfo.put("cleaned_text", cleanedText);

        String[] words = cleanedText.split("\\s+");
        debugInfo.put("words", Arrays.asList(words));

        List<Map<String, Object>> matchedWords = new ArrayList<>();
        for (String word : words) {
            if (word.length() < 2) continue;

            for (Map.Entry<String, Map<String, Double>> category : sentimentModel.entrySet()) {
                Double weight = category.getValue().get(word);
                if (weight != null) {
                    Map<String, Object> match = new HashMap<>();
                    match.put("word", word);
                    match.put("category", category.getKey());
                    match.put("weight", weight);
                    matchedWords.add(match);
                }
            }
        }

        debugInfo.put("matched_words", matchedWords);
        debugInfo.put("analysis_result", analyzeSentiment(text));

        return debugInfo;
    }
}