package nm.sc.systemscope.modules;

import javafx.scene.control.Alert;

/**
 * Custom alert class that extends JavaFX's Alert to provide additional functionality.
 * This class allows for more control over the alert's type and text.
 */
public class ScopeAlert extends Alert{
    AlertType type;
    String text;

    /**
     * Constructs a ScopeAlert object with the specified alert type and text.
     * Sets the header text to null for a cleaner alert display.
     *
     * @param alertType The type of alert (e.g., ERROR, INFORMATION, etc.).
     * @param text The text content to be displayed in the alert.
     */
    public ScopeAlert(AlertType alertType, String text) {
        super(alertType, text);

        this.type = alertType;
        this.text = text;
        this.setHeaderText(null);
    }

    /**
     * Gets the text of the alert.
     *
     * @return The text of the alert.
     */
    public String getText(){
        return text;
    }

    /**
     * Sets the text of the alert.
     *
     * @param text The new text to be displayed in the alert.
     */
    public void setText(String text){
        this.text = text;
    }
}
