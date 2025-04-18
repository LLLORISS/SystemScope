package nm.sc.systemscope.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a chat in the system, including its message history, ID, and name.
 * <p>
 * This class provides methods to manage chat messages, generate unique chat IDs,
 * and retrieve or modify the chat's metadata (name and ID).
 * </p>
 */
public class ScopeChat {
    private List<ChatMessage> messageHistory;
    private String chatID, chatName;

    /**
     * Constructs an empty {@link ScopeChat} with a generated unique chat ID.
     * <p>
     * The chat's message history is initialized as an empty list, and the chat ID
     * is generated using {@link #generateChatID()}.
     * </p>
     */
    public ScopeChat(){
        chatID = generateChatID();
        messageHistory = new ArrayList<>();
    }

    /**
     * Constructs a {@link ScopeChat} with a specified name and a generated unique chat ID.
     * <p>
     * The message history is initialized as an empty list, and the chat ID is
     * generated using {@link #generateChatID()}.
     * </p>
     *
     * @param chatName The name of the chat.
     */
    public ScopeChat(String chatName){
        this.chatName = chatName;
        messageHistory = new ArrayList<>();
        this.chatID = generateChatID();
    }

    /**
     * Generates a unique chat ID using {@link UUID#randomUUID()}.
     * <p>
     * This method generates a random, universally unique identifier (UUID) which
     * is used as the chat's unique ID.
     * </p>
     *
     * @return A unique chat ID in string format.
     */
    private String generateChatID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Returns the list of chat messages in the message history.
     *
     * @return A list of {@link ChatMessage} objects representing the chat's message history.
     */
    public List<ChatMessage> getMessageHistory() {
        return messageHistory;
    }

    /**
     * Sets the chat's message history to the provided list of messages.
     *
     * @param messageHistory A list of {@link ChatMessage} objects to set as the message history.
     */
    public void setMessageHistory(List<ChatMessage> messageHistory) {
        this.messageHistory = messageHistory;
    }

    /**
     * Adds a message to the chat's message history.
     *
     * @param message The {@link ChatMessage} to be added to the chat's history.
     */
    public void addMessage(ChatMessage message) {
        this.messageHistory.add(message);
    }

    /**
     * Clears the chat's message history.
     * <p>
     * This method removes all messages from the chat's history, effectively resetting it.
     * </p>
     */
    public void clearHistory() {
        this.messageHistory.clear();
    }

    /**
     * Sets the chat's unique identifier (ID).
     *
     * @param chatID The unique chat ID to set.
     */
    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    /**
     * Returns the chat's unique identifier (ID).
     *
     * @return The unique chat ID.
     */
    public String getChatID() {
        return chatID;
    }

    /**
     * Returns the chat's name.
     *
     * @return The name of the chat.
     */
    public String getChatName() {
        return chatName;
    }

    /**
     * Sets the chat's name.
     *
     * @param chatName The name to set for the chat.
     */
    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    /**
     * Returns a string representation of the {@link ScopeChat} object.
     * <p>
     * This method returns the name of the chat.
     * </p>
     *
     * @return A string representing the chat's name.
     */
    @Override public String toString(){
        return chatName;
    }
}
