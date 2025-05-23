package client;

import common.ChatIF;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller class for handling user interface interactions in the client application.
 * Manages user inputs, button actions, and updates the UI in response to server communication.
 */
public class ClientController implements ChatIF {

    @FXML private Button btnShowOrders;
    @FXML private Button btnUpdateParkingSpace;
    @FXML private Button btnUpdateOrderDate;
    @FXML private Button btnBack;
    @FXML private Button btnSubmitInput;
    @FXML private Button btnExit;
    @FXML private Button btnFirstSubmit;
    @FXML private Button btnBackCheck;
    @FXML private Button btnUpdate;
    @FXML private TextArea consoleArea;
    @FXML private TextField inputField;

    private ChatClient client;

    /**
     * Initializes the controller and logs a welcome message.
     */
    @FXML
    public void initialize() {
        log("> Welcome! Please select an action.");
    }

    /**
     * Displays a message to the user in the console area.
     * @param message The message to display.
     */
    @Override
    public void display(String message) {
        Platform.runLater(() -> consoleArea.appendText("> " + message + "\n"));
    }

    /**
     * Sets the ChatClient instance for communication with the server.
     * @param client The ChatClient object.
     */
    public void setClient(ChatClient client) {
        this.client = client;
        client.setController(this);
    }

    /**
     * Logs a message directly to the console area.
     * @param msg The message to log.
     */
    private void log(String msg) {
        consoleArea.appendText(msg + "\n");
    }

    /**
     * Handles "Show Orders" button click by requesting all orders from the server.
     */
    @FXML
    private void handleShowOrders() {
        client.showOrders();
    }

    @FXML
    private void handleExit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWelcome.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnExit.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Welcome to BPARK");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Handles the initial click to start updating an order.
     * Prepares the UI for order ID input.
     */
    @FXML
    private void handleFirstUpdateClick() {
        client.setCurrentState(MenuState.CHECK_IF_EXISTS);
        updateUIForOrderIdInput();
        log("> Please enter the order ID and press Submit.");
    }

    /**
     * Submits the user-provided order ID to the server.
     */
    @FXML
    private void handleFirstSubmitUpdate() {
        String input = inputField.getText().trim();
        if (input.isEmpty()) {
            log("> Input field is empty.");
            return;
        }
        client.submitOrderId(input);
        inputField.clear();
    }

    /**
     * Handles the "Back" button click to return to the main menu.
     */
    @FXML
    private void handleBackClick() {
        resetToInitialState();
        client.setCurrentState(MenuState.IDLE);
    }

    /**
     * Handles the "Back" button during the order ID check stage.
     */
    @FXML
    private void handleFirstBackClick() {
        resetToInitialState();
        client.setCurrentState(MenuState.IDLE);
    }

    /**
     * Submits the new value for the selected field to the server.
     */
    @FXML
    private void handleSubmitUpdate() {
        String input = inputField.getText().trim();
        if (input.isEmpty()) {
            log("> Input field is empty.");
            return;
        }
        client.submitFieldUpdate(input);
        inputField.clear();
    }

    /**
     * Handles the click to update the parking space field.
     */
    @FXML
    private void handleUpdateParkingSpaceClick() {
        client.selectFieldToUpdate("parking_space");
        prepareInputForField("Parking Space");
    }

    /**
     * Handles the click to update the order date field.
     */
    @FXML
    private void handleUpdateOrderDateClick() {
        client.selectFieldToUpdate("order_date");
        prepareInputForField("Order Date (yyyy-MM-dd)");
    }

    /**
     * Called when an order ID was successfully verified.
     * Updates the UI to allow field selection.
     */
    public void successfulFirstSubmit() {
        if (client.getCurrentState() == MenuState.UPDATE_ORDER_SELECT_FIELD) {
            btnUpdateParkingSpace.setVisible(true);
            btnUpdateOrderDate.setVisible(true);
            btnBack.setVisible(true);
            inputField.setVisible(false);
            btnSubmitInput.setVisible(false);
            btnBackCheck.setVisible(false);
            btnFirstSubmit.setVisible(false);
            inputField.clear();
            log("> Please choose which field to update.");
        }
    }

    /**
     * Prepares the UI for order ID input.
     */
    private void updateUIForOrderIdInput() {
        btnUpdate.setVisible(false);
        btnShowOrders.setVisible(false);
        btnExit.setVisible(false);
        btnBackCheck.setVisible(true);
        inputField.setVisible(true);
        btnFirstSubmit.setVisible(true);
        consoleArea.setText("");
    }

    /**
     * Prepares the UI for input of a new value for a selected field.
     * @param fieldName The name of the field being updated.
     */
    private void prepareInputForField(String fieldName) {
        consoleArea.setText("");
        log("> You chose to update " + fieldName + ".");
        log("> Enter the new value and press Submit.");
        inputField.setVisible(true);
        btnSubmitInput.setVisible(true);
    }

    /**
     * Resets the UI to the initial state of the application.
     */
    private void resetToInitialState() {
        btnUpdate.setVisible(true);
        btnShowOrders.setVisible(true);
        btnExit.setVisible(true);
        btnUpdateParkingSpace.setVisible(false);
        btnUpdateOrderDate.setVisible(false);
        btnBack.setVisible(false);
        btnBackCheck.setVisible(false);
        inputField.setVisible(false);
        btnSubmitInput.setVisible(false);
        btnFirstSubmit.setVisible(false);
        inputField.clear();
        consoleArea.setText("");
        log("> Back to main actions.");
    }
}