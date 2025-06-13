package client;

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

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import common.LoginRequest;
import common.PasswordResetRequest;
import common.PasswordResetResponse;
import common.Subscriber;

public class TerminalController implements BaseController {

	final public static String SCAN_LOG_ID = "900000001";
	final public static String SCAN_LOG_PW = "carpass";
	private ChatClient client;
    private final Stack<VBox> navigationStack = new Stack<>();
    private static TerminalController instance;
    private Subscriber currentSubscriber = new Subscriber("000000000","DefultUserPassword");
    
    

    // ===== MOCK DATA SECTION =====
    private final String validPickupCode = "AB123";
    // =============================

    // === VBoxes ===
    @FXML private VBox mainMenu, signInForm, spotsView,
            selectServicePane, pickupPane,signInChoice,dropoffMethod,forgotView,insertreservationcode,
            scannerPane;

    // === Buttons ===
    @FXML private Button signInButton, showSpotsButton,
            dropoffButton, pickupButton, submitButton, submitPickupCodeButton,
            backDropoffButton, backInsertCodeButton, backButton, exitButton,btnsignbyhand,signViaScanner,
            btnYesReservation,btnNoReservation,submitdropoffCodeButton,btnScan;

    // === Labels ===
    @FXML private Label chooseServiceLabel, dropoffCarLabel,
            pickupCarLabel, insertCodeLabel, LogOutLabel,resetMessage;

    // === Input Fields ===
    @FXML private TextField idField, parkingCodeField,resetEmailField,reservationCodeField;
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

    private void showOnly(VBox target) {
        for (VBox pane : new VBox[]{mainMenu,signInChoice,signInForm, spotsView, selectServicePane, pickupPane,dropoffMethod,forgotView,insertreservationcode,scannerPane}) {
            if (pane != null) {
                pane.setVisible(false);
                pane.setManaged(false);
            }
        }
        target.setVisible(true);
        target.setManaged(true);
    }

    private void navigateTo(VBox next) {
        for (VBox pane : new VBox[]{mainMenu,signInChoice, signInForm, spotsView, selectServicePane, pickupPane,dropoffMethod,forgotView,insertreservationcode,scannerPane}) {
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
    
    @FXML
    private void handleShowSpotsClick() {
        navigateTo(spotsView);
        try {
			client.sendToServer("REQUEST_AVAILABLE_SPOTS");
		} catch (IOException e) {
			e.printStackTrace();
		}

    }

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
    
    @FXML
    private void handleShowForgot() {
    	navigateTo(forgotView);
    }

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
    
    

    
    @FXML
    private void handleDropoffMethodChoice() {
        navigateTo(dropoffMethod);
    }
    
    @FXML
    private void handleDropoffYesReserve() {
    	navigateTo(insertreservationcode);
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

  
    @FXML
    private void handlePickupClick() {
        navigateTo(pickupPane);
    }

    @FXML
    private void handleSubmitPickupCode() {
    	 String code = parkingCodeField.getText().trim();
         if (code.isEmpty()) {
             showPopup("Please enter the parking code.");
         } else if (code.equals(validPickupCode)) {
             showPopup("Your car is on the way to you.");
         } else {
             showPopup("Incorrect parking code. Please try again.");
         }
    }

    @FXML
    private void handleForgotCode() {
        showPopup("A message with your parking code has been sent to your email and phone.");
    }

    @FXML
    private void handleBack() {
        if (!navigationStack.isEmpty()) {
            VBox previous = navigationStack.pop();
            showOnly(previous);
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWelcome.fxml"));
                Parent root = loader.load();
                MainWelcomeController controller = loader.getController();
                controller.showClientSubMenu();
                Stage stage = (Stage) backButton.getScene().getWindow();
                stage.setScene(new Scene(root));
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
        String email = resetEmailField.getText().trim();
        if (email.isEmpty()) {
            resetMessage.setText("Please enter your email.");
            resetMessage.setStyle("-fx-text-fill: red;");
            return;
        }
        try {
            // שליחת ה־Request לשרת
            client.sendToServer(new PasswordResetRequest(email,"sub"));
        } catch (IOException e) {
            e.printStackTrace();
            resetMessage.setText("Failed to send request.");
            resetMessage.setStyle("-fx-text-fill: red;");
        }
    }

    public void showPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Notice");
        alert.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Label content = new Label(message);
        content.setWrapText(true);
        content.setMaxWidth(300);
        content.setMinHeight(100);
        content.setStyle("-fx-text-alignment: center; -fx-alignment: center; -fx-font-size: 14px;");

        VBox wrapper = new VBox(content);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPrefSize(320, 150);

        alert.getDialogPane().setContent(wrapper);
        alert.showAndWait();
    }
    
    public void handleSucsessfulParking(String parkingCode) 
    {
    	handleBack();
    	showPopup("Dropoff was successful.\n your parking code is: "+ parkingCode +"\n If you would like to extend your parking time by up to 4 additional hours,\n you can do so through the app. ");
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
            client.sendToServer("CHECK_RESERVATION_CODE|" + reservationCode);
        } catch (IOException e) {
            e.printStackTrace();
            showPopup("Failed to send reservation code to server.");
        }
    }
    
    public void handleReservationCodeSuccess(String response) {
        Platform.runLater(() -> showPopup("Dropoff was successful.\nUse your reservation code to pick up your car \n If you would like to extend your parking time by up to 4 additional hours,\n you can do so through the app. "));   

    }

    public void handleReservationCodeFailure(String response) {
        Platform.runLater(() -> showPopup("There are no active reservations under this code.\n Please try again or press 'Forgot my password' \nto restore your password."));
    }


    public void handleCarAlreadyParked() 
    {
    	handleBack();
    	showPopup("Dropoff failed:\nYou already have a car that is parked under your name. ");
    }

    @FXML
    private void handleExit() {
        if (client != null) {
            client.quit();
        } else {
            System.exit(0);
        }
    }  
    
  
    /**
     * Processes the server’s response to a password reset request and updates the UI accordingly.
     * This method must be called on the JavaFX Application Thread; wrapping via Platform.runLater
     * ensures thread safety when modifying UI controls.
     *
     * @param resp the PasswordResetResponse object containing:
     *             
     *               success – true if the reset email was sent successfully, false otherwise
     *            .   message – the feedback text to display to the user
     *             
     */
    public void handlePasswordResetResponse(PasswordResetResponse resp) {
        Platform.runLater(() -> {
            if (resp.isSuccess()) {
                resetMessage.setText(resp.getMessage());
                resetMessage.setStyle("-fx-text-fill: green;");
            } else {
                resetMessage.setText(resp.getMessage());
                resetMessage.setStyle("-fx-text-fill: red;");
            }
        });
    }

} 
