package nm.sc.systemscope.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javafx.scene.control.TextField;
import nm.sc.systemscope.modules.ChatMessage;
import nm.sc.systemscope.modules.DataStorage;
import nm.sc.systemscope.modules.Sender;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the AI Chat interface.
 * Handles the display of chat messages, user input, and chat history management.
 */
public class AiChatController extends BaseScopeController{
    @FXML private VBox chatMessages;
    @FXML private TextField chatInput;
    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * Initializes the controller by loading chat history and setting up event handlers.
     * If chat history is available, it displays the existing messages in the chat view.
     * Sets up the close event to save chat history.
     */
    @FXML public void initialize(){
        messages = DataStorage.loadChatHistory();
        if (messages == null) {
            messages = new ArrayList<>();
        }

        for(ChatMessage chatMessage : messages){
            Label label = createLabel(chatMessage.message(), chatMessage.sender());

            chatMessages.getChildren().add(label);
        }

        Platform.runLater(() -> this.getScene().getWindow().setOnCloseRequest(event -> DataStorage.saveChatHistory(messages)));
    }

    /**
     * Sends a chat message from the user.
     * The message is added to the chat history and displayed in the chat view.
     */
    @FXML public void onSendChat(){
        addMessage(chatInput.getText(), Sender.USER);
    }

    /**
     * Placeholder method for analyzing data.
     * Currently not implemented.
     */
    @FXML public void onAnalyzeData(){

    }

    /**
     * Clears the chat history and removes all messages from the view.
     * This also clears the stored chat history.
     */
    @FXML public void onClearChat(){
        DataStorage.clearChatHistory();
        this.messages.clear();
        chatMessages.getChildren().clear();
    }

    /**
     * Creates a label representing a chat message.
     * The label's style class is set based on the sender of the message.
     *
     * @param text The text content of the message.
     * @param sender The sender of the message (either user or AI).
     * @return A Label containing the chat message.
     */
    public Label createLabel(String text, Sender sender){
        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add(sender == Sender.USER ? "user-message" : "ai-message");

        return messageLabel;
    }

    /**
     * Adds a new message to the chat history and the view.
     * The message is added to the list of messages and a corresponding label is created and displayed.
     *
     * @param text The text content of the message.
     * @param sender The sender of the message (either user or AI).
     */
    public void addMessage(String text, Sender sender) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        Label messageLabel = createLabel(text, sender);

        messages.add(new ChatMessage(text, sender));
        chatMessages.getChildren().add(messageLabel);
    }

}
