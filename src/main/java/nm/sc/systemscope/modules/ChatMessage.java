package nm.sc.systemscope.modules;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a chat message with content, sender, and timestamp.
 * This class is used to store individual chat messages in the AI chat system.
 */
public class ChatMessage {

    private final String message;
    private final Sender sender;
    private String time;

    /**
     * Constructs a ChatMessage with the specified text and sender.
     * The current time is used as the message timestamp.
     *
     * @param text The content of the message.
     * @param sender The sender of the message (user or assistant).
     */
    public ChatMessage(String text, Sender sender) {
        this(text, sender, LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    /**
     * Constructs a ChatMessage with the specified text, sender, and timestamp.
     *
     * @param text The content of the message.
     * @param sender The sender of the message (user or assistant).
     * @param time The time when the message was sent, formatted as HH:mm.
     */
    public ChatMessage(String text, Sender sender, String time) {
        this.message = text;
        this.sender = sender;
        this.time = time;
    }

    /**
     * Returns the content of the chat message.
     *
     * @return The text of the chat message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the sender of the chat message.
     *
     * @return The sender of the message (either user or assistant).
     */
    public Sender getSender() {
        return sender;
    }

    /**
     * Returns the time when the message was sent.
     *
     * @return The time the message was sent, formatted as HH:mm.
     */
    public String getTime() {
        return time;
    }

    /**
     * Sets the time of the message.
     *
     * @param time The new time for the message, formatted as HH:mm.
     */
    public void setTime(String time) {
        this.time = time;
    }
}
