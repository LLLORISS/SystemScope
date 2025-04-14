package nm.sc.systemscope.interfaces;

import javafx.scene.Scene;
import javafx.stage.Stage;
/**
 * The {@code SceneController} interface defines methods for managing a JavaFX {@link Stage} and {@link Scene}.
 * Implementing classes should provide functionality to set and get the stage and scene associated with their UI.
 *
 * <p>Any controller that needs to manage a JavaFX stage and scene should implement this interface.</p>
 */
public interface SceneController {
    /**
     * Sets the {@link Scene} for the controller.
     *
     * @param scene the {@link Scene} to be set for the controller
     */
    void setScene(Scene scene);

    /**
     * Sets the {@link Stage} for the controller.
     *
     * @param stage the {@link Stage} to be set for the controller
     */
    void setStage(Stage stage);

    /**
     * Gets the {@link Scene} associated with this controller.
     *
     * @return the {@link Scene} associated with this controller
     */
    Scene getScene();

    /**
     * Shows the associated {@link Stage} if it's not already visible.
     *
     * <p>This method should be used to display the stage to the user.</p>
     */
    void show();

    /**
     * Closes the associated {@link Stage}.
     *
     * <p>This method should be used to close the stage when the application or window should be terminated.</p>
     */
    void close();
}
