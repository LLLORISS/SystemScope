package nm.sc.systemscope.modules;

import com.google.gson.annotations.Expose;

/**
 * Represents a chat message in the system.
 * Contains the message text and the sender's identity (either user or assistant).
 *
 * <p>This class is used to store chat messages, and is annotated for JSON serialization/deserialization using Gson.</p>
 */
public record ChatMessage(
        @Expose String message,

        @Expose Sender sender
) {
}
