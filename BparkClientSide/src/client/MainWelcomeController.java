package client;
 
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Controller for the main welcome screen.
 * Displays initial role selection (Clients or Management),
 * and provides an in-place sub-menu for client access type selection.
 */
public class MainWelcomeController {

    @FXML
    private VBox mainMenu;

    @FXML
    private VBox clientSubMenu;

    @FXML
    private Button clientsButton;

    @FXML
    private Button managementButton;

    @FXML
    private Button exitButton;

    @FXML
    private Button terminalButton;

    @FXML
    private Button appButton;

    @FXML
    private Button backButton;

    private ChatClient client;

    /**
     * Initializes the ChatClient connection when the controller loads.
     */
    @FXML
    public void initialize() {
        try {
            client = new ChatClient("localhost", 5555, null);
            client.openConnection(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Handles the "Clients" button click.
     * Switches view to show the client access options (App / Terminal).
     */
    @FXML
    private void handleClients() {
        mainMenu.setVisible(false);
        mainMenu.setManaged(false);
        clientSubMenu.setVisible(true);
        clientSubMenu.setManaged(true);
    }

    /**
     * Handles the "Back" button in the client sub-menu.
     * Returns to the main menu.
     */
    @FXML
    private void handleBackToMain() {
        clientSubMenu.setVisible(false);
        clientSubMenu.setManaged(false);
        mainMenu.setVisible(true);
        mainMenu.setManaged(true);
    }

    /**
     * Handles the "Management" button click.
     * Loads the management interface and displays the login screen only.
     */
    @FXML
    private void handleManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("managementGUI.fxml"));
            Parent root = loader.load();

            ManagementController controller = loader.getController();
            System.out.println("[DEBUG] client before setClient: " + client);
            controller.setClient(client);
            client.setController(controller);
            controller.showLoginScreen();

            Stage stage = (Stage) managementButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK - Management");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Handles the "App (Remote)" button click.
     * Loads the client application interface.
     */
    @FXML
    private void handleAppAccess() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("clientGUI.fxml"));
            Parent root = loader.load();

            BaseController controller = loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) appButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK - Client App");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the "Terminal" button click.
     * Loads the terminal interface.
     */
    @FXML
    private void handleTerminalAccess() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("terminalGUI.fxml"));
            Parent root = loader.load();

            BaseController controller = loader.getController();
            controller.setClient(client);

            Stage stage = (Stage) terminalButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("BPARK - Terminal");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the "Exit" button click.
     * Closes the application safely.
     */
    @FXML
    private void handleExit() {
        if (client != null) {
            client.quit();
        } else {
            System.exit(0);
        }
    }

    /**
     * Shows the client sub-menu (Terminal / App) again,
     * used when returning from clientGUI.fxml.
     */
    public void showClientSubMenu() {
        mainMenu.setVisible(false);
        mainMenu.setManaged(false);
        clientSubMenu.setVisible(true);
        clientSubMenu.setManaged(true);
    }

    
}
