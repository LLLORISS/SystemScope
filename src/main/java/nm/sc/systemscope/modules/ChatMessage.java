package nm.sc.systemscope.modules;

/**
 * A record that represents a chat message.
 * This class stores the content of the message and the sender information.
 */
public record ChatMessage(String message, Sender sender) {

    /**
     * Gets the content of the chat message.
     *
     * @return The message text.
     */
    public String message() {
        return message;
    }

    /**
     * Gets the sender of the chat message.
     *
     * @return The sender of the message (either USER or AI).
     */
    public Sender sender() {
        return sender;
    }
}