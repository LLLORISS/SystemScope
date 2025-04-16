package nm.sc.systemscope.adapters;

import com.google.gson.*;
import nm.sc.systemscope.modules.ChatMessage;
import nm.sc.systemscope.modules.ScopeChat;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for serializing and deserializing {@link ScopeChat} objects using Gson.
 * This class implements both {@link JsonSerializer} and {@link JsonDeserializer} interfaces
 * to convert {@link ScopeChat} objects to JSON and vice versa.
 */
public class ScopeChatAdapter implements JsonSerializer<ScopeChat>, JsonDeserializer<ScopeChat> {

    /**
     * Serializes a {@link ScopeChat} object into its JSON representation.
     *
     * @param scopeChat The {@link ScopeChat} object to be serialized.
     * @param typeOfSrc The type of the source object.
     * @param context The context for the serialization process.
     * @return A {@link JsonElement} representing the serialized {@link ScopeChat}.
     */
    @Override public JsonElement serialize(ScopeChat scopeChat, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        JsonArray messageHistoryArray = new JsonArray();
        for (ChatMessage message : scopeChat.getMessageHistory()) {
            messageHistoryArray.add(context.serialize(message));
        }

        jsonObject.add("messageHistory", messageHistoryArray);

        return jsonObject;
    }

    /**
     * Deserializes a {@link ScopeChat} object from its JSON representation.
     *
     * @param json The JSON element representing the {@link ScopeChat}.
     * @param typeOfT The type of the target object.
     * @param context The context for the deserialization process.
     * @return The deserialized {@link ScopeChat} object.
     * @throws JsonParseException If there is an error during deserialization.
     */
    @Override public ScopeChat deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        JsonArray messageHistoryArray = jsonObject.getAsJsonArray("messageHistory");
        List<ChatMessage> messageHistory = new ArrayList<>();
        for (JsonElement element : messageHistoryArray) {
            messageHistory.add(context.deserialize(element, ChatMessage.class));
        }

        ScopeChat scopeChat = new ScopeChat();
        scopeChat.setMessageHistory(messageHistory);

        return scopeChat;
    }
}
