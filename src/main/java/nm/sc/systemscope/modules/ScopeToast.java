package nm.sc.systemscope.modules;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

/**
 * The {@code ScopeToast} class provides a utility to display a toast notification
 * in the form of a popup message that fades in and out. The message is shown for a
 * specified duration before automatically fading out.
 *
 * <p>This class is designed to display non-intrusive, temporary messages on the UI
 * (such as a confirmation or information message) for a short period.</p>
 */
public class ScopeToast {
    /**
     * Displays a toast notification in the form of a popup with a message that fades in
     * and out over a specified duration.
     *
     * <p>The toast is displayed at the specified coordinates on the given owner window.
     * After the fade-in transition, the toast remains visible for the given duration,
     * after which it fades out automatically.</p>
     *
     * @param ownerWindow the window that owns the toast (used to display the toast relative to this window)
     * @param message the message to be displayed in the toast
     * @param durationMillis the duration in milliseconds for which the toast will remain visible before fading out
     * @param x the x-coordinate where the toast will be displayed on the screen
     * @param y the y-coordinate where the toast will be displayed on the screen
     */
    public static void show(Window ownerWindow, String message, int durationMillis, double x, double y) {
        Popup popup = new Popup();

        Label label = new Label(message);
        label.setStyle("-fx-background-color: rgba(60, 60, 60, 0.9); -fx-text-fill: white; -fx-padding: 10px 15px; -fx-background-radius: 8px; -fx-font-size: 13px;");

        label.setOpacity(0);

        StackPane root = new StackPane(label);
        root.setStyle("-fx-padding: 10px;");
        popup.getContent().add(root);

        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);

        popup.show(ownerWindow, x, y);

        FadeTransition fadeIn = getFadeTransition(durationMillis, label, popup);
        fadeIn.play();
    }

    /**
     * Returns a {@link FadeTransition} that makes the toast fade in and out.
     *
     * <p>This transition will fade the label in over 200 milliseconds, wait for the
     * specified duration, and then fade it out over 300 milliseconds before hiding the popup.</p>
     *
     * @param durationMillis the duration in milliseconds for which the toast will remain visible
     * @param label the label containing the toast message
     * @param popup the popup containing the toast
     * @return a {@link FadeTransition} that controls the fade-in and fade-out effects
     */
    @NotNull
    private static FadeTransition getFadeTransition(int durationMillis, Label label, Popup popup) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), label);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> new Thread(() -> {
            try {
                Thread.sleep(durationMillis);
            } catch (InterruptedException ignored) { }
            Platform.runLater(() -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), label);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(evt -> popup.hide());
                fadeOut.play();
            });
        }).start());
        return fadeIn;
    }
}
