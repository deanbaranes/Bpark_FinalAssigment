package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import request.LoginManagementRequest;
import request.MemberStatusReportRequest;
import request.ParkingDurationRequest;
import request.PasswordResetRequest;
import request.RegisterMemberRequest;
import response.DailySubscriberCount;
import response.ParkingDurationRecord;
import response.PasswordResetResponse;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.util.List;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Stack;

import clientSide.ChatClient;
import entities.ActiveParking;
import entities.Reservation;




public class ManagementController implements BaseController{

    private final Stack<Pane> navigationStack = new Stack<>();
    private ChatClient client;
    private static ManagementController instance;
    private String currentRole;
    private String currentUsername;



    // === VBoxes (screens) ===
    @FXML private VBox loginView;
    @FXML private VBox managerMenuView;
    @FXML private VBox memberDetailsView;
    @FXML private VBox parkingDetailsView;
    @FXML private VBox registerMemberView;
    @FXML private VBox memberStatusReportView;
    @FXML private VBox siteActivityChartsView;
    @FXML private VBox parkingDurationView;

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
    @FXML private TextField parkingDurationYearField;
    @FXML private TextField parkingDurationMonthField;

    // === Register New Member ===
    @FXML private Label label_register_member;
    @FXML private TextField textfield_creditcard,label_vehiclenumber_register2,textfield_firstname, textfield_lastname, textfield_id1, textfield_email, textfiled_phonenumber, label_vehiclenumber_register;
    @FXML private Button btnsignup;

 // === Site Activity View ===
    @FXML private VBox siteActivityView;
    @FXML private Button btnViewSiteActivity;
    @FXML private ScrollPane scrollSiteActivity;
    @FXML private TextArea console_siteactivity_reservations;
    @FXML private TextArea console_siteactivity_activeparkings;

    
 // === Member Status Report View ===
    @FXML private Label label_memberStatusTitle;
    @FXML private TextField statusReportYearField, statusReportMonthField;
    @FXML private Button btnSearchStatusReport;
    @FXML private BarChart<String, Number> memberStatusBarChart;
   
    // === Parking Duration Report View ===
    @FXML private Label label_parkingDurationTitle;
    @FXML private Button btnSearchParkingDuration;
    @FXML private BarChart<String, Number> parkingDurationBarChart;

    @FXML private Button btnexit;
    
    
    /**
     * Sets the ChatClient instance used for communication with the server.
     * This method is typically called after controller initialization.
     *
     * @param client The ChatClient instance to associate with this controller.
     */
    @Override
    public void setClient(ChatClient client) {
        this.client = client;
    }
    
    /**
     * Initializes the controller after its root element has been completely processed.
     * Sets initial visibility for views and hides charts by default.
     * Called automatically by the JavaFX framework.
     */
    @FXML
    private void initialize() {
        instance = this;
        showOnly(loginView);
        parkingDurationBarChart.setVisible(false);
        parkingDurationBarChart.setManaged(false);
        memberStatusBarChart.setVisible(false);
        memberStatusBarChart.setManaged(false);

    }
    
    /**
     * Returns the singleton instance of the ManagementController.
     * This allows access to the controller from other parts of the application.
     *
     * @return the current ManagementController instance.
     */
    public static ManagementController getInstance() {
        return instance;
    }


    /**
     * Hides all available VBox views and makes only the specified target view visible and managed.
     * This method is used to control screen navigation in the management interface.
     *
     * @param target The VBox to be shown on screen.
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
            siteActivityView,    
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

    
    /**
     * Navigates to the specified screen by hiding the current visible view
     * and pushing it onto the navigation stack for future back-navigation.
     *
     * @param next The VBox screen to navigate to.
     */

    private void navigateTo(VBox next) {
        for (VBox pane : new VBox[]{loginView, managerMenuView, memberDetailsView, parkingDetailsView, registerMemberView,siteActivityView ,memberStatusReportView,parkingDetailsView }) {
            if (pane != null && pane.isVisible()) {
                navigationStack.push(pane);
                break;
            }
        }
        showOnly(next);
    }

    
    /**
     * Navigates directly to the login screen.
     * Clears the navigation stack to reset the navigation history.
     */
    public void showLoginScreen() {
        navigationStack.clear();
        showOnly(loginView);
    }
    
    /**
     * Handles the exit button click.
     * If the ChatClient is connected, it performs a graceful quit.
     * Otherwise, it exits the application directly.
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
     * Handles navigation to the Member Status Report view.
     * Clears and hides the current bar chart to prepare for refreshed data.
     */
    @FXML
    private void handleViewSiteActivity() {
        navigateTo(siteActivityView);
        try {
            client.sendToServer("GET_SITE_ACTIVITY");
        } catch (IOException e) {
            showPopup("Failed to fetch site activity.");
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the action of navigating to the Member Status Report view.
     * Clears and hides the bar chart to prepare for new data.
     */
    @FXML
    private void handleViewMemberStatusReport() {
    	memberStatusBarChart.getData().clear();
    	memberStatusBarChart.setVisible(false);   
    	memberStatusBarChart.setManaged(false);  
        navigateTo(memberStatusReportView);
    }

    
    /**
     * Handles the action of navigating to the Parking Duration Report view.
     * Clears and hides the bar chart to prepare for updated report data.
     */
    @FXML
    private void handleViewParkingDuration() {
    	parkingDurationBarChart.getData().clear();
        parkingDurationBarChart.setVisible(false);   
        parkingDurationBarChart.setManaged(false);  
        navigateTo(parkingDurationView);
        
    }
    
    /**
     * Handles the back button click from the charts view.
     * Returns to the Site Activity view.
     */
    @FXML
    private void handleBackFromCharts() {
        navigateTo(siteActivityView);
    }

    /**
     * Navigates to the Member Details screen from the Manager Menu.
     */

    @FXML
    private void handleViewMemberDetails() {
        navigateTo(memberDetailsView);
    }

    
    /**
     * Navigates to the Parking Details screen from the Manager Menu.
     */

    @FXML
    private void handleViewParkingDetails() {
        navigateTo(parkingDetailsView);
    }

    
    /**
     * Navigates to the Register New Member screen from the Manager Menu.
     */
    @FXML
    private void handleRegisterNewMember() {
        navigateTo(registerMemberView);
    }

    /**
     * Navigates to the Forgot Password screen from the Login screen.
     */

    /**
     * Navigates to the Forgot Password screen.
     */
    @FXML
    private void handleShowForgot() {
    	usernametextfield.clear();   
        passwordfeild.clear();
        showOnly(forgotView);
    }
    
    /**
     * Returns the TextArea used to display parking details in the management UI.
     * This is typically used to show search results or status messages related to active parking.
     *
     * @return The TextArea displaying parking details.
     */
    public TextArea getConsoleParkingDetails() {
        return console_parkingdetails;
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

        

    /**
     * Handles the "Back" button action.
     * 
     * This method determines the current visible view and navigates accordingly:
     * - If on the Forgot Password screen: clears fields and returns to the login screen.
     * - If a navigation history exists: returns to the previous screen and clears any input fields if applicable.
     * - If in the manager menu: returns to the login screen and clears login fields.
     * - If in the login screen: loads the main welcome screen from FXML.
     * - Otherwise (fallback): also loads the main welcome screen from FXML.
     */
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
            if (memberStatusReportView.isVisible()) {
            	statusReportYearField.clear();
            	statusReportMonthField.clear();
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/mainWelcome.fxml"));
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/mainWelcome.fxml"));
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
            showPopup("Please enter both username and password.");
            return;
        }
        currentUsername = username;
        LoginManagementRequest loginData = new LoginManagementRequest(username, password);

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
     * Otherwise, a popup alert notifies the user of the specific error..
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
            
            btnViewSiteActivity.setVisible(true);   // Manager-only feature
            btnViewSiteActivity.setManaged(true);

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

            btnViewSiteActivity.setVisible(false); 
            btnViewSiteActivity.setManaged(false);
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
                labelwelcome.setText("Welcome, " + currentRole + " " + capitalize(currentUsername) + "!"); 
                navigationStack.clear();
                showOnly(managerMenuView);
            } else {
                showPopup("Invalid Username or Password.");
                
            }
        });
    }
    
    /**
     * Capitalizes the first letter of a given string for cosmetic display purposes.
     *
     * @param str The input string to format.
     * @return A string with the first character in uppercase and the rest in lowercase.
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
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

    
    /**
     * Displays site activity data including future reservations and active parkings
     * in a structured text format within the management interface.
     *
     * @param future List of future Reservation records.
     * @param active List of currently active ActiveParking records.
     */
    public void displaySiteActivity(List<Reservation> future, List<ActiveParking> active) {
        StringBuilder reservations = new StringBuilder();

        reservations.append("=== Future Reservations ===\n");
        if (future.isEmpty()) {
        	reservations.append("No future reservations found.\n");
        } else {
            for (Reservation r : future) {
            	reservations.append(String.format("Subscriber ID: %s\nParking Code: %s\nStart: %s to %s\n\n",
                    r.getSubscriberId(), r.getParkingCode(), r.getEntryDate(), r.getExitDate()));
            }
        }

        StringBuilder active_parkings = new StringBuilder();
        active_parkings.append("\n=== Active Parkings ===\n");
        if (active.isEmpty()) {
        	active_parkings.append("No active parkings found.\n");
        } else {
            for (ActiveParking a : active) {
            	active_parkings.append(String.format("Parking Code: %s\nSubscriber ID: %s\nEntry Time: %s\n\n",
                    a.getParkingCode(), a.getSubscriberId(), a.getEntryTime()));
            }
        }

        Platform.runLater(() -> {
            console_siteactivity_activeparkings.setText(active_parkings.toString());
            console_siteactivity_reservations.setText(reservations.toString());
        });
    }

    /**
     * Triggered when the Search button is clicked on the Parking Duration screen.
     * Validates the input fields and sends a ParkingDurationRequest to the server
     * with the selected year and month as integers.
     */
    @FXML
    private void handleSearchParkingDuration() {
        String yearStr = parkingDurationYearField.getText().trim();
        String monthStr = parkingDurationMonthField.getText().trim();

        if (yearStr.isEmpty() || monthStr.isEmpty()) {
        	parkingDurationBarChart.setVisible(false);
            showPopup("Please enter both year and month.");
            return;
        }

        try {
            int year = Integer.parseInt(yearStr);
            int month = Integer.parseInt(monthStr);
            
            if (year < 2025) {
            	parkingDurationBarChart.setVisible(false);
                showPopup("Year must be 2025 or later.");
                return;
            }
            if (month < 1 || month > 12) {
            	parkingDurationBarChart.setVisible(false);
                showPopup("Month must be between 1 and 12.");
                return;
            }

            LocalDate today = LocalDate.now();
            YearMonth inputMonth = YearMonth.of(year, month);
            LocalDate lastDayOfInput = inputMonth.atEndOfMonth();

            if (!today.isAfter(lastDayOfInput)) {
                // Clear existing chart and input fields
            	parkingDurationBarChart.setVisible(false);
                parkingDurationYearField.clear();
                parkingDurationMonthField.clear();
                
                showPopup("The report for this month is not yet available.");
                return;
            }
            
            ParkingDurationRequest request = new ParkingDurationRequest(year, month);
            client.sendToServer(request);
        } catch (NumberFormatException e) {
        	parkingDurationBarChart.setVisible(false);
            showPopup("Invalid input: Year and Month must be numbers.");
        } catch (Exception e) {
            e.printStackTrace();
            showPopup("Failed to send request.");
        }
    }

    /**
     * Displays a stacked bar chart showing actual parking time,
     * late time, and extended time per day of the month.
     * Durations are converted from minutes to hours with one decimal precision.
     *
     * @param records List of ParkingDurationRecord containing daily parking metrics
     */
    @SuppressWarnings("unchecked")
    public void displayParkingDurationBarChart(List<ParkingDurationRecord> records) {
        if (records == null) {
            System.err.println("[ERROR] records is null in displayParkingDurationBarChart");
            return;
        }
    	
        Platform.runLater(() -> {
            parkingDurationBarChart.setVisible(true);
            parkingDurationBarChart.setManaged(true);
            parkingDurationBarChart.getData().clear();

            XYChart.Series<String, Number> actualSeries = new XYChart.Series<>();
            actualSeries.setName("Actual Duration (hours)");

            XYChart.Series<String, Number> lateSeries = new XYChart.Series<>();
            lateSeries.setName("Late Duration (hours)");

            XYChart.Series<String, Number> extendedSeries = new XYChart.Series<>();
            extendedSeries.setName("Extended Duration (hours)");

            double maxHours = 0;
            for (ParkingDurationRecord record : records) {
            	System.out.println("Day: " + record.getDayOfMonth() +
                        ", Actual: " + record.getDuration() +
                        ", Late: " + record.getLateDuration() +
                        ", Extended: " + record.getExtendedDuration());

                String label = String.valueOf(record.getDayOfMonth());

                double actualHours = Math.round(record.getDuration() / 60.0 * 10.0) / 10.0;
                double lateHours = Math.round(record.getLateDuration() / 60.0 * 10.0) / 10.0;
                double extendedHours = Math.round(record.getExtendedDuration() / 60.0 * 10.0) / 10.0;

                actualSeries.getData().add(new XYChart.Data<>(label, actualHours));
                lateSeries.getData().add(new XYChart.Data<>(label, lateHours));
                extendedSeries.getData().add(new XYChart.Data<>(label, extendedHours));

                double total = actualHours + lateHours + extendedHours;
                if (total > maxHours) {
                    maxHours = total;
                }
            }

            parkingDurationBarChart.getData().addAll(actualSeries, lateSeries, extendedSeries);

            int upperBound = (int) Math.ceil(maxHours + 1);

            NumberAxis yAxis = (NumberAxis) parkingDurationBarChart.getYAxis();
            yAxis.setTickUnit(1);
            yAxis.setMinorTickVisible(false);
            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(0);
            yAxis.setUpperBound(upperBound);
        });
    }


    /**
     * Triggered when the Search button is clicked on the Member Status Report screen.
     * Validates the input fields and sends a MemberStatusReportRequest to the server
     * with the selected year and month as integers.
     */
    @FXML
    private void handleSearchMemberStatusReport() {
        String yearStr = statusReportYearField.getText().trim();
        String monthStr = statusReportMonthField.getText().trim();

        if (yearStr.isEmpty() || monthStr.isEmpty()) {
        	memberStatusBarChart.setVisible(false);
            showPopup("Please enter both year and month.");
            return;
        }

        try {
            int year = Integer.parseInt(yearStr);
            int month = Integer.parseInt(monthStr);
            
            if (year < 2025) {
            	memberStatusBarChart.setVisible(false);
                showPopup("Year must be 2025 or later.");
                return;
            }
            if (month < 1 || month > 12) {
            	memberStatusBarChart.setVisible(false);
                showPopup("Month must be between 1 and 12.");
                return;
            }
            
            LocalDate today = LocalDate.now();
            YearMonth inputMonth = YearMonth.of(year, month);
            LocalDate lastDayOfInput = inputMonth.atEndOfMonth();

            if (!today.isAfter(lastDayOfInput)) {
                // Clear existing chart and input fields
            	memberStatusBarChart.setVisible(false);
                statusReportYearField.clear();
                statusReportMonthField.clear();
                
                showPopup("The report for this month is not yet available.");
                return;
            }

            MemberStatusReportRequest request = new MemberStatusReportRequest(year, month);
            client.sendToServer(request);
        } catch (NumberFormatException e) {
        	memberStatusBarChart.setVisible(false);
            showPopup("Invalid input: Year and Month must be numbers.");
        } catch (Exception e) {
            e.printStackTrace();
            showPopup("Failed to send report request.");
        }
    }

    /**
     * Displays a bar chart showing the number of subscribers who parked each day of the month.
     *
     * @param records List of DailySubscriberCount representing how many subscribers parked per day
     */
    public void displayMemberStatusBarChart(List<DailySubscriberCount> records) {
        Platform.runLater(() -> {
            memberStatusBarChart.setVisible(true);
            memberStatusBarChart.setManaged(true);
            memberStatusBarChart.getData().clear();

            XYChart.Series<String, Number> memberSeries = new XYChart.Series<>();
            memberSeries.setName("Subscribers per Day");

            int maxCount = 0;

            for (DailySubscriberCount record : records) {
                String dayLabel = String.valueOf(record.getDay());
                int count = record.getSubscriberCount();
                memberSeries.getData().add(new XYChart.Data<>(dayLabel, count));
                if (count > maxCount) {
                    maxCount = count;
                }
            }

            memberStatusBarChart.getData().add(memberSeries);

            
            NumberAxis yAxis = (NumberAxis) memberStatusBarChart.getYAxis();
            yAxis.setTickUnit(1);
            yAxis.setMinorTickVisible(false);
            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(0);
            yAxis.setUpperBound(maxCount + 1); 
        });
    }


}
    

