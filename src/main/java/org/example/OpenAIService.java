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

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private static final String ASSISTANT_INSTRUCTIONS_TEMPLATE = """
        Goal setting: 
        "Your goal is to help users plan events within a predefined budget, while matching their needs and preferences. Always keep the conversation history and be sure to remember the user's previous choices."

        Event Information:
        "Start with the following event information provided by the user: 
        Event Summary: Date: {event_date} Guests: {guest_count} Budget: {total_budget} Remaining Budget: {remaining_budget} Selected Items: {selected_items}"

        The beginning of the conversation:
        "Based on the provided event summary, continue to assist the user by suggesting further steps and making sure to stay within the remaining budget."

        Proposals in stages:
        
        First step: choosing an event venue
        "Provide three options for event venues based on the remaining budget. For each option, give only the actual name of the venue and the estimated price. No additional details are necessary."
        Example of an answer:
        "Name of place 1 - estimated price
        2nd place name - estimated price
        Place Name 3 - Approximate Price"
        
        Second step: choosing a food menu (if required)
        "If the selected venue has food, skip to the next step. If not, offer three food menu options with real vendor names and estimated prices, considering the remaining budget. No more details needed."
        Example of an answer:
        "Food option 1 - approximate price
        Food option 2 - approximate price
        Food option 3 - approximate price"
        
        Third step: Selection of attractions
        "Provide three options for attractions with real vendor names and estimated prices, considering the remaining budget. No more details needed."
        Example of an answer:
        "Attraction 1 - approximate price
        Attraction 2 - approximate price
        Attraction 3 - approximate price"
        
        Interaction with the user:
        "Provide only three options for each parameter, using only specific information and real names, and add an estimated price next to each option, considering the remaining budget."
        "If you don't have relevant information, say 'I don't have that information' instead of making things up or using general examples."
        "Move to the next step only after the user has selected one of the options, and proceed according to the terms, budget, remaining budget, and details provided."
        
        Keep conversation history:
        "Keep conversation history and use the information provided by the user in all previous steps to ensure that the conversation continues consistently. Update the remaining budget accordingly after each selection."
    """;

    // משתנה לשמירת היסטוריית השיחה
    private final JsonArray conversationHistory = new JsonArray();

    public String getEventPlanningAssistance(String userMessage, String eventDate, String guestCount, String totalBudget, String remainingBudget, String selectedItems) {
        // החלפת משתני הטקסט בהוראות
        String assistantInstructions = ASSISTANT_INSTRUCTIONS_TEMPLATE
                .replace("{event_date}", eventDate)
                .replace("{guest_count}", guestCount)
                .replace("{total_budget}", totalBudget)
                .replace("{remaining_budget}", remainingBudget)
                .replace("{selected_items}", selectedItems);

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", assistantInstructions);

        JsonObject userMessageObject = new JsonObject();
        userMessageObject.addProperty("role", "user");
        userMessageObject.addProperty("content", userMessage);

        // היסטוריה רלוונטית לשיחה הנוכחית
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

                // הוסף את תגובת העוזר להיסטוריית השיחה
                JsonObject assistantMessageObject = new JsonObject();
                assistantMessageObject.addProperty("role", "assistant");
                assistantMessageObject.addProperty("content", assistantReply);
                conversationHistory.add(userMessageObject); // הוסף את הודעת המשתמש האחרונה להיסטוריה
                conversationHistory.add(assistantMessageObject); // הוסף את התגובה האחרונה של העוזר להיסטוריה

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
