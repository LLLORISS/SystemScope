package nm.sc.systemscope.modules;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * The {@code ScopeAIHelper} class provides methods to interact with an AI model through HTTP requests.
 * It manages the chat history, initializes the model with previous chat messages, and sends requests to
 * the AI service to receive responses based on user prompts.
 *
 * <p>This class is responsible for:
 * - Loading and initializing the model with chat history.
 * - Sending requests to the AI service and handling responses.
 * - Escaping JSON strings for safe transmission over HTTP.
 * - Managing the chat history.
 * </p>
 */
public class ScopeAIHelper {
    private static final String API_KEY = ScopeConfigManager.getAPI_KEY();
    private static final String API_URL = ScopeConfigManager.getAPI_URL();
    private static final String MODEL = ScopeConfigManager.getMODEL();
    private static final String MODEL_DESCRIPTION = ScopeConfigManager.getMODEL_DESCRIPTION();

    private static JsonArray previousChatHistory = new JsonArray();

    /**
     * Loads and initializes the AI model with chat history.
     * This method loads the chat history from data storage and passes it to the model initialization method.
     */
    public static void loadAndInitializeModel(List<ChatMessage> chatHistory) {
        initializeModelWithHistory(chatHistory);
    }

    /**
     * Initializes the AI model with the provided chat history.
     * Adds a system message with the model description and appends each message from the chat history.
     *
     * @param chatHistory A list of previous chat messages to initialize the model with.
     */
    public static void initializeModelWithHistory(List<ChatMessage> chatHistory) {
        previousChatHistory = new JsonArray();

        JsonObject modelIntro = new JsonObject();
        modelIntro.addProperty("role", "system");
        modelIntro.addProperty("content", MODEL_DESCRIPTION);
        previousChatHistory.add(modelIntro);

        for (ChatMessage chatMessage : chatHistory) {
            JsonObject message = new JsonObject();
            message.addProperty("role", chatMessage.getSender().name());
            message.addProperty("content", chatMessage.getMessage());
            previousChatHistory.add(message);
        }
    }

    /**
     * Sends a prompt to the AI model and returns the AI's response.
     * The method builds a request, sends it to the AI service, and processes the response.
     *
     * @param prompt The user-provided prompt for the AI model.
     * @return The AI's response as a string, or an error message if something goes wrong.
     */
    public static String request(String prompt) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", escapeJson(prompt));
            previousChatHistory.add(userMessage);

            String jsonRequest = String.format("""
            {
              "model": "%s",
              "messages": %s
            }
            """, MODEL, previousChatHistory.toString());

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int status = connection.getResponseCode();

            if (status >= 400) {
                var errorStream = connection.getErrorStream();
                if (errorStream == null) {
                    ScopeLogger.logError("API error response: null error stream");
                    return "❌ Error " + status + " - " + checkStatusCode(status);
                }

                try (BufferedReader errReader = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errReader.readLine()) != null) {
                        errorResponse.append(line.trim());
                    }
                    ScopeLogger.logError("API error response: " + errorResponse);
                }

                return "❌ Error " + status + " - " + checkStatusCode(status);
            } else {
                var inputStream = connection.getInputStream();
                if (inputStream == null) {
                    ScopeLogger.logError("API success response, but input stream is null");
                    return "❌ Error: Empty response from AI.";
                }

                StringBuilder response = new StringBuilder();
                try (BufferedReader successReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    String responseLine;
                    while ((responseLine = successReader.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }
                return extractMessage(response.toString());
            }

        } catch (Exception e) {
            ScopeLogger.logError("Error requesting AI: ", e);
            return "❌ " +  checkStatusCode(Integer.parseInt(e.getMessage()));
        }
    }

    /**
     * Extracts the content message from the AI's response.
     *
     * @param json The raw JSON response from the AI service.
     * @return The AI's response content, or an error message if the extraction fails.
     */
    private static String extractMessage(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            JsonArray choices = jsonObject.getAsJsonArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                return message.get("content").getAsString();
            }
        } catch (Exception e) {
            ScopeLogger.logError("Помилка при розборі JSON: ", e);
        }
        return "❌ Не вдалося обробити відповідь від AI.";
    }

    /**
     * Escapes special characters in a JSON string for safe transmission.
     * Replaces double quotes with escaped quotes and newlines with escaped newline characters.
     *
     * @param text The text to be escaped.
     * @return The escaped text.
     */
    private static String escapeJson(String text) {
        return text.replace("\"", "\\\"").replace("\n", "\\n");
    }

    /**
     * Clears the chat history, removing all previous chat messages.
     */
    public static void clearChatHistory(){
        previousChatHistory = new JsonArray();
    }

    /**
     * Checks the status code in the error message and returns an appropriate error message.
     *
     * @param statusCode The error message containing the status code.
     * @return A custom error message based on the status code.
     */
    private static String checkStatusCode(int statusCode) {
        return switch (statusCode) {
            case 400 -> "Bad Request: The request was invalid or cannot be served.";
            case 401 -> "Unauthorized: Authentication failed or user does not have permissions.";
            case 403 -> "Forbidden: You have exceeded the API request limit.";
            case 404 -> "Not Found: The requested resource could not be found.";
            case 500 -> "Internal Server Error: An error occurred on the server.";
            case 502 -> "Bad Gateway: The server was acting as a gateway and received an invalid response.";
            case 503 -> "Service Unavailable: The server is currently unavailable.";
            default -> "Unexpected error occurred.";
        };
    }
}
