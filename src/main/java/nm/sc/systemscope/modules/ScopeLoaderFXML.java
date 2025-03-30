package nm.sc.systemscope.modules;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nm.sc.systemscope.SystemScopeMain;
import nm.sc.systemscope.interfaces.SceneController;

import java.io.IOException;

/**
 * Utility class to load and manage an FXML scene and its controller in a JavaFX application.
 * It provides methods to initialize, display, and manipulate the scene and stage associated with a given FXML file.
 * <p>
 * This class is responsible for:
 * <ul>
 *   <li>Loading an FXML file and creating its associated scene and controller.</li>
 *   <li>Managing the stage (window) associated with the scene.</li>
 *   <li>Providing utility methods to display the stage.</li>
 * </ul>
 * </p>
 */
public class ScopeLoaderFXML {
    private Stage stage;
    private Scene scene;
    private final Object controller;

    /**
     * Constructor that initializes the FXMLLoader and loads the FXML file.
     * It also initializes the stage and scene, and if the controller implements {@link SceneController},
     * it sets the scene and stage on the controller.
     *
     * @param pathFXML the path to the FXML file to be loaded.
     * @throws IOException if an error occurs while loading the FXML file.
     */
    public ScopeLoaderFXML(String pathFXML) throws IOException {

        FXMLLoader loader = new FXMLLoader(SystemScopeMain.class.getResource(pathFXML));
        this.scene = new Scene(loader.load());
        this.controller = loader.getController();

        this.stage = new Stage();
        this.stage.setScene(scene);

        if (controller instanceof SceneController) {
            ((SceneController) controller).setScene(scene);
            ((SceneController) controller).setStage(stage);
        }
    }

    /**
     * Displays the stage (window) associated with the loaded FXML scene.
     *
     * @throws IllegalStateException if the stage is not set.
     */
    public void show() {
        if (stage != null) {
            stage.show();
        } else {
            throw new IllegalStateException("Stage is not set!");
        }
    }

    /**
     * Displays the stage (window) and waits for it to close before returning.
     *
     * @throws IllegalStateException if the stage is not set.
     */
    public void showAndWait(){
        if (stage != null) {
            stage.showAndWait();
        } else {
            throw new IllegalStateException("Stage is not set!");
        }
    }

    /**
     * Gets the stage (window) associated with this FXML loader.
     *
     * @return the stage associated with the scene.
     */
    public Stage getStage(){
        return this.stage;
    }

    /**
     * Gets the scene associated with this FXML loader.
     *
     * @return the scene associated with the stage.
     */
    public Scene getScene(){
        return this.scene;
    }

    /**
     * Sets the stage (window) associated with this FXML loader.
     *
     * @param stage the stage to be set.
     */
    public void setStage(Stage stage){
        this.stage = stage;
        stage.setScene(scene);
    }

    /**
     * Sets the scene to be associated with the stage.
     *
     * @param scene the scene to be set.
     */
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    /**
     * Gets the controller associated with this FXML loader.
     *
     * @return the controller associated with the loaded FXML.
     */
    public Object getController(){
        return this.controller;
    }
}
