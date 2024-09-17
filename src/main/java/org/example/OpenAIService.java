package org.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

@Service
public class OpenAIService {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private static final String ASSISTANT_INSTRUCTIONS_TEMPLATE = """
                Goal setting: 
                "Your goal is to help users plan events within a predefined budget, while matching their needs and preferences. Use dynamic language but always provide concrete options first."
            
                Event Information:
                "You are provided with the following event information: 
                Event Summary: Date: {event_date}, Guests: {guest_count}, Budget: {total_budget}, Remaining Budget: {remaining_budget}, Selected Items: {selected_items}."
            
                The beginning of the conversation:
                "Immediately after receiving the event information, present three venue options based on the remaining budget without engaging in small talk. The user should see venue options as soon as the conversation begins."
            
                Proposals in stages:
            
                First step: choosing an event venue
                "Provide three venue options immediately. Format each option as follows: 
                Option {number}: {Venue Name} - {Vendor Name}, Estimated Price: ${price}."
            
                Second step: choosing a food menu (if required)
                "If the selected venue has food, skip to the next step. If not, present three food options using the format: 
                Option {number}: {Food Option} - {Vendor Name}, Estimated Price: ${price}."
            
                Third step: Selection of attractions
                "Once food has been chosen, move directly to entertainment or attraction options. Present three attraction options as follows: 
                Option {number}: {Attraction} - {Vendor Name}, Estimated Price: ${price}."
            
                Interaction with the user:
                "For every step, present only three concrete options with real names and estimated prices. Avoid open-ended questions and proceed immediately to the next step after the user has made a selection."
            
                Keep conversation history:
                "Always track the user’s selections and update the remaining budget accordingly. Use the user's past choices to guide future suggestions and ensure consistency."
            """;

    private final JsonArray conversationHistory = new JsonArray();

    @Value("${openai.api.key}")
    private String apiKey;

    public String getEventPlanningAssistance(String userMessage, String eventDate, String guestCount, String totalBudget, String remainingBudget, String selectedItems) {
        String assistantInstructions = ASSISTANT_INSTRUCTIONS_TEMPLATE
                .replace("{event_date}", eventDate)
                .replace("{guest_count}", guestCount)
                .replace("{total_budget}", totalBudget)
                .replace("{remaining_budget}", remainingBudget)
                .replace("{selected_items}", selectedItems);

        return sendRequestToOpenAI(assistantInstructions, userMessage);
    }

    public String getSuggestedVenues(String eventType, String eventDate, String guestCount, String remainingBudget, String location) {
        int budget = Integer.parseInt(remainingBudget);
        int highBudgetLimit = (int) (budget * 0.30);
        int midBudgetLimit = (int) (budget * 0.20);
        int lowBudgetLimit = (int) (budget * 0.10);

        String assistantInstructions = String.format("""
        Goal setting: 
        "Your goal is to suggest three suitable venues for an event based on the event type, date, guest count, budget, and location. Ensure the venues exist in the specified location. If a suitable venue is not found, clearly state 'No suitable venue found' with a price of $0."

        Event Information:
        "Event Type: %s, Event Date: %s, Number of Guests: %s, Remaining Budget: %s, Location: %s"

        Response Guidelines:
        "Based on the provided event information, suggest three venue options:
        1. Low-cost option (up to %d USD)
        2. Mid-range option (up to %d USD)
        3. High-end option (up to %d USD, but not exceeding 30%% of the total budget)

        If no venue fits the criteria, state 'No suitable venue found - $0' for that option. Do not invent venues. Ensure all venues suggested exist in the specified location."

        Example Format:
        "1. Venue Name - Brief description - Estimated price
        2. Venue Name - Brief description - Estimated price
        3. Venue Name - Brief description - Estimated price"
        """, eventType, eventDate, guestCount, remainingBudget, location, lowBudgetLimit, midBudgetLimit, highBudgetLimit);

        return sendRequestToOpenAI(assistantInstructions);
    }

    public String getSuggestedFood(String eventType, String eventDate, String guestCount, String remainingBudget, String location) {
        int budget = Integer.parseInt(remainingBudget);
        int highBudgetLimit = (int) (budget * 0.30);
        int midBudgetLimit = (int) (budget * 0.20);
        int lowBudgetLimit = (int) (budget * 0.10);

        String assistantInstructions = String.format("""
        Goal setting: 
        "Your goal is to suggest three suitable food options for an event based on the event type, date, guest count, budget, and location. Ensure the food options are feasible and within the specified budget limits."

        Event Information:
        "Event Type: %s, Event Date: %s, Number of Guests: %s, Remaining Budget: %s, Location: %s"

        Response Guidelines:
        "Based on the provided event information, suggest three food options:
        1. Low-cost option (up to %d USD)
        2. Mid-range option (up to %d USD)
        3. High-end option (up to %d USD, but not exceeding 30%% of the total budget)

        If no food fits the criteria, state 'No suitable food option found - $0' for that option."

        Example Format:
        "1. Food Option Name - Brief description - Estimated price
        2. Food Option Name - Brief description - Estimated price
        3. Food Option Name - Brief description - Estimated price"
        """, eventType, eventDate, guestCount, remainingBudget, location, lowBudgetLimit, midBudgetLimit, highBudgetLimit);

        return sendRequestToOpenAI(assistantInstructions);
    }

    public String getSuggestedAttractions(String eventType, String eventDate, String guestCount, String remainingBudget, String location) {
        int budget = Integer.parseInt(remainingBudget);
        int highBudgetLimit = (int) (budget * 0.30);
        int midBudgetLimit = (int) (budget * 0.20);
        int lowBudgetLimit = (int) (budget * 0.10);

        String assistantInstructions = String.format("""
        Goal setting: 
        "Your goal is to suggest three suitable attractions for an event based on the event type, date, guest count, budget, and location. Ensure the attractions are feasible and within the specified budget limits."

        Event Information:
        "Event Type: %s, Event Date: %s, Number of Guests: %s, Remaining Budget: %s, Location: %s"

        Response Guidelines:
        "Based on the provided event information, suggest three attractions:
        1. Low-cost option (up to %d USD)
        2. Mid-range option (up to %d USD)
        3. High-end option (up to %d USD, but not exceeding 30%% of the total budget)

        If no attraction fits the criteria, state 'No suitable attraction found - $0' for that option."

        Example Format:
        "1. Attraction Name - Brief description - Estimated price
        2. Attraction Name - Brief description - Estimated price
        3. Attraction Name - Brief description - Estimated price"
        """, eventType, eventDate, guestCount, remainingBudget, location, lowBudgetLimit, midBudgetLimit, highBudgetLimit);

        return sendRequestToOpenAI(assistantInstructions);
    }

    private String sendRequestToOpenAI(String assistantInstructions) {
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", assistantInstructions);

        JsonArray currentConversation = new JsonArray();
        currentConversation.add(systemMessage);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-4");
        requestBody.add("messages", currentConversation);

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            System.out.println("Full Response from OpenAI: " + response.body());

            if (response.statusCode() == 200) {
                String assistantReply = extractContentFromResponse(response.body());

                JsonObject assistantMessageObject = new JsonObject();
                assistantMessageObject.addProperty("role", "assistant");
                assistantMessageObject.addProperty("content", assistantReply);
                conversationHistory.add(assistantMessageObject);

                return assistantReply;
            } else {
                System.out.println("Error: " + response.body());
                return "Error: " + response.body();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
    private String sendRequestToOpenAI(String assistantInstructions, String userMessage) {
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", assistantInstructions);

        JsonObject userMessageObject = new JsonObject();
        userMessageObject.addProperty("role", "user");
        userMessageObject.addProperty("content", userMessage);

        JsonArray currentConversation = new JsonArray();
        currentConversation.add(systemMessage);

        // Limit the conversation history to the last 10 messages
        int historySize = conversationHistory.size();
        int startIndex = Math.max(0, historySize - 10);
        for (int i = startIndex; i < historySize; i++) {
            currentConversation.add(conversationHistory.get(i));
        }

        currentConversation.add(userMessageObject);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-4");
        requestBody.add("messages", currentConversation);

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            System.out.println("Full Response from OpenAI: " + response.body());

            if (response.statusCode() == 200) {
                String assistantReply = extractContentFromResponse(response.body());

                JsonObject assistantMessageObject = new JsonObject();
                assistantMessageObject.addProperty("role", "assistant");
                assistantMessageObject.addProperty("content", assistantReply);
                conversationHistory.add(userMessageObject);
                conversationHistory.add(assistantMessageObject);

                return assistantReply;
            } else {
                System.out.println("Error: " + response.body());
                return "Error: " + response.body();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private String extractContentFromResponse(String response) {
        try {
            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
            JsonArray choices = jsonObject.getAsJsonArray("choices");
            if (choices != null && choices.size() > 0) {
                JsonObject firstChoice = choices.get(0).getAsJsonObject();
                JsonObject message = firstChoice.getAsJsonObject("message");
                if (message != null) {
                    return message.get("content").getAsString();
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to parse response: " + e.getMessage());
        }
        return "Error parsing response from OpenAI.";
    }
}
