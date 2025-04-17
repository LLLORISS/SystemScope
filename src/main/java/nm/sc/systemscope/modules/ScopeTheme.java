package nm.sc.systemscope.modules;

import javafx.scene.Scene;
import java.util.Objects;

/**
 * A class responsible for managing and applying the theme (dark or light) to the JavaFX scene.
 * The theme is loaded from the configuration and applied to the current scene's stylesheet.
 */
public class ScopeTheme {
    private Theme theme;
    private Scene scene;

    /**
     * Constructor that initializes the theme from the configuration and assigns it to the provided JavaFX scene.
     *
     * @param scene The JavaFX scene to which the theme will be applied.
     */
    public ScopeTheme(Scene scene){
        this.theme = ScopeConfigManager.getTheme();
        this.scene = scene;
    }

    /**
     * Applies the selected theme's stylesheet to the current scene.
     * If the theme is DARK, the dark stylesheet will be applied.
     * If the theme is LIGHT, the light stylesheet will be applied.
     * The method clears any existing stylesheets before adding the new one.
     * The applied theme is saved to the configuration.
     */
    public void applyTheme(){
        if (this.scene != null) {
            this.scene.getStylesheets().clear();

            String themeStyleFile = "";

            if (theme == Theme.DARK) {
                themeStyleFile = "/nm/sc/systemscope/CSS/styles.css";
            } else if (theme == Theme.LIGHT) {
                themeStyleFile = "/nm/sc/systemscope/CSS/light-styles.css";
            }

            this.scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(themeStyleFile)).toExternalForm());

            ScopeConfigManager.setTheme(theme);
        }
    }

    /**
     * Sets a new theme for the current instance.
     *
     * @param theme The theme to set (either DARK or LIGHT).
     */
    public void setTheme(Theme theme){
        this.theme = theme;
    }

    /**
     * Returns the current JavaFX scene associated with this theme.
     *
     * @return The current JavaFX scene.
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Returns the current theme.
     *
     * @return The current theme (either DARK or LIGHT).
     */
    public Theme getTheme() {
        return theme;
    }

    /**
     * Sets a new JavaFX scene for this theme.
     *
     * @param scene The scene to set.
     */
    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
