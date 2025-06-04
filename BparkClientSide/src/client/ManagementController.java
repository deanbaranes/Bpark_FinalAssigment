package client;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.Stack;

public class ManagementController {

    private final Stack<Pane> navigationStack = new Stack<>();

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

    @FXML
    private void initialize() {
        showOnly(loginView);
    }

    private void showOnly(Pane target) {
        for (VBox pane : new VBox[]{loginView, managerMenuView, memberDetailsView, parkingDetailsView, registerMemberView, memberStatusReportView}) {
            if (pane != null) {
                pane.setVisible(false);
                pane.setManaged(false);
            }
        }
        target.setVisible(true);
        target.setManaged(true);
    }

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
    private void handleLoginSubmit() {
        String username = usernametextfield.getText().trim();
        String password = passwordfeild.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Please enter both username and password.");
            return;
        }
        // You can add real authentication here
        labelwelcome.setText("Welcome, Manager " + username);
        navigationStack.clear();
        showOnly(managerMenuView);
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
        navigateTo(memberStatusReportView); // Assuming same view for now
    }

    @FXML
    private void handleBack() {
        if (!navigationStack.isEmpty()) {
        	Pane previous = navigationStack.pop();
            showOnly(previous);
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWelcome.fxml"));
                Parent root = loader.load();
                MainWelcomeController controller = loader.getController();
                controller.showClientSubMenu();
                Stage stage = (Stage) btnback.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("BPARK - Welcome");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notice");
        alert.setContentText(message);
        alert.showAndWait();
    }

}