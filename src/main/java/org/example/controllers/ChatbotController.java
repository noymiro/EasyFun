package org.example.controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class ChatbotController {

    private static final String GPT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final Logger logger = Logger.getLogger(ChatbotController.class.getName());

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openaimodel}")
    private String[] models;

    private final RestTemplate restTemplate;

    @Autowired
    public ChatbotController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message, @RequestParam(required = false) String model) {
        if (model == null || !isValidModel(model)) {
            model = models[0]; // Default to the first model if none is provided or invalid
        }
        logger.info("Using model: " + model);
        logger.info("Using API Key: " + apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", message);

        requestBody.put("messages", new Object[]{userMessage});
        requestBody.put("max_tokens", 150);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(GPT_API_URL, request, String.class);
            logger.info("API response received: " + response.getBody());

            JsonElement jsonElement = JsonParser.parseString(response.getBody());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonObject choicesObject = jsonObject.getAsJsonArray("choices").get(0).getAsJsonObject();
            String assistantMessage = choicesObject.getAsJsonObject("message").get("content").getAsString();

            logger.info("Extracted assistant message: " + assistantMessage);

            return assistantMessage;

        } catch (Exception e) {
            logger.severe("Error occurred while calling OpenAI API: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private boolean isValidModel(String model) {
        for (String validModel : models) {
            if (validModel.equals(model)) {
                return true;
            }
        }
        return false;
    }
}
