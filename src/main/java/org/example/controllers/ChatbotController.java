package org.example.controllers;

import org.example.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.logging.Logger;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class ChatbotController {

    private static final Logger logger = Logger.getLogger(ChatbotController.class.getName());

    @Value("${openai.api.key}")
    private String apiKey;

    private final OpenAIService openAIService;

    @Autowired
    public ChatbotController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        logger.info("Using API Key: " + apiKey);

        String response = openAIService.getEventPlanningAssistance(message);
        logger.info("Response from OpenAI: " + response);

        // בדוק אם התגובה היא JSON או טקסט רגיל
        String content = extractContentFromResponse(response);
        return content;
    }

    private String extractContentFromResponse(String response) {
        try {
            // בדיקה אם התגובה מתחילה ב-{ כדי לוודא שהיא בפורמט JSON
            if (response.trim().startsWith("{")) {
                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                JsonObject firstChoice = jsonObject.getAsJsonArray("choices").get(0).getAsJsonObject();
                JsonObject message = firstChoice.getAsJsonObject("message");
                if (message != null) {
                    return message.get("content").getAsString();
                }
            } else {
                // אם התגובה היא טקסט רגיל, פשוט החזר אותה
                return response.trim();
            }
        } catch (Exception e) {
            logger.severe("Failed to parse response: " + e.getMessage());
            return "Error parsing response from OpenAI.";
        }
        return "Error parsing response from OpenAI.";
    }
}
