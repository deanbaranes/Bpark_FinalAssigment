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
import common.Subscriber;

public class TerminalController implements BaseController {

    private ChatClient client;
    private final Stack<VBox> navigationStack = new Stack<>();
    private static TerminalController instance;
    private Subscriber currentSubscriber = new Subscriber("000000000","DefultUserPassword");
    
    // ===== MOCK DATA SECTION =====
    private final String validPickupCode = "AB123";
    // =============================


    // === VBoxes ===
    @FXML private VBox mainMenu, signInForm, spotsView,
            selectServicePane, pickupPane,signInChoice,dropoffMethod;

    // === Buttons ===
    @FXML private Button signInButton, showSpotsButton,
            dropoffButton, pickupButton, submitButton, submitPickupCodeButton,
            backDropoffButton, backInsertCodeButton, backButton, exitButton,btnsignbyhand,signViaScanner,
            btnYesReservation,btnNoReservation;

    // === Labels ===
    @FXML private Label chooseServiceLabel, dropoffCarLabel,
            pickupCarLabel, insertCodeLabel, LogOutLabel;

    // === Input Fields ===
    @FXML private TextField idField, parkingCodeField;
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
        for (VBox pane : new VBox[]{mainMenu,signInChoice,signInForm, spotsView, selectServicePane, pickupPane,dropoffMethod}) {
            if (pane != null) {
                pane.setVisible(false);
                pane.setManaged(false);
            }
        }
        target.setVisible(true);
        target.setManaged(true);
    }

    private void navigateTo(VBox next) {
        for (VBox pane : new VBox[]{mainMenu,signInChoice, signInForm, spotsView, selectServicePane, pickupPane,dropoffMethod}) {
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
    	idField.clear();
        codeField.clear();
        navigateTo(signInForm);
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
    	showPopup("ongoing coding progress ");
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
} 
