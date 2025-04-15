package nm.sc.systemscope.controllers;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import nm.sc.systemscope.interfaces.SceneController;
import nm.sc.systemscope.modules.ScopeTheme;

/**
 * The {@code BaseScopeController} class provides a base implementation of the {@link SceneController} interface.
 * It holds references to the {@link Stage} and {@link Scene} associated with a JavaFX view and provides
 * methods to set and get them. This class is intended to be extended by other controllers that require access
 * to the stage and scene.
 *
 * <p>This class implements the {@link SceneController} interface, ensuring that controllers which extend
 * this class can manage the stage and scene seamlessly.</p>
 */
public class BaseScopeController implements SceneController {
    /** The {@link Stage} associated with this controller. */
    protected Stage stage;
    /** The {@link Scene} associated with this controller. */
    protected Scene scene;
    /** The theme manager responsible for applying styles to the scene. */
    protected ScopeTheme theme;

    /**
     * Sets the {@link Scene} for this controller.
     *
     * @param scene the {@link Scene} to be set
     */
    @Override public void setScene(Scene scene){
        this.scene = scene;
        if (theme == null) {
            theme = new ScopeTheme(scene);
        }
        Platform.runLater(() -> theme.applyTheme());
    }

    /**
     * Sets the {@link Stage} for this controller.
     *
     * @param stage the {@link Stage} to be set
     */
    @Override public void setStage(Stage stage){
        this.stage = stage;
    }

    /**
     * Returns the {@link Scene} associated with this controller.
     *
     * @return the {@link Scene} associated with this controller
     */
    @Override public Scene getScene(){
        return this.scene;
    }

    /**
     * Returns the {@link Stage} associated with this controller.
     *
     * @return the {@link Stage} associated with this controller
     */
    @Override public Stage getStage() {return this.stage; }

    /**
     * Displays the {@link Stage} if it is not already shown.
     */
    @Override public void show() {
        if (stage != null && !stage.isShowing()) {
            this.stage.show();
        }
    }

    /**
     * Closes the {@link Stage} if it is not already closed.
     */

    @Override public void close() {
        if (stage != null) {
            stage.close();
        }
    }
}
