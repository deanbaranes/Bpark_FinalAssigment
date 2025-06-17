package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

/**
 * ServerMain is the main entry point for the server application.
 * It launches the JavaFX GUI defined in serverGUI.fxml.
 */
public class ServerMain extends Application {

    /**
     * JavaFX start method that initializes and displays the server GUI.
     * @param primaryStage The main window of the application..
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("serverGUI.fxml"));
            Parent root = loader.load();

            primaryStage.setTitle("Server");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
            
            System.out.println("[ServerMain] ✅ Starting scheduled tasks...");
            SchedulerTasks.startAll();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The main method to launch the server GUI application.
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
