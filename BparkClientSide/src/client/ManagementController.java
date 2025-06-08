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

import java.io.IOException;
import java.util.Stack;

import common.LoginManagement;
import common.LoginRequest;
import common.RegisterMemberRequest;

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
    @FXML private Hyperlink btnforgerpassword;
    @FXML private Label usernamelabel, passwordlabel, loginlabel;
    @FXML private Button btnloginsubmit;

    // === Manager Menu ===
    @FXML private Label labelwelcome;
    @FXML private Button btnmemberdetails, btnparkingdetails, btnregisternewmember, btnparkingduration, btnmemberstatusreport, btnback;

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
            parkingDurationView
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

    @FXML
    private void handleBack() {
        if (!navigationStack.isEmpty()) {
            Pane previous = navigationStack.pop();
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
        String vehicle2 = label_vehiclenumber_register2.getText().trim(); 
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
                firstName, lastName, id, email, phone, vehicle,vehicle2, creditCard);

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
    
    /*
    @FXML
    private void handleLoginSubmit() {
        String username = usernametextfield.getText().trim();
        String password = passwordfeild.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showPopup("Please enter both username and password.");
            return;
        }

        // שלח את הנתונים לשרת לבדיקה
        LoginManagement loginData = new LoginManagement(username, password);
        try {
            ChatClient.getClient().sendToServer(loginData);
        } catch (IOException e) {
            e.printStackTrace();
            showPopup("Failed to send login request to server.");
        }
    }

     */

}