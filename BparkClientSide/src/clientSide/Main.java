package clientSide;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

/**
 * Main class for launching the JavaFX client application of the BPARK system.
 * This class initializes the JavaFX runtime and displays the main welcome screen
 * (role selection). The connection to the server is handled later, depending on the selected role.
 */
public class Main extends Application {

    /**
     * Starts the JavaFX application by loading the main welcome screen.
     * This method sets up the primary stage, loads the FXML layout for the welcome screen,
     * applies the application's CSS theme, and displays the window.
     * @param primaryStage the main stage provided by the JavaFX runtime.
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the main welcome screen (role selection)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/mainWelcome.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Welcome to BPARK");
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Failed to load welcome screen:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * The application's main entry point.
     * This method is required to launch the JavaFX application. It delegates control to
     * the JavaFX runtime, which will then call the start(Stage) method.
     * @param args command-line arguments passed to the application (unused).
     */
    public static void main(String[] args) {
        launch(args);
    }
}