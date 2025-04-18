package nm.sc.systemscope.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import nm.sc.systemscope.modules.*;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

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
    @FXML private TextField apiKeyField, apiUrlField, modelField;
    @FXML private TextArea modelDescriptionField;

    @FXML private Button unlockApiKeyBtn, unlockApiUrlBtn, unlockModelBtn, unlockDescriptionModelBtn;

    @FXML private Button copyApiKeyBtn, copyApiUrlBtn, copyModelBtn, copyModelDescriptionBtn;

    @FXML private CheckBox saveLogsCheckBox, aiReportCheckBox,
            showCPUTempCheckBox, showCPUUsageCheckBox, showGPUTempCheckBox, showGPUUsageCheckBox;

    @FXML private ToggleButton darkThemeButton, lightThemeButton;

    private boolean apiKeyUnlocked = false, apiUrlUnlocked = false, modelUnlocked = false, modelDescriptionUnlocked = false;

    /**
     * Initializes the settings view with data from the configuration storage.
     * If configuration data is found, it populates the text fields with the corresponding values.
     * It also sets up tooltips for the copy buttons.
     */
    @FXML private void initialize() {
        Platform.runLater(() -> {
            apiKeyField.setText(ScopeConfigManager.getAPI_KEY());
            apiUrlField.setText(ScopeConfigManager.getAPI_URL());
            modelField.setText(ScopeConfigManager.getMODEL());
            modelDescriptionField.setText(ScopeConfigManager.getMODEL_DESCRIPTION());
            modelDescriptionField.setWrapText(true);

            Tooltip copyTooltip = new Tooltip("Скопіювати в буфер обміну");
            copyTooltip.setShowDelay(Duration.ZERO);

            copyApiKeyBtn.setTooltip(copyTooltip);
            copyApiUrlBtn.setTooltip(copyTooltip);
            copyModelBtn.setTooltip(copyTooltip);
            copyModelDescriptionBtn.setTooltip(copyTooltip);

            updateAIReportCheckbox();

            saveLogsCheckBox.setSelected(ScopeConfigManager.isSaveBenchLogs());
            aiReportCheckBox.setSelected(ScopeConfigManager.isGenerateAIReport());
            showCPUTempCheckBox.setSelected(ScopeConfigManager.isShowCPUTemp());
            showCPUUsageCheckBox.setSelected(ScopeConfigManager.isShowCPUUsage());
            showGPUTempCheckBox.setSelected(ScopeConfigManager.isShowGPUTemp());
            showGPUUsageCheckBox.setSelected(ScopeConfigManager.isShowGPUUsage());

            updateCheckBox();

            if(ScopeConfigManager.getTheme() == Theme.DARK){
                Platform.runLater(() -> {
                    darkThemeButton.setSelected(true);
                    lightThemeButton.setSelected(false);
                });
            }
            else{
                Platform.runLater(() -> {
                    lightThemeButton.setSelected(false);
                    darkThemeButton.setSelected(true);
                });
            }

            if(!ScopeConfigManager.isSaveBenchLogs()){
                aiReportCheckBox.setDisable(true);
            }
        });
    }

    private void updateAIReportCheckbox() {
        if(saveLogsCheckBox.isSelected()) {
            boolean isAnyCheckboxSelected = showCPUTempCheckBox.isSelected() ||
                    showCPUUsageCheckBox.isSelected() ||
                    showGPUTempCheckBox.isSelected() ||
                    showGPUUsageCheckBox.isSelected();

            aiReportCheckBox.setDisable(!isAnyCheckboxSelected);
            aiReportCheckBox.setSelected(isAnyCheckboxSelected);
            ScopeConfigManager.swapGenerateAIReport();
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
        String apiKeyText = apiKeyField.getText();
        String apiUrlText = apiUrlField.getText();
        String modelText = modelField.getText();
        String modelDescription = modelDescriptionField.getText();

        ScopeConfigManager.setAPI_KEY(apiKeyText);
        ScopeConfigManager.setAPI_URL(apiUrlText);
        ScopeConfigManager.setMODEL(modelText);
        ScopeConfigManager.setMODEL_DESCRIPTION(modelDescription);

        ScopeAlert alert;
        if(ScopeConfigManager.getSaveStatus()){
            alert = new ScopeAlert(Alert.AlertType.INFORMATION, "Дані успішно збережені");
        }
        else{
            alert = new ScopeAlert(Alert.AlertType.ERROR, "Помилка збереження конфігурації");
        }
        alert.show();
    }

    private void updateCheckBox(){
        if(ScopeConfigManager.isSaveBenchLogs()) {
            Platform.runLater(() -> {
                showCPUTempCheckBox.setDisable(false);
                showCPUTempCheckBox.setSelected(false);
                ScopeConfigManager.setShowCPUTemp(false);

                showGPUTempCheckBox.setDisable(false);
                showGPUTempCheckBox.setSelected(false);
                ScopeConfigManager.setShowGPUTemp(false);

                showCPUUsageCheckBox.setDisable(false);
                showCPUUsageCheckBox.setSelected(false);
                ScopeConfigManager.setShowCPUUsage(false);

                showGPUUsageCheckBox.setDisable(false);
                showGPUUsageCheckBox.setSelected(false);
                ScopeConfigManager.setShowGPUUsage(false);
            });
        }
        else{
            showCPUTempCheckBox.setDisable(true);
            showCPUTempCheckBox.setSelected(false);
            ScopeConfigManager.setShowCPUTemp(false);

            showGPUTempCheckBox.setDisable(true);
            showGPUTempCheckBox.setSelected(false);
            ScopeConfigManager.setShowGPUTemp(false);

            showCPUUsageCheckBox.setDisable(true);
            showCPUUsageCheckBox.setSelected(false);
            ScopeConfigManager.setShowCPUUsage(false);

            showGPUUsageCheckBox.setDisable(true);
            showGPUUsageCheckBox.setSelected(false);
            ScopeConfigManager.setShowGPUUsage(false);
        }
    }

    /**
     * Called when the "Save Logs" checkbox is toggled.
     * Updates the save logs setting and enables/disables the AI report checkbox accordingly.
     */
    @FXML public void onToggleSaveLogs(){
        ScopeConfigManager.swapSaveBenchLogs();
        ScopeConfigManager.swapGenerateAIReport();

        updateCheckBox();

        if(ScopeConfigManager.isGenerateAIReport()) {
            Platform.runLater(() -> {
                aiReportCheckBox.setSelected(false);
                aiReportCheckBox.setDisable(true);
            });
        }
        else{
            Platform.runLater(() -> aiReportCheckBox.setDisable(false));
        }
        updateAIReportCheckbox();
    }


    /**
     * Called when the "AI Report" checkbox is toggled.
     * Updates the configuration to enable or disable AI report generation.
     */
    @FXML public void onToggleAIReport(){
        ScopeConfigManager.swapGenerateAIReport();
    }

    @FXML public void onToggleCPUTemp(){
        updateAIReportCheckbox();
        boolean selected = showCPUTempCheckBox.isSelected();
        ScopeConfigManager.setShowCPUTemp(selected);
    }

    @FXML public void onToggleCPUUsage(){
        updateAIReportCheckbox();
        boolean selected = showCPUUsageCheckBox.isSelected();
        ScopeConfigManager.setShowCPUUsage(selected);
    }

    @FXML public void onToggleGPUTemp(){
        updateAIReportCheckbox();
        boolean selected = showGPUTempCheckBox.isSelected();
        ScopeConfigManager.setShowGPUTemp(selected);
    }

    @FXML public void onToggleGPUUsage(){
        updateAIReportCheckbox();
        boolean selected = showGPUUsageCheckBox.isSelected();
        ScopeConfigManager.setShowGPUUsage(selected);
    }

    /**
     * Switches the application to the dark theme if it is not already selected.
     * Updates the theme setting and applies the dark theme.
     */
    @FXML private void onDarkThemeSelected() {
        if (theme.getTheme() != Theme.DARK) {
            theme.setTheme(Theme.DARK);
            ScopeConfigManager.setTheme(Theme.DARK);
            theme.applyTheme();
        }
        darkThemeButton.setSelected(true);
        lightThemeButton.setSelected(false);
    }

    /**
     * Switches the application to the light theme if it is not already selected.
     * Updates the theme setting and applies the light theme.
     */
    @FXML private void onLightThemeSelected() {
        if (theme.getTheme() != Theme.LIGHT) {
            theme.setTheme(Theme.LIGHT);
            ScopeConfigManager.setTheme(Theme.LIGHT);
            theme.applyTheme();
        }
        lightThemeButton.setSelected(true);
        darkThemeButton.setSelected(false);
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
