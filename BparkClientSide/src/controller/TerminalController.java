package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import request.LoginRequest;
import request.PasswordResetRequest;
import response.PasswordResetResponse;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import clientSide.ChatClient;
import entities.Subscriber;

public class TerminalController implements BaseController {

	final public static String SCAN_LOG_ID = "211273813";
	final public static String SCAN_LOG_PW = "SUB1005";
	private ChatClient client;
    private final Stack<VBox> navigationStack = new Stack<>();
    private static TerminalController instance;
    private Subscriber currentSubscriber = new Subscriber("000000000","DefultUserPassword");
    private String lastResetType = null; 

 

    // === VBoxes ===
    @FXML private VBox mainMenu, signInForm, spotsView,
            selectServicePane, pickupPane,signInChoice,dropoffMethod,forgotView,insertreservationcode,
            scannerPane,forgotParkingCodeView;

    // === Buttons ===
    @FXML private Button signInButton, showSpotsButton,
            dropoffButton, pickupButton, submitButton, submitPickupCodeButton,
            backDropoffButton, backInsertCodeButton, backButton, exitButton,btnsignbyhand,signViaScanner,
            btnYesReservation,btnNoReservation,submitdropoffCodeButton,btnScan,sendParkingCodeButton;

    // === Labels ===
    @FXML private Label welcomeLabelTerminal, chooseServiceLabel, dropoffCarLabel, pickupCarLabel, insertCodeLabel,
    LogOutLabel,resetMessage,errorMessageLabel,parkingCodeMessage, loginlabel;
    

    // === Input Fields ===
    @FXML private TextField idField, parkingCodeField,resetEmailField,reservationCodeField,parkingCodeEmailField;
    @FXML private PasswordField codeField;

    // === Text Areas and Texts ===
    @FXML private TextArea parkingCodeTextArea,spotsTextArea;
    @FXML private Text leaveCarText, parkingDelayText;

    // === Hyperlinks ===
    @FXML private Hyperlink forgotPasswordLink;

    @Override
    public void setClient(ChatClient client) {
        this.client = client;
    }

    @FXML
    private void initialize() {
        showOnly(mainMenu);
    }

    /**
     * Displays only the specified VBox pane while hiding and unmanaging all others.
     * This method is used to switch between different screens or views in the terminal interface.
     * It ensures that only one pane is visible and interactive at a time by:
     * Setting all known panes to setVisible(false) and setManaged(false).
     * Setting the target pane to setVisible(true) and  setManaged(true).
     * @param target The  VBox pane to show.
     */
    private void showOnly(VBox target) {
        for (VBox pane : new VBox[]{mainMenu,signInChoice,signInForm, spotsView, selectServicePane, pickupPane,dropoffMethod,forgotView,insertreservationcode,scannerPane,forgotParkingCodeView}) {
            if (pane != null) {
                pane.setVisible(false);
                pane.setManaged(false);
            }
        }
        target.setVisible(true);
        target.setManaged(true);
    }

    /**
     * Navigates to a specified  VBox pane while saving the current visible pane
     * to the  navigationStack for back navigation.
     * This method enables forward navigation in the terminal interface. It first identifies
     * which pane is currently visible, pushes it onto the stack, and then shows the target pane.
     * This allows the user to return to the previous screen via the  handleBack() method.
     *
     * @param next The  VBox pane to navigate to.
     */
    private void navigateTo(VBox next) {
        for (VBox pane : new VBox[]{mainMenu,signInChoice, signInForm, spotsView, selectServicePane, pickupPane,dropoffMethod,forgotView,insertreservationcode,scannerPane,forgotParkingCodeView}) {
            if (pane != null && pane.isVisible()) {
                navigationStack.push(pane);
                break;
            }
        }
        showOnly(next);
    }
    
    public TerminalController() {
        instance = this;
    }

    public static TerminalController getInstance() {
        return instance;
    }

    /**
     * Handles the "Sign In" button click event.
     * This method clears the input fields for ID and subscriber code to ensure
     * the form is reset, and then navigates the user to the manual sign-in form.
     * It is typically used when the user chooses to log in manually (not via scanner).
     */
    @FXML
    private void handleSignInClick() {
    	idField.clear();
        codeField.clear();
        navigateTo(signInForm);
    }
    @FXML
    private void handleSignInChoiceClick() {
        navigateTo(signInChoice);
    }
    
    @FXML
    private void handleSignInViaScannerClick() {
        navigateTo(scannerPane);
    }
    
   
    /**
     * Handles the scan button click to simulate subscriber login via scanning.
     * 
     * Randomly selects between a valid subscriber and a fake subscriber for testing purposes.
     * If the valid subscriber is selected, sends a login request to the server using
     * pre-defined login credentials.
     * If the fake subscriber is selected, displays a popup indicating that no subscriber
     * was found and prompts the user to try scanning again or to sign in manually.
     */

    @FXML
    private void handleScanClick() 
    {
    	Subscriber[] subscribers = new Subscriber[2];
        Random random = new Random();
        int index = random.nextInt(2);
        Subscriber realSub = new Subscriber(SCAN_LOG_ID,SCAN_LOG_PW);
        Subscriber fakeSub = new Subscriber("notRealID","notRealPW");
        subscribers[0] = realSub;
        subscribers[1] = fakeSub;
        System.out.println("the index was: " + index);
        if(index == 0 ) 
        {
        	currentSubscriber = realSub;
        	try {
            	client.sendToServer(new LoginRequest(SCAN_LOG_ID, SCAN_LOG_PW, "terminal"));
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
        }
        else 
        {
        	showPopup("No subscriber has been found.\n please try to scan again, or sign in manually");
        	return;
        }  
    }
    /**
     * Handles the user action of requesting to view available parking spots.
     * 
     * This method navigates the UI to the available spots view,
     * and sends a request to the server to retrieve the current list of available parking spots.
     * Any IOException encountered during communication with the server is caught and printed.
     */

    @FXML
    private void handleShowSpotsClick() {
        navigateTo(spotsView);
        try {
			client.sendToServer("REQUEST_AVAILABLE_SPOTS");
		} catch (IOException e) {
			e.printStackTrace();
		}

    }
    /**
     * Handles the login submission process for terminal users.
     *
     * This method reads the user input for ID and subscriber code,
     * validates that both fields are not empty, and then sends a LoginRequest
     * object to the server for authentication. If any field is empty,
     * a popup message is displayed to the user.
     * Any IOException during the server communication is caught and printed.
     */

    @FXML
    private void handleSubmitLogin() {
        String id = idField.getText().trim();
        String code = codeField.getText().trim();
        currentSubscriber = new Subscriber(id,code);
        idField.clear();
        codeField.clear();

        if (id.isEmpty() || code.isEmpty()) {
            showPopup("Please enter both ID and Subscriber Code.");
            return;
        }

        //send login request to the server
        try {
        	client.sendToServer(new LoginRequest(id, code, "terminal"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
   

    /**
     * Handles the login response from the server for the terminal client.
     * 
     * If the login is successful ("TERMINAL_LOGIN_SUCCESS"), clears the navigation stack 
     * and navigates to the service selection screen.
     * If the login fails, displays an error popup indicating invalid credentials.
     *
     * @param response The response string received from the server.
     */

    public void handleLoginResponse(String response) {
        Platform.runLater(() -> {
            if ("TERMINAL_LOGIN_SUCCESS".equals(response)) {
                navigationStack.clear();
                showOnly(selectServicePane);
            } else {
                showPopup("Invalid ID or Subscriber Code.\nTry again.");
            }
        });
    }
    
    

    /**
     * Handles the "Forgot Password" link or button click event.
     * This method clears the ID and code input fields to reset the form,
     * and navigates the user to the password reset view where they can
     * initiate the recovery process via email.
     */
    @FXML
    private void handleShowForgot() {
    	idField.clear();
        codeField.clear();
    	navigateTo(forgotView);
    }
    
    /**
     * Handles the user's choice to start a Dropoff process.
     * 
     * This method checks if the current subscriber already has an active parking.
     * If so, the server will return the appropriate response ("HAS ACTIVE PARKING" or "NO ACTIVE PARKING").
     * The check is performed by sending a command to the server with the subscriber's ID.
     */
    @FXML
    private void handleDropoffMethodChoice() {
        try {
            client.sendToServer("CHECK_IF_ACTIVE_PARKING|" + currentSubscriber.getSubscriber_id());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes the server's response regarding active parking.
     * 
     * If the subscriber already has an active parking, a popup is shown to notify the user.
     * If there is no active parking, the UI navigates to the dropoff method screen.
     * 
     * @param response The response string from the server ("HAS ACTIVE PARKING" or "NO ACTIVE PARKING")
     */
    public void handleActiveParkingResponse(String response) {
        Platform.runLater(() -> {
            if (response.equals("HAS ACTIVE PARKING")) {
                showPopup("There is an active parking under your name already.");
            } else if (response.equals("NO ACTIVE PARKING")) {
                navigateTo(dropoffMethod);
            }
        });
    }

    
    @FXML
    private void handleDropoffYesReserve() {
    	navigateTo(insertreservationcode);
    }

    @FXML
    private void handlePickupClick() {
        navigateTo(pickupPane);
    }
    
    @FXML
    private void handleDropoffClick() {
        try {
            client.sendToServer(currentSubscriber);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void subscriberNotFoundCase()
    {
    	//System.out.println(currentSubscriber.getSubscriber_id());
    	currentSubscriber =  new Subscriber("000000000","DefultUserPassword");
    }
    
    /**
     * Displays the list of available parking spots in the UI.
     * 
     * If no available spots are provided, shows a message indicating no availability.
     * Otherwise, iterates through the list of available spots and displays them
     * in the designated text area.
     *
     * @param availableSpots A list of strings representing the available parking spots.
     */

    public void handleAvailableSpots(List<String> availableSpots) {
        Platform.runLater(() -> {
            StringBuilder builder = new StringBuilder();
            if(availableSpots.isEmpty())
            {
            	spotsTextArea.setText("There are no available parking spots.");
            }
            else
            {
            	for (String spot : availableSpots) {
                    builder.append(spot).append("\n");
                }
                spotsTextArea.setText(builder.toString());
            }
        });
    }


    /**
     * Handles the "Back" button navigation behavior within the terminal client.
     * 
     * If there is a previous screen in the navigation stack, pops back to that screen.
     * Otherwise, if the stack is empty, returns the user to the main welcome screen
     * by loading the MainWelcome.fxml layout and switching the scene.
     * 
     * This method ensures proper screen navigation both for in-session transitions
     * and for returning fully to the application’s main menu.
     */

    @FXML
    private void handleBack() {
        if (!navigationStack.isEmpty()) {
            VBox previous = navigationStack.pop();
            showOnly(previous);
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/MainWelcome.fxml"));
                Parent root = loader.load();
                MainWelcomeController controller = loader.getController();
                controller.showClientSubMenu();
                
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());

                Stage stage = (Stage) backButton.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("BPARK - Welcome");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Handles the "Send Reset" button click by validating the entered email
     * and sending a PasswordResetRequest to the server.
     * If the email field is empty, displays an error message.
     * Any IOException during sendToServer is caught and reported to the user.
     *
     * @throws IllegalArgumentException if the email field is empty (handled by UI feedback)
     */
    @FXML
    private void handleSendReset() {
    	lastResetType = "sub";
        String email = resetEmailField.getText().trim();
        if (email.isEmpty()) {
            resetMessage.setText("Please enter your email.");
            resetMessage.setStyle("-fx-text-fill: red;");
            return;
        }
        try {
          client.sendToServer(new PasswordResetRequest(email,"sub"));
        } catch (IOException e) {
            e.printStackTrace();
            resetMessage.setText("Failed to send request.");
            resetMessage.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleForgotCode() {
    	navigateTo(forgotParkingCodeView);
    }
 
    /**
     * Handles the "Send Parking Code" button click in the "Forgot Parking Code" view.
     * This method validates the entered email address and, if valid,
     * sends a  common.PasswordResetRequest with type  "pcode" to the server.
     * If the email field is empty, an error message is shown to the user.
     * Any  IOException during the sending process is caught and displayed as an error message.
     */
    @FXML
    private void handleSendParkingCode() {
    	lastResetType = "pcode";
       
        String emailInput = parkingCodeEmailField.getText().trim();
        if (emailInput.isEmpty()) {
            parkingCodeMessage.setText("Please enter your email.");
            parkingCodeMessage.setStyle("-fx-text-fill: red;");
            return;
        }
        try {
            client.sendToServer(new PasswordResetRequest(emailInput, "pcode"));
        } catch (IOException e) {
            e.printStackTrace();
            parkingCodeMessage.setText("Failed to send request.");
            parkingCodeMessage.setStyle("-fx-text-fill: red;");
        }
    }


    /**
     * Displays a popup window with a custom message.
     * 
     * This method creates a non-blocking alert dialog centered on the screen,
     * formats the content to fit nicely with wrapping text, and allows the user
     * to close the popup manually.
     *
     * @param message The message string to display inside the popup window.
     */

    public void showPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Notice");
        alert.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        alert.getDialogPane().setStyle(
            "-fx-background-color: linear-gradient(to right, #041958, #0458c0);" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 20;"
        );

        Label content = new Label(message);
        content.setWrapText(true);
        content.setMaxWidth(300);
        content.setMinHeight(100);
        content.setStyle(
            "-fx-text-alignment: center;" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: white;"
        );

        VBox wrapper = new VBox(content);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPrefSize(500, 180);

        alert.getDialogPane().setContent(wrapper);
        alert.showAndWait();
    }
    
    /**
     * Displays a dark-styled popup with a parking code message and a "Copy Code" button.
     * The message is shown in white over a blue gradient background.
     *
     * @param parkingCode the parking code to display and copy (e.g., "BPARK0953")
     */
    public void showCodePopup(String parkingCode) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Notice");

        // Define buttons
        ButtonType copyButton = new ButtonType("Copy Code", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getDialogPane().getButtonTypes().addAll(copyButton, closeButton);

        // Message content
        String message = "Dropoff was successful.\n" +
                         "Your parking code is: " + parkingCode + "\n" +
                         "If you would like to extend your parking time\n" +
                         "by up to 4 additional hours,\n" +
                         "you can do so through the app.";

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(260);
        messageLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: white;" +               
            "-fx-text-alignment: center;"
        );

        VBox vbox = new VBox(messageLabel);
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPrefSize(500, 180);
        alert.getDialogPane().setContent(vbox);

        alert.getDialogPane().setStyle(
            "-fx-background-color: linear-gradient(to right, #041958, #0458c0);" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 20;"
        );

        // Handle copy action
        alert.showAndWait().ifPresent(response -> {
            if (response == copyButton) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(parkingCode);
                clipboard.setContent(content);
            }
        });
    }


    
    
    /**
     * Handles a successful dropoff operation by displaying a confirmation popup.
     * 
     * This method navigates back to the previous screen and shows a popup containing
     * the generated parking code along with instructions for the user regarding
     * possible extension of parking time.
     *
     * @param parkingCode The parking code assigned to the active parking session.
     */

    public void handleSucsessfulParking(String parkingCode) 
    {
    	showOnly(mainMenu);
        navigationStack.clear();
    	//showPopup("Dropoff was successful.\n your parking code is: "+ parkingCode +"\n If you would like to extend your parking time by up to 4 additional hours,\n you can do so through the app. ");
        showCodePopup(parkingCode);
    }
    
    
    /**
     * Handles the submission of a reservation code during the drop-off process.
     * <p>
     * This method retrieves the user-entered reservation code from the input field
     * and sends a CHECK_RESERVATION_CODE request to the server for validation.
     * <p>
     * If the field is empty, a UI popup informs the user to enter a code.
     * Any IOException during sendToServer is caught and reported via a popup message.
     *
     * @throws IllegalArgumentException if the reservation code field is empty (handled by UI feedback)
     */
    @FXML
    private void handleSubmitReservationCode() {
        String reservationCode = reservationCodeField.getText().trim();
        reservationCodeField.clear();
        if (reservationCode.isEmpty()) {
            showPopup("Please enter the reservation code.");
            return;
        }
        
        try {
        	client.sendToServer("ACTIVATE_RESERVATION_CODE|" + reservationCode);
        } catch (IOException e) {
            e.printStackTrace();
            showPopup("Failed to send reservation code to server.");
        }
    }
    
    	
    /**
     * Handles the result of reservation activation received from the server.
     * 
     * This method unifies the UI response into:
     * - Success message for "SUCCESS" result.
     * - Early arrival message for "ARRIVE_EARLY".
     * - General failure message for all other failure scenarios.
     *
     * @param result The result string returned from server ("SUCCESS", "ARRIVE_EARLY", "INVALID_CODE", etc.)
     */
    public void handleReservationActivationResult(String result) {
        Platform.runLater(() -> {
            String cleanResult = result;
            if (result.startsWith("ACTIVATION_RESULT|")) {
                cleanResult = result.substring("ACTIVATION_RESULT|".length());
            }

            if (cleanResult.equals("SUCCESS")) {
                navigateTo(signInChoice);
                showPopup("Dropoff was successful.\nUse your reservation code to pick up your car.\n" +
                          "If you would like to extend your parking time by up to 4 additional hours,\n" +
                          "you can do so through the app.");
            } else if (cleanResult.equals("ARRIVE_EARLY")) {
            	showPopup(
            		    "You may park your car up to 15 minutes before your reserved time based on your existing reservation.\n" +
            		    "If you choose to park earlier based on availability, please note that it is your responsibility to cancel your existing reservation via the app.\n" +
            		    "Failing to do so may result in a fine."
            		);

                navigateTo(mainMenu);
            } else {
                showPopup("There are no active reservations under this code.\n" +
                          "Please try again or press 'Forgot my password'\n" +
                          "to restore your password.");
            }
        });
    }

    
    
    
    /**
     * Handles the submission of a pickup code during the car pickup process.
     * 
     * Retrieves the entered parking code from the input field and sends it to the server
     * for validation using the "CHECK_PICKUP_CODE" protocol message.
     * 
     * If the input field is empty, displays a popup prompting the user to enter the code.
     * If an I/O exception occurs during communication with the server, shows an error popup.
     */

    @FXML
    private void handleSubmitPickupCode() {
    	 String code = parkingCodeField.getText().trim();
    	 parkingCodeField.clear();
         if (code.isEmpty()) {
             showPopup("Please enter the parking code.");
             return;
         }
         
         if (client == null || !client.isConnected()) {
             showPopup("Connection to server lost. Please restart the terminal.");
             return;
         }
         
         try {
        	    client.sendToServer("CHECK_PICKUP_CODE|" + code);
         } catch (IOException e) {
             e.printStackTrace();
             showPopup("Failed to send pickup code to server.");
         }

    }
    /**
     * Handles the result of pickup request received from the server.
     * <p>
     * This method parses the server response, extracts the actual result ("SUCCESS" or "FAILURE"),
     * and displays an appropriate user message via popup window.
     * <p>
     * - If the pickup was successful, notifies the user that the car is on its way.
     * - If the pickup failed, informs the user that the parking code was incorrect.
     *
     * @param result The server response string, expected format: "PICKUP_RESULT|SUCCESS" or "PICKUP_RESULT|FAILURE"
     */
    public void handlePickupResult(String result) {
        Platform.runLater(() -> {
            String cleanResult = result;
            if (result.startsWith("PICKUP_RESULT|")) {
                cleanResult = result.substring("PICKUP_RESULT|".length());
            }
            if (cleanResult.equals("SUCCESS")) {
            	showOnly(mainMenu);
            	 navigationStack.clear();
                showPopup("We're bringing your car now. Please wait at the delivery point.");
            }
            else if(cleanResult.equals("SENT_TOWED_VEHICLE_MSG"))
            {
            	showOnly(mainMenu);
             navigationStack.clear();
            	showPopup("You were late to your pickup.\nYour vehicle is now being returned from the towed vehicle lot.\nThe late fee has been charged to your credit card.");
            } 
            else {
                showPopup("Incorrect parking code. Please try again.");
            }
        });
    }


    public void handleCarAlreadyParked() 
    {
    	handleBack();
    }

    /**
     * Handles the "Exit" button click event in the terminal interface.
     * If the  ChatClient instance exists, it  closes the client connection
     * by calling  client.quit(). Otherwise, it forcibly terminates the application
     * using  System.exit(0).
     * This method ensures proper shutdown behavior for both connected and disconnected states.
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
     * Handles the response from the server after a password or parking code reset request.
     * This method updates the appropriate message label in the UI (either for password reset or
     * parking code reset) based on the  lastResetType field. It runs the UI update
     * on the JavaFX Application Thread using  Platform#runLater(Runnable) to ensure thread safety.
     * If the response indicates success, the label is updated with the success message in green.
     * Otherwise, the label is updated with the error message in red.
     *
     * @param resp The  PasswordResetResponse object received from the server.
     */
    public void handlePasswordResetResponse(PasswordResetResponse resp) {
        Platform.runLater(() -> {
            Label targetLabel;

            if ("pcode".equals(lastResetType)) {
                targetLabel = parkingCodeMessage;
            } else {
                targetLabel = resetMessage;
            }

            if (resp.isSuccess()) {
                targetLabel.setText(resp.getMessage());
                targetLabel.setStyle("-fx-text-fill: green;");
            } else {
                targetLabel.setText(resp.getMessage());
                targetLabel.setStyle("-fx-text-fill: red;");
            }
        });
    }


}