package nm.sc.systemscope.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import nm.sc.systemscope.ScopeHardware.ScopeCentralProcessor;
import nm.sc.systemscope.modules.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the AI Chat interface.
 * Handles the display of chat messages, user input, and chat history management.
 * This controller allows the user to send messages, receive AI responses, and analyze system data.
 */
public class AiChatController extends BaseScopeController{
    @FXML private VBox chatMessages;
    @FXML private TextField chatInput;
    @FXML private ScrollPane chatScrollPane;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button sendBtn;
    @FXML private Button analyzeBtn;
    @FXML private Button clearBtn;

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

        Platform.runLater(() -> {
            if (chatMessages.getHeight() > chatScrollPane.getHeight()) {
                chatScrollPane.setVvalue(1.0);
            }

            if (this.getScene() != null && this.getScene().getWindow() != null) {
                this.getScene().getWindow().setOnCloseRequest(event -> DataStorage.saveChatHistory(messages));
            }
        });

        ScopeAIHelper.loadAndInitializeModel();
    }

    /**
     * Sends a chat message from the user.
     * The message is added to the chat history and displayed in the chat view.
     * It then sends the user's input to the AI for a response.
     */
    @FXML public void onSendChat() {
        String input = chatInput.getText().trim();
        if (input.isEmpty()) return;

        addMessage(input, Sender.user);
        chatInput.setText("");

        new Thread(() -> {
            try {
                hideOnRequest();

                String response = ScopeAIHelper.request(input);
                Platform.runLater(() -> addMessage(response, Sender.assistant));
            } catch (Exception e) {
                Platform.runLater(() -> addMessage("Сталася помилка під час обробки запиту", Sender.assistant));
            } finally {
                showOnResponse();
            }
        }).start();
    }

    /**
     * Placeholder method for analyzing system data.
     * It gathers system information and sends it to the AI for analysis.
     * Currently, this feature is not fully implemented.
     */
    @FXML public void onAnalyzeData() {
        addMessage("Проведи аналітику поточних показників системи", Sender.user);

        String systemInfo = gatherSystemInfo();

       hideOnRequest();

        new Thread(() -> {
            try {
                String response = ScopeAIHelper.request(systemInfo);
                Platform.runLater(() -> addMessage(response, Sender.assistant));
            } catch (Exception e) {
                Platform.runLater(() -> addMessage("Сталася помилка під час обробки запиту", Sender.assistant));
            } finally {
                showOnResponse();
            }
        }).start();
    }


    /**
     * Clears the chat history and removes all messages from the view.
     * This also clears the stored chat history.
     */
    @FXML public void onClearChat(){
        DataStorage.clearChatHistory();
        this.messages.clear();
        chatMessages.getChildren().clear();
        ScopeAIHelper.clearChatHistory();
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
        messageLabel.getStyleClass().add(sender == Sender.user ? "user-message" : "ai-message");

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

    /**
     * Gathers system information, including CPU and GPU statistics,
     * and returns it as a formatted string for analysis by the AI.
     *
     * @return A formatted string containing the system's current CPU and GPU information.
     */
    private String gatherSystemInfo() {
        String CPU = ScopeCentralProcessor.getProcessorName();
        String CPUTemperature = ScopeCentralProcessor.getTemperatureCPU();
        String CPUUsage = ScopeCentralProcessor.getCPUUsage();

        String GPU = SystemInformation.getGraphicCards();
        String GPUTemperature = SystemInformation.getTemperatureGPU();
        String GPUUsage = SystemInformation.getGPUUsage();

        return "Проведи аналітику показників системи:\n" +
                "CPU: " + CPU + "\n" +
                "Температура CPU: " + CPUTemperature + "\n" +
                "Завантаження CPU: " + CPUUsage + "\n" +
                "GPU: " + GPU + "\n" +
                "Температура GPU: " + GPUTemperature + "\n" +
                "Завантаження GPU: " + GPUUsage + "\n";
    }

    /**
     * Hides certain UI elements (loading indicator, chat input field, send button, analyze button, and clear button)
     * while the system is processing a request. This method is executed on the JavaFX application thread
     * to ensure thread safety when manipulating the UI elements.
     */
    private void hideOnRequest(){
        Platform.runLater(() -> {
            loadingIndicator.setVisible(true);
            chatInput.setVisible(false);
            sendBtn.setVisible(false);
            analyzeBtn.setVisible(false);
            clearBtn.setVisible(false);
        });
    }

    /**
     * Shows certain UI elements (loading indicator, chat input field, send button, analyze button, and clear button)
     * after the system has responded to the request. This method is executed on the JavaFX application thread
     * to ensure thread safety when manipulating the UI elements.
     */
    private void showOnResponse(){
        Platform.runLater(() -> {
            loadingIndicator.setVisible(false);
            chatInput.setVisible(true);
            sendBtn.setVisible(true);
            analyzeBtn.setVisible(true);
            clearBtn.setVisible(true);
        });
    }
}
