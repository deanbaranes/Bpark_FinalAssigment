package client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controller for the main welcome screen.
 * Handles navigation to the appropriate interface based on user role.
 */
public class MainWelcomeController {

    @FXML
    private Button customerButton;

    @FXML
    private Button staffButton;

    /**
     * Called when the customer button is clicked.
     * TODO: Implement customer interface flow.
     */
    @FXML
    private void handleCustomer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("customerMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) customerButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK - Customer Access");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Called when the staff button is clicked.
     * Opens the staff GUI with ChatClient connection.
     */
    @FXML
    private void handleStaff() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("clientGUI.fxml"));
            Parent root = loader.load();

            ClientController controller = loader.getController();
            ChatClient client = new ChatClient("localhost", 5555, controller);
            controller.setClient(client);

            Stage stage = (Stage) staffButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK - Staff Interface");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
