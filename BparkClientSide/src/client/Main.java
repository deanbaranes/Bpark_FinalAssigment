package client;

import java.io.IOException;//

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

/**
 * Main class for launching the JavaFX client application.
 * Loads the welcome screen GUI and defers connection logic to next screens.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the main welcome screen (role selection)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWelcome.fxml"));
            Parent root = loader.load();

            primaryStage.setTitle("Welcome to BPARK");
            primaryStage.setScene(new Scene(root));
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
