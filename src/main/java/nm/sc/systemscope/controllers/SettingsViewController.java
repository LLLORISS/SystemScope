package nm.sc.systemscope.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import nm.sc.systemscope.modules.DataStorage;
import nm.sc.systemscope.modules.ScopeAlert;
import nm.sc.systemscope.modules.ScopeToast;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code SettingsViewController} class is responsible for managing the settings view of the application.
 * It allows the user to view, edit, and save the configuration settings for the application, such as API keys,
 * model details, and other relevant parameters. The class also includes functionality for unlocking and editing
 * fields, copying values to the clipboard, and saving the configuration settings to persistent storage.
 *
 * <p>This controller interacts with UI elements like {@link TextField}s, {@link TextArea}s, and {@link Button}s,
 * and uses {@link ScopeAlert} to display success or error messages. The configuration data is loaded from and saved
 * to the {@link DataStorage} module.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * SettingsViewController controller = new SettingsViewController();
 * controller.onSaveSettings();
 * }
 * </pre>
 */
public class SettingsViewController extends BaseScopeController{
    @FXML private TextField apiKeyField;
    @FXML private TextField apiUrlField;
    @FXML private TextField modelField;
    @FXML private TextArea modelDescriptionField;

    @FXML private Button unlockApiKeyBtn;
    @FXML private Button unlockApiUrlBtn;
    @FXML private Button unlockModelBtn;
    @FXML private Button unlockDescriptionModelBtn;

    @FXML private Button copyApiKeyBtn;
    @FXML private Button copyApiUrlBtn;
    @FXML private Button copyModelBtn;
    @FXML private Button copyModelDescriptionBtn;

    private boolean apiKeyUnlocked = false;
    private boolean apiUrlUnlocked = false;
    private boolean modelUnlocked = false;
    private boolean modelDescriptionUnlocked = false;


    /**
     * Initializes the settings view with data from the configuration storage.
     * If configuration data is found, it populates the text fields with the corresponding values.
     * It also sets up tooltips for the copy buttons.
     */
    @FXML private void initialize() {
        Map<String, String> cfg = DataStorage.getConfigAI();
        if(!cfg.isEmpty()) {
            Platform.runLater(() -> {
                apiKeyField.setText(cfg.get("API_KEY"));
                apiUrlField.setText(cfg.get("API_URL"));
                modelField.setText(cfg.get("MODEL"));
                modelDescriptionField.setText(cfg.get("MODEL_DESCRIPTION"));
                modelDescriptionField.setWrapText(true);

                Tooltip copyTooltip = new Tooltip("Скопіювати в буфер обміну");
                copyTooltip.setShowDelay(Duration.ZERO);

                copyApiKeyBtn.setTooltip(copyTooltip);
                copyApiUrlBtn.setTooltip(copyTooltip);
                copyModelBtn.setTooltip(copyTooltip);
                copyModelDescriptionBtn.setTooltip(copyTooltip);
            });
        }
    }

    /**
     * Toggles the unlock/lock state for the API Key field and updates the button style accordingly.
     */
    @FXML public void onUnlockApiKey(){
        apiKeyUnlocked = !apiKeyUnlocked;
        Platform.runLater(() -> toggleField(unlockApiKeyBtn, apiKeyField, apiKeyUnlocked));
    }

    /**
     * Toggles the unlock/lock state for the API URL field and updates the button style accordingly.
     */
    @FXML public void onUnlockApiUrl(){
        apiUrlUnlocked = !apiUrlUnlocked;
        Platform.runLater(() -> toggleField(unlockApiUrlBtn, apiUrlField, apiUrlUnlocked));
    }

    /**
     * Toggles the unlock/lock state for the model field and updates the button style accordingly.
     */
    @FXML public void onUnlockModel(){
        modelUnlocked = !modelUnlocked;
        Platform.runLater(() -> toggleField(unlockModelBtn, modelField, modelUnlocked));
    }

    /**
     * Toggles the unlock/lock state for the model description field and updates the button style accordingly.
     */
    @FXML public void onUnlockDescriptionModel(){
        modelDescriptionUnlocked = !modelDescriptionUnlocked;
        Platform.runLater(() -> toggleField(unlockDescriptionModelBtn, modelDescriptionField, modelDescriptionUnlocked));
    }

    /**
     * Copies the API Key to the system clipboard and shows a toast notification.
     *
     * @param event the mouse event triggered by clicking the button
     */
    @FXML public void onCopyApiKey(MouseEvent event){
        copyToClipboard(apiKeyField.getText(), event);
    }

    /**
     * Copies the API URL to the system clipboard and shows a toast notification.
     *
     * @param event the mouse event triggered by clicking the button
     */
    @FXML public void onCopyApiUrl(MouseEvent event){
        copyToClipboard(apiUrlField.getText(), event);
    }

    /**
     * Copies the model to the system clipboard and shows a toast notification.
     *
     * @param event the mouse event triggered by clicking the button
     */
    @FXML public void onCopyModel(MouseEvent event){
        copyToClipboard(modelField.getText(), event);
    }

    /**
     * Copies the model description to the system clipboard and shows a toast notification.
     *
     * @param event the mouse event triggered by clicking the button
     */
    @FXML public void onCopyModelDescription(MouseEvent event){
        copyToClipboard(modelDescriptionField.getText(), event);
    }

    /**
     * Saves the configuration settings to the storage, and displays a success or error alert.
     */
    @FXML public void onSaveSettings(){
        Map<String, String> cfg = new HashMap<>();

        String apiKeyText = apiKeyField.getText();
        String apiUrlText = apiUrlField.getText();
        String modelText = modelField.getText();
        String modelDescription = modelDescriptionField.getText();

        cfg.put("API_KEY", apiKeyText);
        cfg.put("API_URL", apiUrlText);
        cfg.put("MODEL", modelText);
        cfg.put("MODEL_DESCRIPTION", modelDescription);


        if(DataStorage.saveConfigAI(cfg)){
            ScopeAlert alert = new ScopeAlert(Alert.AlertType.INFORMATION, "Дані успішно збережені");
            alert.show();
        }
        else{
            ScopeAlert alert = new ScopeAlert(Alert.AlertType.ERROR, "Помилка збереження конфігурації");
            alert.show();
        }
    }

    /**
     * Toggles the unlock/lock state of a field (either {@link TextField} or {@link TextArea}),
     * and updates the button style to indicate the current state (locked or unlocked).
     *
     * @param btn the button associated with the field
     * @param field the field (either {@link TextField} or {@link TextArea}) to be toggled
     * @param unlocked the new state of the field (true if unlocked, false if locked)
     */
    private void toggleField(Button btn, TextField field, boolean unlocked) {
        field.setEditable(unlocked);
        btn.getStyleClass().removeAll("unlockButton", "lockButton");
        btn.setStyle("");
        btn.applyCss();
        btn.getStyleClass().add(unlocked ? "lockButton" : "unlockButton");
        btn.setText(unlocked ? "❌" : "✅");
    }

    /**
     * Toggles the unlock/lock state of a field (either {@link TextField} or {@link TextArea}),
     * and updates the button style to indicate the current state (locked or unlocked).
     *
     * @param btn the button associated with the field
     * @param field the field (either {@link TextField} or {@link TextArea}) to be toggled
     * @param unlocked the new state of the field (true if unlocked, false if locked)
     */
    private void toggleField(Button btn, TextArea field, boolean unlocked) {
        field.setEditable(unlocked);
        btn.getStyleClass().removeAll("unlockButton", "lockButton");
        btn.setStyle("");
        btn.applyCss();
        btn.getStyleClass().add(unlocked ? "lockButton" : "unlockButton");
        btn.setText(unlocked ? "❌" : "✅");
    }

    /**
     * Copies the provided text to the system clipboard and shows a toast notification near the provided mouse event.
     *
     * @param text the text to copy to the clipboard
     * @param event the mouse event triggered by the copy action
     */
    private void copyToClipboard(String text, MouseEvent event){
        if (text == null) return;
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);

        double x = event.getScreenX();
        double y = event.getScreenY();

        ScopeToast.show(apiKeyField.getScene().getWindow(), "Дані збережено", 1500, x, y);
    }

}
