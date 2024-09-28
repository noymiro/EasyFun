package org.example.controllers;

import org.example.OpenAIService;
import org.example.entities.Item;
import org.example.entities.elements.Attraction;
import org.example.entities.elements.EventAddition;
import org.example.entities.elements.Food;
import org.example.entities.elements.Place;
import org.example.utils.Persist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class ChatbotController {

    private static final Logger logger = Logger.getLogger(ChatbotController.class.getName());

    @Value("${openai.api.key}")
    private String apiKey;

    private final OpenAIService openAIService;
    private final Persist persist;

    @Autowired
    public ChatbotController(OpenAIService openAIService, Persist persist) {
        this.openAIService = openAIService;
        this.persist = persist;
    }

    @GetMapping("/chat")
    public String chat(
            @RequestParam String message,
            @RequestParam String eventDate,
            @RequestParam String guestCount,
            @RequestParam String totalBudget,
            @RequestParam String remainingBudget,
            @RequestParam String selectedItems,
            @RequestParam String location
    ) {

        logger.info("Using API Key: " + apiKey);

        String response;
        try {
            response = openAIService.getEventPlanningAssistance(message, eventDate, guestCount, totalBudget, remainingBudget, selectedItems, location);
            logger.info("Response from OpenAI: " + response);
        } catch (Exception e) {
            logger.severe("Failed to get response from OpenAI: " + e.getMessage());
            response = getFallbackEventAdditions(Float.parseFloat(remainingBudget),location);
        }

        String content = extractContentFromResponse(response);
        System.out.println("content: " + content);
        List<Item> items = createItemsFromChatResponse(content,location);

        for (Item item : items) {
            EventAddition eventAddition = new EventAddition(item.getName(), "Event Addition", item.getPrice(), item.getDescription(), location);
            persist.addItem(eventAddition);
        }

        return content;
    }

    private String getFallbackEventAdditions(float remainingBudget, String location) {
        List<Item> items = persist.getItemsByCategoryAndBudget("EventAddition", remainingBudget,location);
        StringBuilder response = new StringBuilder();
        for (Item item : items) {
            response.append(item.getName()).append(" - ").append(item.getDescription()).append(" - Estimated price: $").append(item.getPrice()).append("\n");
        }
        return response.toString();
    }

    @GetMapping("get-three-event-additions")
    public String getThreeEventAdditions(int remainingBudget,String location) {
        List<Item> items = persist.getItemsByCategoryAndBudget("EventAddition", remainingBudget,location);
        StringBuilder response = new StringBuilder();
        for (int i = 0; i < 3 && i < items.size(); i++) {
            Item item = items.get(i);
            response.append(item.getName()).append(" - ").append(item.getDescription()).append(" - Estimated price: $").append(item.getPrice()).append("\n");
        }
        return response.toString();
    }


    @GetMapping("/get-suggested-venues")
    public String getSuggestedVenues(
            @RequestParam String eventType,
            @RequestParam String eventDate,
            @RequestParam String guestCount,
            @RequestParam String remainingBudget,
            @RequestParam String location) {

        logger.info("Received request with eventType: " + eventType + ", eventDate: " + eventDate + ", guestCount: " + guestCount + ", remainingBudget: " + remainingBudget + ", location: " + location);

        String response;
        try {
            response = openAIService.getSuggestedVenues(eventType, eventDate, guestCount, remainingBudget, location);
            logger.info("Response from OpenAI: " + response);
        } catch (Exception e) {
            logger.severe("Failed to get response from OpenAI: " + e.getMessage());
            response = getFallbackVenues(Float.parseFloat(remainingBudget),location);
        }

        List<Item> items = createItemsFromString(response,location);

        for (Item item : items) {

            Place place = new Place(item.getName(), "Place", item.getPrice(), item.getDescription(), location);
            persist.addItem(place);
        }

        return response;
    }


    private String getFallbackVenues(float remainingBudget,String location) {
        List<Item> items = persist.getItemsByCategoryAndBudget("Place", remainingBudget,location);
        StringBuilder response = new StringBuilder();
        for (Item item : items) {
            response.append(item.getName()).append(" - ").append(item.getDescription()).append(" - Estimated price: $").append(item.getPrice()).append("\n");
        }
        return response.toString();
    }

    @GetMapping("get-three-places")
    public String getThreePlaces(int remainingBudget,String location) {
        List<Item> items = persist.getItemsByCategoryAndBudget("Place", remainingBudget,location);
        StringBuilder response = new StringBuilder();
        for (int i = 0; i < 3 && i < items.size(); i++) {
            Item item = items.get(i);
            response.append(item.getName()).append(" - ").append(item.getDescription()).append(" - Estimated price: $").append(item.getPrice()).append("\n");
        }
        return response.toString();
    }


    @GetMapping("/get-suggested-food")
    public String getSuggestedFood(
            @RequestParam String eventType,
            @RequestParam String eventDate,
            @RequestParam String guestCount,
            @RequestParam String remainingBudget,
            @RequestParam String location) {

        logger.info("Received food request with eventType: " + eventType + ", eventDate: " + eventDate + ", guestCount: " + guestCount + ", remainingBudget: " + remainingBudget + ", location: " + location);

        String response;
        try {
            response = openAIService.getSuggestedFood(eventType, eventDate, guestCount, remainingBudget, location);
            logger.info("Response from OpenAI (food): " + response);
        } catch (Exception e) {
            logger.severe("Failed to get response from OpenAI: " + e.getMessage());
            response = getFallbackFood(Float.parseFloat(remainingBudget), location);
        }

        List<Item> items = createItemsFromString(response,location);

        for (Item item : items) {
            Food food = new Food(item.getName(), "Food", item.getPrice(), item.getDescription(), item.getLocation());
            persist.addItem(food);
        }

        return response;
    }

    private String getFallbackFood(float remainingBudget, String location) {
        List<Item> items = persist.getItemsByCategoryAndBudget("Food", remainingBudget,location);
        StringBuilder response = new StringBuilder();
        for (Item item : items) {
            response.append(item.getName()).append(" - ").append(item.getDescription()).append(" - Estimated price: $").append(item.getPrice()).append("\n");
        }
        return response.toString();
    }

    @GetMapping("get-three-foods")
    public String getThreeFoods(int remainingBudget,String location) {
        List<Item> items = persist.getItemsByCategoryAndBudget("Food", remainingBudget,location);
        StringBuilder response = new StringBuilder();
        for (int i = 0; i < 3 && i < items.size(); i++) {
            Item item = items.get(i);
            response.append(item.getName()).append(" - ").append(item.getDescription()).append(" - Estimated price: $").append(item.getPrice()).append("\n");
        }
        return response.toString();
    }

    @GetMapping("/get-suggested-attractions")
    public String getSuggestedAttractions(
            @RequestParam String eventType,
            @RequestParam String eventDate,
            @RequestParam String guestCount,
            @RequestParam String remainingBudget,
            @RequestParam String location) {

        logger.info("Received attractions request with eventType: " + eventType + ", eventDate: " + eventDate + ", guestCount: " + guestCount + ", remainingBudget: " + remainingBudget + ", location: " + location);

        String response;
        try {
            response = openAIService.getSuggestedAttractions(eventType, eventDate, guestCount, remainingBudget, location);
            logger.info("Response from OpenAI (attractions): " + response);
        } catch (Exception e) {
            logger.severe("Failed to get response from OpenAI: " + e.getMessage());
            response = getFallbackAttractions(Float.parseFloat(remainingBudget),location);
        }

        List<Item> items = createItemsFromString(response,location);

        for (Item item : items) {
            Attraction attraction = new Attraction(item.getName(), "Attraction", item.getPrice(), item.getDescription(), item.getLocation());
            persist.addItem(attraction);
        }

        return response;
    }



    private String getFallbackAttractions(float remainingBudget, String location) {
        List<Item> items = persist.getItemsByCategoryAndBudget("Attraction", remainingBudget,location);
        StringBuilder response = new StringBuilder();
        for (Item item : items) {
            response.append(item.getName()).append(" - ").append(item.getDescription()).append(" - Estimated price: $").append(item.getPrice()).append("\n");
        }
        return response.toString();
    }

    @GetMapping("get-three-attractions")
    public String getThreeAttractions(int remainingBudget, String location) {
        List<Item> items = persist.getItemsByCategoryAndBudget("Attraction", remainingBudget, location);
        StringBuilder response = new StringBuilder();
        for (int i = 0; i < 3 && i < items.size(); i++) {
            Item item = items.get(i);
            response.append(item.getName()).append(" - ").append(item.getDescription());
            if (item.getPrice() > 0) {
                response.append(" - Estimated price: $").append(item.getPrice());
            }
            response.append("\n");
        }
        return response.toString();
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


    private List<Item> createItemsFromString(String itemsString, String location) {
        List<Item> items = new ArrayList<>();
        try {
            String[] lines = itemsString.split("\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(" - Estimated price: ");
                if (parts.length < 2) {
                    continue; // Skip lines that don't match the expected format
                }

                String[] nameAndDescription = parts[0].split(" - ");
                if (nameAndDescription.length < 2) {
                    continue; // Skip lines that don't match the expected format
                }

                String name = nameAndDescription[0].substring(nameAndDescription[0].indexOf('.') + 1).trim();
                String description = nameAndDescription[1].trim();
                String priceString = parts[1].replace("USD", "").replace("$", "").replace(".","").trim();
                float price = Float.parseFloat(priceString);

                items.add(new Item(name, "Estimated Price", price, description, location));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    private List<Item> createItemsFromChatResponse(String itemsString, String location) {
        List<Item> items = new ArrayList<>();
        try {
            String[] lines = itemsString.split("\n");
            for (String line : lines) {
                if (line.trim().isEmpty() || !line.trim().startsWith("Option")) continue; // דילוג על שורות ריקות או כאלה שלא מתחילות ב-Option

                // בדיקה אם השורה מכילה את המחרוזת "Estimated Price"
                if (!line.contains("Estimated Price")) {
                    logger.severe("Invalid line format, missing 'Estimated Price': " + line);
                    continue;
                }

                // פיצול המבנה ל-Name ותיאור על בסיס " - Estimated Price: "
                String[] detailParts = line.split(" Estimated Price: ");
                if (detailParts.length < 2) {
                    logger.severe("Invalid item format: " + line);
                    continue;
                }

                String nameAndDescription = detailParts[0].trim(); // חלק שמכיל את שם האופציה והתיאור
                String priceString = detailParts[1].replace("$", "").trim(); // חלק שמכיל את המחיר, לאחר הסרת $

                // כאן אנו מטפלים בשם ובתיאור
                String[] nameDescParts = nameAndDescription.split(": ", 2);
                if (nameDescParts.length < 2) {
                    logger.severe("Invalid name/description format: " + line);
                    continue;
                }

                String name = nameDescParts[1].trim();
                String description = "";

                // טיפול במחיר והמרה למספר float
                float price = Float.parseFloat(priceString);

                // יצירת אובייקט Item והוספתו לרשימה
                items.add(new Item(name, "Event Addition", price, description, location));
            }
        } catch (Exception e) {
            logger.severe("Failed to create Items from string: " + e.getMessage());
        }
        return items;
    }



}