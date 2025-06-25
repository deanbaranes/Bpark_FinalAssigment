package clientSide;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

/**
 * Main class for launching the JavaFX client application.
 * Loads the welcome screen GUI and defers connection logic to next screens..
 */
public class Main extends Application {

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

    public static void main(String[] args) {
        launch(args);
    }
}