package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.util.List;

import java.io.IOException;
import java.util.Stack;

import common.PasswordResetRequest;
import common.PasswordResetResponse;
import common.LoginManagement;
import common.LoginRequest;
import common.RegisterMemberRequest;
import common.ActiveParking;

public class ManagementController implements BaseController{

    private final Stack<Pane> navigationStack = new Stack<>();
    private ChatClient client;
    private static ManagementController instance;
    private String currentRole;


    // === VBoxes (screens) ===
    @FXML private VBox loginView;
    @FXML private VBox managerMenuView;
    @FXML private VBox memberDetailsView;
    @FXML private VBox parkingDetailsView;
    @FXML private VBox registerMemberView;
    @FXML private VBox memberStatusReportView;

    // === Login ===
    @FXML private TextField usernametextfield;
    @FXML private PasswordField passwordfeild;
    @FXML private Hyperlink btnforgotpassword;
    @FXML private Label usernamelabel, passwordlabel, loginlabel;
    @FXML private Button btnloginsubmit;

    // === Manager Menu ===
    @FXML private Label labelwelcome,resetMessage;
    @FXML private Button btnmemberdetails, btnparkingdetails, btnregisternewmember, btnparkingduration, btnmemberstatusreport, btnback;
    @FXML private VBox     forgotView;
    @FXML private TextField resetEmailField;  

    // === Member Details ===
    @FXML private Label labelmemberdetails, labelsearchby_id;
    @FXML private TextField searchbyidtext;
    @FXML private Button btnsearch_memberdetails;
    @FXML private TextArea console_memberdeatils;

    // === Parking Details ===
    @FXML private Label label_parking_details, label_parkingdetails_search;
    @FXML private TextField searchbytext2;
    @FXML private Button btnsearch_parkingdetails;
    @FXML private TextArea console_parkingdetails;
    @FXML private Label parkingDurationTitle;
    @FXML private VBox parkingDurationView;
    @FXML private TextField parkingDurationYearField;
    @FXML private TextField parkingDurationMonthField;


    // === Register New Member ===
    @FXML private Label label_register_member;
    @FXML private TextField textfield_creditcard,label_vehiclenumber_register2,textfield_firstname, textfield_lastname, textfield_id1, textfield_email, textfiled_phonenumber, label_vehiclenumber_register;
    @FXML private Button btnsignup;

    // === Member Status Report ===
    @FXML private Label label_enteryear, label_entermoth;
    @FXML private TextField label_Enteryear, monthField;
    @FXML private Button btnsearchreport;
    @FXML private Text memberStatusTitle;
    @FXML private javafx.scene.chart.LineChart<?, ?> chart_memberstatus, parking_timechart;

    
    @Override
    public void setClient(ChatClient client) {
        this.client = client;
    }
    
    @FXML
    private void initialize() {
        instance = this;
        showOnly(loginView);
      
    }

    /* Returns the singleton instance of the ManagementController */
    public static ManagementController getInstance() {
        return instance;
    }
    /*
    Hides all VBoxes and shows only the specified target pane by setting its visibility and managed state.
    Used for navigating between different management screens.
    */
    private void showOnly(Pane target) {
        for (VBox pane : new VBox[]{
            loginView,
            managerMenuView,
            memberDetailsView,
            parkingDetailsView,
            registerMemberView,
            memberStatusReportView,
            parkingDurationView,
            forgotView 
        }) {
            if (pane != null) {
                pane.setVisible(false);
                pane.setManaged(false);
            }
        }
        target.setVisible(true);
        target.setManaged(true);
    }

    /*
    Navigates to the specified VBox screen by hiding the current one and pushing it onto the navigation stack for back navigation.
    */

    private void navigateTo(VBox next) {
        for (VBox pane : new VBox[]{loginView, managerMenuView, memberDetailsView, parkingDetailsView, registerMemberView, memberStatusReportView}) {
            if (pane != null && pane.isVisible()) {
                navigationStack.push(pane);
                break;
            }
        }
        showOnly(next);
    }

    public void showLoginScreen() {
        navigationStack.clear();
        showOnly(loginView);
    }
    
    @FXML
    private void handleExit() {
        if (client != null) {
            client.quit();
        } else {
            System.exit(0);
        }
    } 
    
    @FXML
    private void handleViewMemberDetails() {
        navigateTo(memberDetailsView);
    }

    @FXML
    private void handleViewParkingDetails() {
        navigateTo(parkingDetailsView);
    }

 
    @FXML
    private void handleRegisterNewMember() {
        navigateTo(registerMemberView);
    }


    @FXML
    private void handleViewMemberStatusReport() {
        navigateTo(memberStatusReportView);
    }
    

    @FXML
    private void handleViewParkingDuration() {
        navigateTo(parkingDurationView);
    }
    /*
    Handles the "Back" button logic by navigating to the previous screen in the stack or returning to login/main welcome screen if the stack is empty.
    */

    /**
     * Triggered by clicking the "Forgot password?" hyperlink.
     * Switches the UI to the forgotView pane.
     */
    @FXML
    private void handleShowForgot() {
        showOnly(forgotView);
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
            client.sendToServer(new PasswordResetRequest(email,"reserve"));
        } catch (IOException e) {
            e.printStackTrace();
            resetMessage.setText("Failed to send request.");
            resetMessage.setStyle("-fx-text-fill: red;");
        }
    }

        

    
    @FXML
    private void handleBack() {
        // 0. If we're on the Forgot-Password screen, just clear it and go back
        if (forgotView.isVisible()) {
            // Clear the forgot-password fields
            resetEmailField.clear();
            resetMessage.setText("");

            // Show login screen
            showOnly(loginView);
            return;
        }
        if (!navigationStack.isEmpty()) {
            Pane previous = navigationStack.pop();
            if (parkingDetailsView.isVisible()) {
                searchbytext2.clear();               // Clear search input field
                console_parkingdetails.clear();      // Clear result display
            }

            if (memberDetailsView.isVisible()) {
                searchbyidtext.clear();              
                console_memberdeatils.clear();      
            }
            if (parkingDurationView.isVisible()) {
            	parkingDurationYearField.clear();
            	parkingDurationMonthField.clear();

            }
            showOnly(previous);
            
        } else if (managerMenuView.isVisible()) {
            // We're in manager menu, go back to login
            usernametextfield.clear();   
            passwordfeild.clear();
            showOnly(loginView);
            
        } else if (loginView.isVisible()) {
            // We're in login view, go back to mainWelcome.fxml
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWelcome.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) btnback.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("BPARK - Welcome");
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } else {
            // fallback — go to welcome screen anyway
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWelcome.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) btnback.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("BPARK - Welcome");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Handles the click on the 'Search' button in the parking details view.
     * Sends a request to the server to fetch active parking data based on input.
     */
    @FXML
    private void handleSearchParkingDetails() {
        String query = searchbytext2.getText().trim();
        if (query.isEmpty()) {
            console_parkingdetails.setText("Please enter a member number or parking spot.");
            return;
        }

        try {
            client.sendToServer("SEARCH_ACTIVE_PARKING|" + query);
        } catch (IOException e) {
            console_parkingdetails.setText("Error sending request: " + e.getMessage());
        }
    }


    /**
     * Sends a request to the server to retrieve subscriber details based on the entered ID.
     * If the input field is empty, displays a popup asking the user to enter an ID.
     * Constructs a message in the format "REQUEST_ID_DETAILS|<ID>" and sends it to the server.
     * 
     * This method is triggered when the user clicks the "Search" button in the "Member Details" screen.
     */

    @FXML
    private void handleSearchMemberDetails() {
        String id = searchbyidtext.getText().trim();

        if (id.isEmpty()) {
            showPopup("Please enter an ID.");
            return;
        }

        try {
            client.sendToServer("REQUEST_ID_DETAILS|" + id);
        } catch (IOException e) {
            e.printStackTrace();
            showPopup("Failed to send request.");
        }
    }
    
    
    /**
     * Displays subscriber information in the member details text area.
     * Ensures the update runs on the JavaFX Application Thread.
     *
     * @param info The formatted subscriber information string to display.
     */

    public void displaySubscriberInfo(String info) {
        Platform.runLater(() -> {
            console_memberdeatils.setText(info);
        });
    }

    /**
     * Handles the login submission process for management users.
     * Validates input fields and sends a LoginManagement object to the server
     * containing the username and password for authentication.
     */

    @FXML
    private void handleLoginManagementSubmit() {
        String username = usernametextfield.getText().trim();
        String password = passwordfeild.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Please enter both username and password.");
            return;
        }
        LoginManagement loginData = new LoginManagement(username, password);

        try {
        	client.sendToServer(loginData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Displays a styled popup alert with a custom message, centered text, and fixed size for user notifications.
    */

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
    /**
     * Handles the submission of the "Register New Member" form.
     * 
     * This method performs full client-side validation before sending the request to the server:
     * - All fields must be filled
     * - First and last names must contain only letters
     * - ID must be exactly 9 digits
     * - Phone number must be exactly 10 digits
     * - Credit card must be exactly 16 digits
     *
     * If validation passes, a RegisterMemberRequest object is created and sent to the server.
     * Otherwise, a popup alert notifies the user of the specific error.
     */
    @FXML
    private void handleSubmitNewMember() {
        String firstName = textfield_firstname.getText().trim();
        String lastName = textfield_lastname.getText().trim();
        String id = textfield_id1.getText().trim();
        String email = textfield_email.getText().trim();
        String phone = textfiled_phonenumber.getText().trim();
        String vehicle = label_vehiclenumber_register.getText().trim();
        String creditCard = textfield_creditcard.getText().trim();
        
        if (firstName.isEmpty() || lastName.isEmpty() || id.isEmpty()
                || email.isEmpty() || phone.isEmpty() || vehicle.isEmpty()
                || creditCard.isEmpty()) {
            showPopup("Please fill in all the fields.");
            return;
        }


        if (!firstName.matches("[a-zA-Z]+")) {
            showPopup("First name must contain only letters.");
            return;
        }

        if (!lastName.matches("[a-zA-Z]+")) {
            showPopup("Last name must contain only letters.");
            return;
        }

        if (!id.matches("\\d{9}")) {
            showPopup("ID must be exactly 9 digits.");
            return;
        }

        if (!phone.matches("\\d{10}")) {
            showPopup("Phone number must be exactly 10 digits.");
            return;
        }

        if (!creditCard.matches("\\d{16}")) {
            showPopup("Credit card must be exactly 16 digits.");
            return;
        }

        
        RegisterMemberRequest request = new RegisterMemberRequest(
                firstName, lastName, id, email, phone, vehicle, creditCard);

        try {
            client.sendToServer(request);
        } catch (IOException e) {
            e.printStackTrace();
            showPopup("Failed to send registration request.");
        }
    }
    
    /**
     * Adjusts the UI elements in the manager menu view based on the user's role.
     * Hides or shows buttons according to whether the user is a 'manager' or 'attendant'.
     */
    private void configureRoleBasedAccess() {
        if ("manager".equals(currentRole)) {
            // Hide register button
            btnregisternewmember.setVisible(false);
            btnregisternewmember.setManaged(false);

            // Show manager-only reports
            btnparkingduration.setVisible(true); 
            btnparkingduration.setManaged(true);

            btnmemberstatusreport.setVisible(true);
            btnmemberstatusreport.setManaged(true);

            // Show shared buttons
            btnmemberdetails.setVisible(true);
            btnmemberdetails.setManaged(true);
            
            btnparkingdetails.setVisible(true);
            btnparkingdetails.setManaged(true);

        } else {
            // Show register button for attendant
            btnregisternewmember.setVisible(true);
            btnregisternewmember.setManaged(true);

            // Hide reports
            btnparkingduration.setVisible(false); 
            btnparkingduration.setManaged(false);

            btnmemberstatusreport.setVisible(false);
            btnmemberstatusreport.setManaged(false);

            // Show shared buttons
            btnmemberdetails.setVisible(true);
            btnmemberdetails.setManaged(true);
            
            btnparkingdetails.setVisible(true);
            btnparkingdetails.setManaged(true);
        }
    }
    
    
    /**
     * Clears all input fields in the "Register New Member" form.
     */
    public void clearRegisterMemberForm() {
        textfield_firstname.clear();
        textfield_lastname.clear();
        textfield_id1.clear();
        textfield_email.clear();
        textfiled_phonenumber.clear();
        label_vehiclenumber_register.clear();
        textfield_creditcard.clear();
    }
    
    /**
     * Handles the response from the server after a management user attempts to log in.
     * Parses the response to determine whether the login was successful and which role
     * (e.g., "manager" or "attendant") the user has. Based on the role, it configures 
     * the UI appropriately and displays the management menu view.
     *
     * Expected response format:
     * - "LOGIN_Management_SUCCESS|manager"
     * - "LOGIN_Management_SUCCESS|attendant"
     * - or simply "LOGIN_Management_FAILURE"
     *
     * @param response the response string received from the server
     */
    public void handleLoginManagementResponse(String response) {
        Platform.runLater(() -> {
            if (response.startsWith("LOGIN_Management_SUCCESS")) {
                String[] parts = response.split("\\|");
                currentRole = (parts.length > 1) ? parts[1].toLowerCase() : "attendant";

                configureRoleBasedAccess(); // adjust UI based on role
                navigationStack.clear();
                showOnly(managerMenuView);
            } else {
                showPopup("Invalid Username or Password.");
                
            }
        });
    }
    /* 
    Displays an informational alert popup with the provided message. 
    */

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notice");
        alert.setContentText(message);
        alert.showAndWait();
    }
    /**
     * Displays a detailed list of active parking records in a structured format.
     * Each record is shown as a multi-line list for readability.
     *
     * @param records The list of matching active parking records.
     */
    public void displayActiveParkingDetails(List<ActiveParking> records) {
        // Handle empty result
        if (records == null || records.isEmpty()) {
            console_parkingdetails.setText("No active parking records found for the given member number / parking number.");
            return;
        }

        // Use StringBuilder for efficient output construction
        StringBuilder sb = new StringBuilder();

        // Loop through all records
        for (ActiveParking rec : records) {
            sb.append("Parking Code: ").append(rec.getParkingCode()).append("\n");
            sb.append("Subscriber ID: ").append(rec.getSubscriberId()).append("\n");
            sb.append("Entry Date: ").append(rec.getEntryDate()).append("\n");
            sb.append("Entry Time: ").append(rec.getEntryTime()).append("\n");
            sb.append("Expected Exit Date: ").append(rec.getExpectedExitDate()).append("\n");
            sb.append("Expected Exit Time: ").append(rec.getExpectedExitTime()).append("\n");
            sb.append("Parking Spot: ").append(rec.getParkingSpot()).append("\n");
            sb.append("Extended: ").append(rec.isExtended() ? "Yes" : "No").append("\n");
            sb.append("----------------------------------------------------\n");
        }

        // Set the formatted string to the TextArea
        console_parkingdetails.setText(sb.toString());
    }

    
    /**
     * Processes the server’s response to a password reset request and updates the UI accordingly.
     * This method must be called on the JavaFX Application Thread; wrapping via Platform.runLater
     * ensures thread safety when modifying UI controls.
     *
     * @param resp the PasswordResetResponse object containing:
     *             
     *               success – true if the reset email was sent successfully, false otherwise
     *               message – the feedback text to display to the user
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
    


