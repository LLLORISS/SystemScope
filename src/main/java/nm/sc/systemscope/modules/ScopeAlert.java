package nm.sc.systemscope.modules;

import javafx.scene.control.Alert;

public class ScopeAlert extends Alert{
    AlertType type;
    String text;

    public ScopeAlert(AlertType alertType, String text) {
        super(alertType, text);

        this.type = alertType;
        this.text = text;
        this.setHeaderText(null);
    }

    public AlertType getType(){
        return type;
    }

    public String getText(){
        return text;
    }

    public void setType(AlertType type){
        this.type = type;
    }

    public void setText(String text){
        this.text = text;
    }
}
