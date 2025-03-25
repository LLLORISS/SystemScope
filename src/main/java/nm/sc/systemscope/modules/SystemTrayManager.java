package nm.sc.systemscope.modules;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import nm.sc.systemscope.SystemScopeMain;
import nm.sc.systemscope.controllers.SystemTrayMenuWindowController;
import javafx.scene.Scene;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.util.Objects;
import java.awt.event.MouseEvent;

/**
 * Manages the system tray functionality for the application.
 * Handles adding an icon to the system tray, showing a custom menu window,
 * and preventing the application from fully closing when minimized.
 */
public class SystemTrayManager {
    private static TrayIcon icon;
    private static boolean isTrayIconAdded = false;

    private static Stage menuStage = null;

    /**
     * Adds the application to the system tray.
     * If the system tray is not supported, it prints an error message.
     * Prevents the application from fully exiting when the close button is pressed.
     *
     * @param primaryStage The primary stage of the application.
     */
    public static void addToSystemTray(Stage primaryStage) {
        if (!SystemTray.isSupported()) {
            System.out.println("Системний трей не підтримується!");
            return;
        }

        Platform.setImplicitExit(false);
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            primaryStage.hide();
            showTrayIcon(primaryStage);
        });
    }

    /**
     * Displays an icon in the system tray and sets up event listeners.
     *
     * @param primaryStage The primary stage of the application.
     */
    public static void showTrayIcon(Stage primaryStage) {
        if (isTrayIconAdded) return;

        java.awt.Image image;
        try {
            image = ImageIO.read(Objects.requireNonNull(SystemTrayManager.class.getResource("/nm/sc/systemscope/icons/trayIcon.png")));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        icon = new TrayIcon(image, "SystemScope");
        icon.setImageAutoSize(true);

        icon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Platform.runLater(() -> showMenuWindow(primaryStage, e.getLocationOnScreen()));
                }
            }
        });

        try {
            SystemTray.getSystemTray().add(icon);
            isTrayIconAdded = true;
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows a custom menu window near the system tray icon.
     * If the menu is already open, it brings it to the front instead of opening a new instance.
     *
     * @param primaryStage The primary stage of the application.
     * @param location     The screen coordinates where the menu should appear.
     */
    private static void showMenuWindow(Stage primaryStage, Point location) {
        if (menuStage != null && menuStage.isShowing()) {
            menuStage.setX(location.x);
            menuStage.setY(location.y);
            menuStage.toFront();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(SystemScopeMain.class.getResource("TrayMenu-view.fxml"));
            Parent root = loader.load();

            menuStage = new Stage();
            menuStage.initStyle(StageStyle.UNDECORATED);
            menuStage.setAlwaysOnTop(true);

            menuStage.setX(location.x);
            menuStage.setY(location.y);

            Scene scene = new Scene(root);
            menuStage.setScene(scene);
            menuStage.show();
            SystemTrayMenuWindowController controller = loader.getController();
            controller.initialize(primaryStage, menuStage);

            menuStage.setScene(scene);
            menuStage.show();

            menuStage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    menuStage.close();
                    menuStage = null;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes the tray icon from the system tray.
     */
    public static void removeTrayIcon() {
        if (icon != null) {
            SystemTray.getSystemTray().remove(icon);
            isTrayIconAdded = false;
        }
    }


}
