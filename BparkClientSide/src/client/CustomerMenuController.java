package client;
 
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class CustomerMenuController {

    @FXML
    private Button remoteButton;

    @FXML
    private Button kioskButton;

    @FXML
    private Button backButton;

    @FXML
    private void handleRemoteAccess() {
        System.out.println("Remote Access selected.");
        // TODO: Load remoteCustomerUI.fxml
    }

    @FXML
    private void handleKioskAccess() {
        System.out.println("On-site Access selected.");
        // TODO: Load kioskCustomerUI.fxml
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWelcome.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Welcome to BPARK");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
