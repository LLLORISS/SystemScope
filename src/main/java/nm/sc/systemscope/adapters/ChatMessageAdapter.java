package nm.sc.systemscope.adapters;

import com.google.gson.*;
import nm.sc.systemscope.modules.ChatMessage;
import nm.sc.systemscope.modules.Sender;

import java.lang.reflect.Type;

/**
 * Adapter class for serializing and deserializing {@link ChatMessage} objects
 * using the Gson library. This class implements the {@link JsonSerializer}
 * and {@link JsonDeserializer} interfaces to customize the JSON conversion
 * process for {@link ChatMessage}.
 * <p>
 * It converts a {@link ChatMessage} object into a JSON representation and
 * reconstructs a {@link ChatMessage} object from a JSON string.
 * </p>
 */
public class ChatMessageAdapter implements JsonSerializer<ChatMessage>, JsonDeserializer<ChatMessage>  {
    /**
     * Serializes a {@link ChatMessage} object into a {@link JsonElement}.
     * <p>
     * This method converts a {@link ChatMessage} to a JSON object with two properties:
     * <ul>
     *     <li>"message" - the message text of the {@link ChatMessage}.</li>
     *     <li>"sender" - the sender of the {@link ChatMessage} as a string.</li>
     * </ul>
     * </p>
     *
     * @param src The {@link ChatMessage} to be serialized.
     * @param typeOfSrc The type of the source object being serialized.
     * @param context The serialization context.
     * @return A {@link JsonElement} representing the {@link ChatMessage}.
     */
    public JsonElement serialize(ChatMessage src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("message", src.getMessage());
        jsonObject.addProperty("sender", src.getSender().toString());
        jsonObject.addProperty("time", src.getTime());
        return jsonObject;
    }

    /**
     * Deserializes a {@link JsonElement} into a {@link ChatMessage} object.
     * <p>
     * This method takes a JSON representation and reconstructs a {@link ChatMessage} object
     * by extracting the "message" and "sender" fields from the JSON.
     * </p>
     *
     * @param json The {@link JsonElement} representing a {@link ChatMessage}.
     * @param typeOfT The type of the object being deserialized.
     * @param context The deserialization context.
     * @return The {@link ChatMessage} object reconstructed from the JSON.
     * @throws JsonParseException If the JSON is not in the expected format.
     */
    @Override
    public ChatMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String message = jsonObject.get("message").getAsString();
        Sender sender = Sender.valueOf(jsonObject.get("sender").getAsString());
        String time = jsonObject.has("time") ? jsonObject.get("time").getAsString() : "??:??";

        return new ChatMessage(message, sender, time);
    }
}
