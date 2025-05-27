package client;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ManagementController {

    @FXML private TextField usernametextfield;
    @FXML private PasswordField passwordfeild;
    @FXML private Button btnloginsubmit;
    @FXML private Button btnloginback;
    @FXML private Hyperlink btnforgerpassword;
    @FXML private Label usernamelabel;
    @FXML private Label passwordlabel;
    @FXML private Label loginlabel;
    @FXML private Label labelwelcome;
    @FXML private Button btnexit;
    @FXML private Button btnmemberdetails;
    @FXML private Button btnparkingdetails;
    @FXML private Button btnregisternewmember;
    @FXML private Button btnparkingduration;
    @FXML private Button btnmemberstatusreport;

    

    public void showLoginScreen() {
        usernametextfield.setVisible(true);
        passwordfeild.setVisible(true);
        btnloginsubmit.setVisible(true);
        btnloginback.setVisible(true);
        btnforgerpassword.setVisible(true);
        usernamelabel.setVisible(true);
        passwordlabel.setVisible(true);
        loginlabel.setVisible(true);
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
            Stage stage = (Stage) btnloginback.getScene().getWindow();

            // Set the new scene and update the window title
            stage.setScene(new Scene(root));
            stage.setTitle("Welcome to BPARK");

        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    public void setAllInvisible() {
        labelwelcome.setVisible(false);
        btnexit.setVisible(false);
        btnmemberdetails.setVisible(false);
        btnparkingdetails.setVisible(false);
        btnregisternewmember.setVisible(false);
        btnparkingduration.setVisible(false);
        btnmemberstatusreport.setVisible(false);

    }
    public void showManagerMenu() {
        setAllInvisible();
        labelwelcome.setText("Welcome, Manager");
        labelwelcome.setVisible(true);
        btnexit.setVisible(true);
        btnmemberdetails.setVisible(true);
        btnparkingdetails.setVisible(true);
        btnregisternewmember.setVisible(true);
        btnparkingduration.setVisible(true);
        btnmemberstatusreport.setVisible(true);
        usernametextfield.setVisible(false);
        passwordfeild.setVisible(false);
        btnloginsubmit.setVisible(false);
        btnloginback.setVisible(true);
        btnforgerpassword.setVisible(false);
        usernamelabel.setVisible(false);
        passwordlabel.setVisible(false);
        loginlabel.setVisible(false);

    }
    @FXML
    private void handleLoginSubmit() {
        showManagerMenu(); 
    }


}
