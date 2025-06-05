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

public class ManagementController implements BaseController{

    private final Stack<Pane> navigationStack = new Stack<>();
    private ChatClient client;
    private static ManagementController instance;


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

    // === Register New Member ===
    @FXML private Label label_register_member;
    @FXML private TextField textfield_firstname, textfield_lastname, textfield_id1, textfield_emil, textfiled_phonenumber, label_vehiclenumber_register;
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
            showOnly(previous);
        } else if (managerMenuView.isVisible()) {
            // We're in manager menu, go back to login
            loginView.setVisible(true);
            loginView.setManaged(true);
            managerMenuView.setVisible(false);
            managerMenuView.setManaged(false);
        } else 
        {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWelcome.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) btnback.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("BPARK - Welcome");
            } 
            catch (IOException e) 
            {
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

    private void showPopup(String message) {
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
    
    /*
    Handles the response for management login; shows manager menu on success or error popup on failure.
    */

    public void handleLoginManagementResponse(String response) {
        Platform.runLater(() -> {
            if ("LOGIN_Management_SUCCESS".equals(response)) {
                navigationStack.clear();
                showOnly(managerMenuView);
            } else {
                showPopup("Invalid Username or Password .");
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