package serverSide;

import controller.SchedulerController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jdbc.mysqlConnection;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/serverGUI.fxml"));
            Parent root = loader.load();


            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());
            
            primaryStage.setTitle("Server");
            primaryStage.setScene(scene);
            primaryStage.show();
            
            System.out.println("[ServerMain] Starting scheduled tasks...");
            SchedulerController.startAll();
            

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
