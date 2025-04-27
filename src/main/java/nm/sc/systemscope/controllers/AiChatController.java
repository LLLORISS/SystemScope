package nm.sc.systemscope.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import nm.sc.systemscope.ScopeHardware.ScopeCentralProcessor;
import nm.sc.systemscope.modules.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Controller for managing the AI chat functionality in the application.
 * Handles the creation, sending, and displaying of chat messages, as well as
 * interactions with the AI assistant for responding to user queries.
 */
public class AiChatController extends BaseScopeController {
    @FXML private VBox chatMessages;
    @FXML private TextField chatInput;
    @FXML private ListView<ScopeChat> chatListView;
    @FXML private ScrollPane chatScrollPane;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button sendBtn, analyzeBtn, clearBtn;
    private List<ChatMessage> currentMessages = new ArrayList<>();
    private ScopeChat currentChat;

    /**
     * Initializes the chat controller. This method is automatically called when
     * the controller is loaded and is responsible for setting up the UI and loading
     * chat history.
     */
    @FXML public void initialize() {
        List<ScopeChat> chats = DataStorage.loadChatHistory();
        chatListView.getItems().addAll(chats);

        Platform.runLater(() -> {
            if (chatMessages.getHeight() > chatScrollPane.getHeight()) {
                chatScrollPane.setVvalue(1.0);
            }

            if (this.getScene() != null && this.getScene().getWindow() != null) {
                this.getScene().getWindow().setOnCloseRequest(event -> {
                    if (currentChat != null) {
                        DataStorage.saveChatHistory(currentChat);
                    }
                });
            }

            chatInput.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    sendBtn.fire();
                    event.consume();
                }
            });
        });
    }

    /**
     * Handles the event when a chat is selected from the list.
     * Loads and displays the message history of the selected chat.
     */
    @FXML private void onChatSelected() {
        if (currentChat != null) {
            DataStorage.saveChatHistory(currentChat);
        }

        currentChat = chatListView.getSelectionModel().getSelectedItem();

        if (currentChat != null) {
            List<ChatMessage> messageHistory = currentChat.getMessageHistory();

            chatMessages.getChildren().clear();
            for (ChatMessage message : messageHistory) {
                chatMessages.getChildren().add(createChatMessageContainer(message));
            }

            currentMessages.clear();
            currentMessages.addAll(messageHistory);

            ScopeAIHelper.clearChatHistory();
            ScopeAIHelper.loadAndInitializeModel(currentChat.getMessageHistory());

        }
    }

    /**
     * Creates a container for a chat message that includes the message text and timestamp.
     *
     * @param message The chat message to display.
     * @return A {@link VBox} containing the message text and timestamp.
     */
    private VBox createChatMessageContainer(ChatMessage message) {
        Label messageLabel = createLabel(message.getMessage(), message.getSender());
        Label timeLabel = new Label(message.getTime() != null && !message.getTime().isEmpty() ? message.getTime() : LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.getStyleClass().add("message-time");

        VBox messageContainer = new VBox(messageLabel, timeLabel);
        messageContainer.getStyleClass().add("message-box");
        return messageContainer;
    }

    /**
     * Opens a popup window for creating a new chat.
     */
    @FXML
    public void onCreateChatBtn() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Створити новий чат");

        TextField chatNameField = new TextField();
        chatNameField.setPromptText("Введіть назву");

        Button createButton = new Button("Створити");
        createButton.setOnAction(e -> {
            String chatName = chatNameField.getText();
            if (chatName != null && !chatName.isEmpty()) {
                createChat(chatName);
                popupStage.close();
            } else {
                ScopeAlert alert = new ScopeAlert(Alert.AlertType.ERROR, "Please enter a valid chat name.");
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        });

        VBox layout = new VBox(10);
        layout.getChildren().add(chatNameField);

        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().add(createButton);

        layout.getChildren().add(buttonContainer);

        Scene popupScene = new Scene(layout, 300, 150);
        popupScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/nm/sc/systemscope/CSS/styles.css")).toExternalForm());

        popupStage.setScene(popupScene);

        popupStage.showAndWait();
    }

    /**
     * Sends the user's chat input to the AI assistant for a response.
     */
    @FXML
    public void onSendChat() {
        String input = chatInput.getText().trim();
        if (input.isEmpty()) return;

        if ((currentMessages == null || currentMessages.isEmpty()
                && (chatListView.getItems().isEmpty() || chatListView.getItems() == null))) {
            createChat("Loading...");
            new Thread(() -> {
               String name = ScopeAIHelper.request("Придумай коротку назву чату по цьому запиту: " + input);
               currentChat.setChatName(name);
               Platform.runLater(() -> chatListView.refresh());
            }).start();
        }

        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        Platform.runLater(() -> addMessage(new ChatMessage(input, Sender.user, timestamp)));
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
     * Analyzes the system's current performance and sends the results to the AI assistant.
     */
    @FXML
    public void onAnalyzeData() {
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
     * Clears the chat history of the current chat.
     */
    @FXML
    public void onClearChat() {
        currentChat.clearHistory();
        this.currentMessages.clear();
        chatMessages.getChildren().clear();
        ScopeAIHelper.clearChatHistory();
        DataStorage.clearChatHistory(currentChat);
    }

    /**
     * Creates a label for a message, formatted based on the sender (user or assistant).
     *
     * @param text The text of the message.
     * @param sender The sender of the message.
     * @return A label representing the message.
     */
    public Label createLabel(String text, Sender sender) {
        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add(sender == Sender.user ? "user-message" : "ai-message");
        return messageLabel;
    }

    /**
     * Adds a new message to the current chat and updates the UI.
     *
     * @param message The chat message to add.
     */
    public void addMessage(ChatMessage message) {
        if (currentMessages == null) {
            currentMessages = new ArrayList<>();
        }

        String messageTime = (message.getTime() != null && !message.getTime().isEmpty()) ? message.getTime() : LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        Label messageLabel = createLabel(message.getMessage(), message.getSender());

        Label timeLabel = new Label(messageTime);
        timeLabel.getStyleClass().add("message-time");

        VBox messageContainer = new VBox(messageLabel, timeLabel);
        messageContainer.getStyleClass().add("message-box");

        message.setTime(messageTime);
        currentMessages.add(message);
        currentChat.addMessage(message);
        chatMessages.getChildren().add(messageContainer);
    }

    /**
     * Gathers system information, including CPU and GPU stats.
     *
     * @return A string containing system performance information.
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
     * Shows the loading indicator during requests.
     */
    private void hideOnRequest() {
        Platform.runLater(() -> {
            loadingIndicator.setVisible(true);
            chatInput.setVisible(false);
            sendBtn.setVisible(false);
            analyzeBtn.setVisible(false);
            clearBtn.setVisible(false);
        });
    }

    /**
     * Hides the loading indicator after requests.
     */
    private void showOnResponse() {
        Platform.runLater(() -> {
            loadingIndicator.setVisible(false);
            chatInput.setVisible(true);
            sendBtn.setVisible(true);
            analyzeBtn.setVisible(true);
            clearBtn.setVisible(true);
        });
    }

    /**
     * Creates a new chat with the specified name.
     *
     * @param name The name of the new chat.
     */
    private void createChat(String name) {
        for (ScopeChat chat : chatListView.getItems()) {
            if (chat.getChatName().equals(name)) {
                chatListView.getSelectionModel().select(chat);
                currentChat = chat;
                chatMessages.getChildren().clear();
                return;
            }
        }

        ScopeChat chat = new ScopeChat(name);
        currentChat = chat;

        Platform.runLater(() -> {
            chatListView.getItems().add(chat);
            chatListView.getSelectionModel().select(chat);
            chatMessages.getChildren().clear();
        });
    }
}