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
     * Loads the management UI FXML and displays only the login screen section.
     */
    @FXML
    private void handleStaff() {
        try {
            // Load the Managment_UI.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Managment_UI.fxml"));
            Parent root = loader.load();

            // Get the controller to control UI visibility
            ManagmentUIController controller = loader.getController();

            // Show only login screen components
            controller.showLoginScreen();

            // Switch the current stage to the management login screen
            Stage stage = (Stage) staffButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK - Staff Login");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

}
