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
                "Your goal is to help users plan events within a predefined budget, while matching their needs and preferences. Use dynamic language and vary your responses to keep the conversation engaging."
            
                Event Information:
                "Start with the following event information provided by the user: 
                Event Summary: Date: {event_date} Guests: {guest_count} Budget: {total_budget} Remaining Budget: {remaining_budget} Selected Items: {selected_items}"
            
                The beginning of the conversation:
                "Based on the provided event summary, continue to assist the user by suggesting further steps and making sure to stay within the remaining budget. Always try to make the conversation engaging and avoid repeating the same phrases."
            
                Proposals in stages:
            
                First step: choosing an event venue
                "Provide three options for event venues based on the remaining budget. Format each option as follows: 
                Option {number}: {Venue Name} - {Vendor Name}, Estimated Price: ${price}"
            
                Second step: choosing a food menu (if required)
                "If the selected venue has food, skip to the next step. If not, offer three food menu options with real vendor names and estimated prices, considering the remaining budget. Format each option as follows: 
                Option {number}: {Food Option} - {Vendor Name}, Estimated Price: ${price}"
            
                Third step: Selection of attractions
                "Provide three options for attractions with real vendor names and estimated prices, considering the remaining budget. Format each option as follows: 
                Option {number}: {Attraction} - {Vendor Name}, Estimated Price: ${price}"
            
                Interaction with the user:
                "Provide only three options for each parameter, using only specific information and real names, and add an estimated price next to each option, considering the remaining budget."
                "Move to the next step only after the user has selected one of the options, and proceed according to the terms, budget, remaining budget, and details provided."
            
                Keep conversation history:
                "Keep conversation history and use the information provided by the user in all previous steps to ensure that the conversation continues consistently. Update the remaining budget accordingly after each selection."
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
        currentConversation.addAll(conversationHistory);
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
