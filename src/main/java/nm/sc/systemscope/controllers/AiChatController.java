package nm.sc.systemscope.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import nm.sc.systemscope.ScopeHardware.ScopeCentralProcessor;
import nm.sc.systemscope.modules.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    private List<ChatMessage> currentMessages = new ArrayList<>();
    private List<ChatMessage> loadedMessages = new ArrayList<>();

    /**
     * Initializes the controller by loading chat history and setting up event handlers.
     * If chat history is available, it displays the existing messages in the chat view.
     * Sets up the close event to save chat history.
     */
    @FXML public void initialize(){
        loadedMessages = DataStorage.loadChatHistory();
        if (loadedMessages == null) {
            loadedMessages = new ArrayList<>();
        }

        for(ChatMessage chatMessage : loadedMessages){
            addMessage(chatMessage);
        }

        this.loadedMessages.clear();

        Platform.runLater(() -> {
            if (chatMessages.getHeight() > chatScrollPane.getHeight()) {
                chatScrollPane.setVvalue(1.0);
            }

            if (this.getScene() != null && this.getScene().getWindow() != null) {
                this.getScene().getWindow().setOnCloseRequest(event -> DataStorage.saveChatHistory(currentMessages));
            }

            chatInput.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    sendBtn.fire();
                    event.consume();
                }
            });
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

        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        addMessage(new ChatMessage(input, Sender.user, timestamp));
        chatInput.setText("");

        new Thread(() -> {
            try {
                hideOnRequest();

                String response = ScopeAIHelper.request(input);
                Platform.runLater(() -> addMessage(new ChatMessage(response, Sender.assistant)));
            } catch (Exception e) {
                Platform.runLater(() -> addMessage(new ChatMessage("Сталася помилка під час обробки запиту", Sender.assistant)));
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
        addMessage(new ChatMessage("Проведи аналітику поточних показників системи", Sender.user));
        hideOnRequest();

        new Thread(() -> {
            try {
                String systemInfo = gatherSystemInfo();
                String response = ScopeAIHelper.request(systemInfo);
                Platform.runLater(() -> addMessage(new ChatMessage(response, Sender.assistant)));
            } catch (Exception e) {
                Platform.runLater(() -> addMessage(new ChatMessage("Сталася помилка під час обробки запиту", Sender.assistant)));
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
        this.currentMessages.clear();
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
     * Adds a new message to the chat interface and updates the chat history.
     * The message is wrapped in a container with the corresponding sender's label and time.
     *
     * @param message The message to be added to the chat.
     */
    public void addMessage(ChatMessage message) {
        if (currentMessages == null) {
            currentMessages = new ArrayList<>();
        }

        String messageTime = (message.getTime() != null && !message.getTime().isEmpty()) ? message.getTime() : LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        Label messageLabel = createLabel(message.getMessage(), message.getSender());

        Label timeLabel = new Label(messageTime);
        timeLabel.getStyleClass().add("message-time");

        VBox messageContainer = new VBox();
        messageContainer.getChildren().addAll(messageLabel, timeLabel);
        messageContainer.getStyleClass().add("message-box");

        currentMessages.add(new ChatMessage(message.getMessage(), message.getSender(), messageTime));
        chatMessages.getChildren().add(messageContainer);
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
