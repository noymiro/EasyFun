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
    "Your goal is to assist users in enhancing their event by suggesting additional items or services within the remaining budget. Adapt your suggestions based on the event type, location, and selected items. Ensure all responses are formatted consistently with three concrete options."

    Event Information:
    "You are provided with the following event details:
    - Event Date: {event_date}
    - Number of Guests: {guest_count}
    - Total Budget: ${total_budget}
    - Remaining Budget: ${remaining_budget}
    - Selected Items: {selected_items}
    - Event Location: {location}"
    
    Start of the conversation:
    "Present three additional service or item options based on the remaining budget and event type. Avoid small talk. The user should immediately see relevant options in three precise lines."

    Response Template:
    "Always format your responses in the following structure:
    
    Option {number}: {Option Name} - {Vendor Name}, Estimated Price: ${price}
    
    Example:
    Option 1: Photographer - Event Photos Ltd., Estimated Price: $1,000
    Option 2: Decoration - Party Design Co., Estimated Price: $500
    Option 3: Sound System - DJ Sound Experts, Estimated Price: $750
    
    Remaining Budget: ${remaining_budget}"
    
    Proposals in stages:
    "For each step, provide three concrete options formatted using the template above. Ensure the options fit within the remaining budget."

    Budget management:
    "After each selection, update the remaining budget and present only options that fit within it using the template. If the remaining budget is low, present low-cost alternatives or suggest skipping certain elements."
    
    Adaptation:
    "Adapt your responses based on the user's past choices and remaining budget. Always follow the three-line format."
""";




    private final JsonArray conversationHistory = new JsonArray();

    @Value("${openai.api.key}")
    private String apiKey;

    public String getEventPlanningAssistance(String userMessage, String eventDate, String guestCount, String totalBudget, String remainingBudget, String selectedItems,String location) {
        String assistantInstructions = ASSISTANT_INSTRUCTIONS_TEMPLATE
                .replace("{event_date}", eventDate)
                .replace("{guest_count}", guestCount)
                .replace("{total_budget}", totalBudget)
                .replace("{remaining_budget}", remainingBudget)
                .replace("{selected_items}", selectedItems)
                .replace("{location}", location);

        return sendRequestToOpenAI(assistantInstructions, userMessage);
    }

    public String getSuggestedVenues(String eventType, String eventDate, String guestCount, String remainingBudget, String location) {
        int budget = Integer.parseInt(remainingBudget);
        int highBudgetLimit = (int) (budget * 0.30);
        int midBudgetLimit = (int) (budget * 0.20);
        int lowBudgetLimit = (int) (budget * 0.10);

        String assistantInstructions = String.format("""
        Goal setting: 
        "Your goal is to suggest three suitable venues for an event based on the event type, date, guest count, budget, and location.
        Ensure the venues exist in the specified location. If a suitable venue is not found, clearly state 'No suitable venue found' with a price of $0."

        Event Information:
        "Event Type: %s, Event Date: %s, Number of Guests: %s, Remaining Budget: %s, Location: %s"

        Response Guidelines:
        "Based on the provided event information, suggest three venue options:
        1. Low-cost option (up to %d USD)
        2. Mid-range option (up to %d USD)
        3. High-end option (up to %d USD, but not exceeding 30%% of the total budget).

        If no venue fits the criteria, state 'No suitable venue found - $0' for that option. Do not invent venues. 
        Ensure all venues suggested exist in the specified location."

        Example Format:
        "1. Venue Name - Brief description - Estimated price
        2. Venue Name - Brief description - Estimated price
        3. Venue Name - Brief description - Estimated price."
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
    "Your goal is to suggest three suitable food options for an event based on the event type, date, guest count, budget, and location. 
    Ensure that each option includes the name of the food, a **concise description** (up to 100 characters), and the estimated price in USD."
    Event Information:
    "Event Type: %s, Event Date: %s, Number of Guests: %s, Remaining Budget: %s, Location: %s"
    Response Guidelines:
    "Based on the provided event information, suggest three food options:
    1. Low-cost option (up to %d USD)
    2. Mid-range option (up to %d USD)
    3. High-end option (up to %d USD, but not exceeding 30%% of the total budget).
    Each option should follow this format:
    1. [Food Option Name] - [Concise description, max 100 characters] - Estimated price: $[price]
    2. [Food Option Name] - [Concise description, max 100 characters] - Estimated price: $[price]
    3. [Food Option Name] - [Concise description, max 100 characters] - Estimated price: $[price]
    Example response:
    1. Vegan Salad Bar - Fresh salads with seasonal veggies and toppings - Estimated price: $300
    2. BBQ Grill - Grilled meats and vegetables with sauces - Estimated price: $600
    3. Seafood Platter - A selection of local seafood, including shrimp and fish - Estimated price: $1200

    If no food fits the criteria, state 'No suitable food option found - $0' for that option."
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
"Your goal is to suggest three suitable attractions or activities for an event based on the event type, date, guest count, budget, and the pre-selected location. Ensure all suggestions are feasible, within the specified budget limits, and logically appropriate for the chosen location and event type."

Event Information:
"Event Type: %s, Event Date: %s, Number of Guests: %s, Remaining Budget: %s, Location: %s"

Response Guidelines:
"Based on the provided event information, suggest three attractions or activities that are suitable for the selected location:
1. A low-cost option (up to %d USD)
2. A mid-range option (up to %d USD)
3. A high-end option (up to %d USD, but not exceeding 30%% of the total budget)

Ensure all suggestions are logically consistent with the location. For example, don't suggest outdoor activities for an indoor venue. If no suitable option is found for any category, state 'No suitable option found - 0 USD' for that item.

Important: 
- Do not mention the budget category in the response. 
- Present prices as whole numbers without decimals or currency symbols.
- Do not use any punctuation at the end of the price or anywhere else in the response."

Example Format:
"1. Attraction/Activity Name - Brief description - Estimated price
2. Attraction/Activity Name - Brief description - Estimated price
3. Attraction/Activity Name - Brief description - Estimated price."
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
