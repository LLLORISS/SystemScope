package nm.sc.systemscope.Controllers;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import nm.sc.systemscope.SystemInformation;

public class SystemScopeController {
    @FXML
    private Label InfoPC;
    @FXML
    private Label CPU;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            InfoPC.setText(SystemInformation.getComputerName());
            CPU.setText(SystemInformation.getProcessorName());
        });

        System.out.println(InfoPC.getText());
    }

    /*@FXML
    public void onExitBtnClicked(){
        System.exit(0);
    }*/
}