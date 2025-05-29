package client;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Stack;




public class ManagementController {

    @FXML private TextField usernametextfield;
    @FXML private PasswordField passwordfeild;
    @FXML private Button btnloginsubmit;
    @FXML private Hyperlink btnforgerpassword;
    @FXML private Label usernamelabel;
    @FXML private Label passwordlabel;
    @FXML private Label loginlabel;
    @FXML private Label labelwelcome;
    @FXML private Button btnmemberdetails;
    @FXML private Button btnparkingdetails;
    @FXML private Button btnregisternewmember;
    @FXML private Button btnparkingduration;
    @FXML private Button btnmemberstatusreport;
    @FXML private Label labelmemberdetails;
    @FXML private Label labelsearchby_id;
    @FXML private TextField searchbyidtext;
    @FXML private Button btnsearch_memberdetails;
    @FXML private TextArea console_memberdeatils;
    @FXML private Label label_parking_details;
    @FXML private TextArea console_parkingdetails;
    @FXML private Label label_parkingdetails_search;
    @FXML private TextField searchbytext2;
    @FXML private Button btnsearch_parkingdetails;
    @FXML private Label label_register_member;
    @FXML private Label label_firstname;
    @FXML private Label label_lsatname;
    @FXML private Label label_id;
    @FXML private Label label_email;
    @FXML private Label label_phonenumber;
    @FXML private Label label_vehiclenumber;
    @FXML private TextField textfield_firstname;
    @FXML private TextField textfield_lastname;
    @FXML private TextField textfield_id1;
    @FXML private TextField textfield_emil;
    @FXML private TextField textfiled_phonenumber;
    @FXML private TextField label_vehiclenumber_register;
    @FXML private Label label_enteryear;
    @FXML private Label label_entermoth;
    @FXML private TextField label_Enteryear; // Note: check this naming in FXML!
    @FXML private TextField monthField;      // This one is unnamed in your FXML, we’ll rename it here for clarity
    @FXML private javafx.scene.chart.LineChart<?, ?> parking_timechart;
    @FXML private Text memberStatusTitle;
    @FXML private javafx.scene.chart.LineChart<?, ?> chart_memberstatus;
    @FXML private Button btnback;
    @FXML private Button btnbacktomain;

    private Stack<Runnable> screenHistory = new Stack<>();

    public void setAllInvisible() {
        // תפריט ראשי - Manager Menu
        labelwelcome.setVisible(false);
        btnback.setVisible(false);
        btnmemberdetails.setVisible(false);
        btnparkingdetails.setVisible(false);
        btnregisternewmember.setVisible(false);
        btnparkingduration.setVisible(false);
        btnmemberstatusreport.setVisible(false);

        // Member Details
        labelmemberdetails.setVisible(false);
        labelsearchby_id.setVisible(false);
        searchbyidtext.setVisible(false);
        btnsearch_memberdetails.setVisible(false);
        console_memberdeatils.setVisible(false);

        // Parking Details
        label_parking_details.setVisible(false);
        console_parkingdetails.setVisible(false);
        label_parkingdetails_search.setVisible(false);
        searchbytext2.setVisible(false);
        btnsearch_parkingdetails.setVisible(false);

        // Register New Member
        label_register_member.setVisible(false);
        label_firstname.setVisible(false);
        label_lsatname.setVisible(false);
        label_id.setVisible(false);
        label_email.setVisible(false);
        label_phonenumber.setVisible(false);
        label_vehiclenumber.setVisible(false);
        textfield_firstname.setVisible(false);
        textfield_lastname.setVisible(false);
        textfield_id1.setVisible(false);
        textfield_emil.setVisible(false);
        textfiled_phonenumber.setVisible(false);
        label_vehiclenumber_register.setVisible(false);
        
     // Parking Duration Report
        label_enteryear.setVisible(false);
        label_entermoth.setVisible(false);
        label_Enteryear.setVisible(false);
        monthField.setVisible(false);
        parking_timechart.setVisible(false);
        
     // Member Status Report
        memberStatusTitle.setVisible(false);
        chart_memberstatus.setVisible(false);
        
        btnback.setVisible(false);

        
    }
    @FXML
    private void handleViewMemberStatusReport() {
    	screenHistory.push(this::showManagerMenu); 
        setAllInvisible();
        memberStatusTitle.setVisible(true);
        chart_memberstatus.setVisible(true);
        btnback.setVisible(true);
    }


    public void showLoginScreen() {
        screenHistory.push(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWelcome.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) btnback.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Welcome to BPARK");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        usernametextfield.setVisible(true);
        passwordfeild.setVisible(true);
        btnloginsubmit.setVisible(true);
        btnforgerpassword.setVisible(true);
        usernamelabel.setVisible(true);
        passwordlabel.setVisible(true);
        loginlabel.setVisible(true);
        btnback.setVisible(true);
    }

    /**
     * Handles the Back button click from the login screen.
     * This method loads the main welcome screen (mainWelcome.fxml)
     * and replaces the current scene with it.
     */
    @FXML
    private void handleLoginBack() {
        try {
            // Load the welcome screen FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWelcome.fxml"));
            Parent root = loader.load();

            // Get the current stage from the back button
            Stage stage = (Stage) btnback.getScene().getWindow();

            // Set the new scene and update the window title
            stage.setScene(new Scene(root));
            stage.setTitle("Welcome to BPARK");

        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    public void showManagerMenu() {
        screenHistory.push(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWelcome.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) btnback.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Welcome to BPARK");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        setAllInvisible();
        labelwelcome.setText("Welcome, Manager");
        labelwelcome.setVisible(true);
        btnmemberdetails.setVisible(true);
        btnparkingdetails.setVisible(true);
        btnregisternewmember.setVisible(true);
        btnparkingduration.setVisible(true);
        btnmemberstatusreport.setVisible(true);
        usernametextfield.setVisible(false);
        passwordfeild.setVisible(false);
        btnloginsubmit.setVisible(false);
        btnforgerpassword.setVisible(false);
        usernamelabel.setVisible(false);
        passwordlabel.setVisible(false);
        loginlabel.setVisible(false);
        btnback.setVisible(true);
        
    }

    @FXML
    private void handleViewMemberDetails() {
    	screenHistory.push(this::showManagerMenu); 
        setAllInvisible(); 
        labelmemberdetails.setVisible(true);
        labelsearchby_id.setVisible(true);
        searchbyidtext.setVisible(true);
        btnsearch_memberdetails.setVisible(true);
        console_memberdeatils.setVisible(true);
        btnback.setVisible(true);
    }
    @FXML
    private void handleViewParkingDetails() {
    	screenHistory.push(this::showManagerMenu);
        setAllInvisible(); 
        label_parking_details.setVisible(true);
        console_parkingdetails.setVisible(true);
        label_parkingdetails_search.setVisible(true);
        searchbytext2.setVisible(true);
        btnsearch_parkingdetails.setVisible(true);
        btnback.setVisible(true);
    }
    @FXML
    private void handleRegisterNewMember() {
    	screenHistory.push(this::showManagerMenu);
        setAllInvisible(); 
        label_register_member.setVisible(true);
        label_firstname.setVisible(true);
        label_lsatname.setVisible(true);
        label_id.setVisible(true);
        label_email.setVisible(true);
        label_phonenumber.setVisible(true);
        label_vehiclenumber.setVisible(true);
        textfield_firstname.setVisible(true);
        textfield_lastname.setVisible(true);
        textfield_id1.setVisible(true);
        textfield_emil.setVisible(true);
        textfiled_phonenumber.setVisible(true);
        label_vehiclenumber_register.setVisible(true);
        btnback.setVisible(true);
    }
    @FXML
    private void handleViewParkingDuration() {
    	screenHistory.push(this::showManagerMenu);
        setAllInvisible();
        label_enteryear.setVisible(true);
        label_entermoth.setVisible(true);
        label_Enteryear.setVisible(true);
        monthField.setVisible(true);
        parking_timechart.setVisible(true);
        btnback.setVisible(true);
    }

    @FXML
    private void handleLoginSubmit() {
        showManagerMenu(); 
    }
    @FXML
    private void handleBack() {
        if (!screenHistory.isEmpty()) {
            Runnable lastScreen = screenHistory.pop();
            lastScreen.run();
        }
    }
    @FXML
    private void handleBackToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWelcome.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnbacktomain.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Welcome to BPARK");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
