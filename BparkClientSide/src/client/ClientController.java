/**
 * ClientController manages the flow and UI of the remote client app.
 * It switches between views using visibility toggles and handles user input,
 * form submissions, and mock data logic until database integration.
 */
package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import common.LoginRequest;
import common.ParkingHistory;
import common.PasswordResetRequest;
import common.PasswordResetResponse;
import common.Reservation;
import common.Subscriber;
import common.UpdateReservationRequest;
import common.UpdateSubscriberDetailsRequest;

public class ClientController implements BaseController {

    private ChatClient client;
    private Subscriber currentSubscriber;
    private boolean isLoggedIn = false;
    private Reservation reservationBeingEdited = null;


    /**
     * Stack to manage the user's navigation history across panes (screens).
     * Each time the user navigates forward, the current pane is saved here.
     * When 'Back' is clicked, the top pane from the stack is shown again.
     * This allows navigating backward through previous screens.
     */
    private final Stack<Pane> navigationStack = new Stack<>();
    private static ClientController instance;

    /**
     * A Pane in JavaFX is a layout container for organizing UI elements (buttons, labels, etc.).
     * We use multiple panes to represent different "screens" within the same window.
     * Only one pane is visible at a time to simulate switching between screens.
     */
    @FXML private VBox mainMenu, signInForm, spotsView, forgotPasswordView, postLoginMenu, personalInfoView,
            editInfoForm, activityMenu, historyView, reservationsView,
            reservationForm, extendInfo;

    @FXML private Button signInButton, showSpotsButton, personalInfoButton, activityButton,
            scheduleButton, extendButton, logoutButton, editInfoButton,
            submitButton, submitEditButton, historyButton, reservationsButton, editReservationButton, cancelReservationButton,
            reserveSubmitButton, backButton;

    @FXML private Label welcomeLabel, resetMessage, usernameLabel, emailLabel, phoneLabel,
            car1Label, creditCardLabel, LogOutLabel, loginlabel, 
            greetingLabelHistory, greetingLabelReservations;
    

    @FXML private TextField resetEmailField, idField, editPhoneField, editEmailField,
            dateField, timeField;

    @FXML private TextArea spotsTextArea;
    
    @FXML private PasswordField codeField;
    
    @FXML private Hyperlink forgotPasswordLink;
    
    @FXML private ListView<Reservation> reservationListView;
   
    @FXML private ListView<ParkingHistory> historyListView;



    /**
     * Sets the ChatClient instance for server communication.
     * @param client the ChatClient object
     */
    @Override
    public void setClient(ChatClient client) {
        this.client = client;
    }

    /**
     * Initializes the controller by showing the main menu.
     * Called automatically when the FXML is loaded.
     */
    @FXML
    private void initialize() {
        showOnly(mainMenu);
        
        if (client != null) {
            client.setController(this);
        }
    }

    
    public ClientController() {
        instance = this;
    }

    public static ClientController getInstance() {
        return instance;
    }

    /**
     * Makes only the given pane visible and hides all others.
     * This is used to display a specific screen in the UI.
     *
     * @param target the pane to show
     */
    private void showOnly(Pane target) {
        for (Pane pane : new Pane[]{mainMenu, signInForm, spotsView, forgotPasswordView, postLoginMenu, personalInfoView,
                editInfoForm, activityMenu, historyView, reservationsView, reservationForm, extendInfo}) {
            if (pane != null) {
                pane.setVisible(false);
                pane.setManaged(false);
            }
        }
        target.setVisible(true);
        target.setManaged(true);
    }

    /**
     * Navigates to the given pane by saving the current visible pane into the navigation stack.
     * Use this method when you want the 'Back' button to return to the current screen.
     *
     * If you don't want to allow navigating back to a specific screen (e.g., after editing),
     * use showOnly() instead of navigateTo().
     *
     * @param next the next pane to show
     */
    private void navigateTo(Pane next) {
        for (Pane pane : new Pane[]{mainMenu, signInForm, spotsView, postLoginMenu, personalInfoView,
                editInfoForm, activityMenu, historyView, reservationsView, reservationForm, extendInfo}) {
            if (pane != null && pane.isVisible()) {
                navigationStack.push(pane);
                break;
            }
        }
        showOnly(next);
    }

    /** Button handlers for navigation */
    @FXML 
    private void handleSignInClick() {
    	idField.clear();
        codeField.clear();
    	navigateTo(signInForm);
    }
    @FXML 
    private void handleShowForgotPasswordView() {
    	showOnly(forgotPasswordView);
    	
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
            client.sendToServer(new PasswordResetRequest(email,"sub"));
        } catch (IOException e) {
            e.printStackTrace();
            resetMessage.setText("Failed to send request.");
            resetMessage.setStyle("-fx-text-fill: red;");
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
    
    @FXML
    private void handleShowSpotsClick() {
        navigateTo(spotsView);
        try {
			client.sendToServer("REQUEST_AVAILABLE_SPOTS");
		} catch (IOException e) {
			e.printStackTrace();
		}

    }
    
    @FXML private void handlePersonalInfo() { 
    	navigationStack.push(postLoginMenu);  
        showOnly(personalInfoView);
       
    }
    
    /**
     * Navigates the user interface to the post-login menu screen.
     * 
     * This method is used after successful actions such as login or reservation,
     * to return the user to the main menu of the client application.
     * It wraps a call to showOnly(postLoginMenu) while keeping the postLoginMenu
     * field private and encapsulated.
     */
    public void showOnlyPostLoginMenu() {
        showOnly(postLoginMenu);
    }
    
    /**
     * handleEditInfo — Navigates to the edit info form and pre-fills data.
     *
     * Description:
     * Loads current subscriber phone and email into editable fields for update,
     * then transitions to the edit pane.
     *
     * Parameters:
     *   - None
     *
     * Returns:
     *   - void
     */
    @FXML
    private void handleEditInfo() {
        if (currentSubscriber != null) {
            editEmailField.setText(currentSubscriber.getEmail());
            editPhoneField.setText(currentSubscriber.getPhone());
        }
        navigateTo(editInfoForm);
    }
    
    
    @FXML private void handleActivity() { navigateTo(activityMenu); }
    
    
  
    /**
     * handleHistory — Initiates request to fetch parking history.
     * Description:
     * Sends a GET_HISTORY command to the server using the subscriber's ID
     * and navigates to the history screen.
     */  
    @FXML
    private void handleHistory() {
        navigateTo(historyView);
        if (currentSubscriber != null) {
            client.sendToServerSafe("GET_HISTORY|" + currentSubscriber.getSubscriber_id());
        } else {
            showPopup("Subscriber not loaded.");
        }
    }

    
    /**
     * handleReservations — Sends request to load subscriber's active reservations.
     * Description:
     * Sends GET_RESERVATIONS to server using current subscriber ID
     * and navigates to the reservations pane.
     */
    @FXML
    private void handleReservations() {
        navigateTo(reservationsView);
        if (currentSubscriber != null) {
            client.sendToServerSafe("GET_RESERVATIONS|" + currentSubscriber.getSubscriber_id());
        } else {
            showPopup("Subscriber not loaded.");
        }
    }
    
    @FXML private void handleSchedule() { navigateTo(reservationForm); }

    /**
     * Handles login form submission.
     * Checks mock ID and code, loads user data if successful, or shows error popup.
     */
    @FXML
    private void handleSubmitLogin() {
        String id = idField.getText().trim();
        String code = codeField.getText().trim();
        idField.clear();
        codeField.clear();

        if (id.isEmpty() || code.isEmpty()) {
            showPopup("Please enter both ID and Subscriber Code.");
            return;
        }

       //send login request to the server
        try {
        	client.sendToServer(new LoginRequest(id, code, "app"));
		} catch (IOException e) {
			
			e.printStackTrace();
		}
    }
    
    
    /**
     * setSubscriber — Updates the current logged-in subscriber's details in the controller.
     * Description:
     * Sets the active subscriber and updates various UI labels such as email, phone,
     * car numbers, and credit card info. Also initializes personalized greeting labels
     * across different panes.
     * Parameters:
     *   - subscriber: the Subscriber object returned from the server after successful login
     */
    public void setSubscriber(Subscriber subscriber) {
    	 System.out.println("Subscriber received: " + subscriber.getFull_name()); // 🔍 Debug
        this.currentSubscriber = subscriber;
        isLoggedIn = true;

        Platform.runLater(() -> {
        	usernameLabel.setText("Full Name: " + subscriber.getFull_name());
            emailLabel.setText("E-mail: " + subscriber.getEmail());
            phoneLabel.setText("Phone Number: " + subscriber.getPhone());
            car1Label.setText("Car Number: " + subscriber.getVehicle_number1());
            creditCardLabel.setText("Credit Card Number: " + subscriber.getCredit_card());
            
            welcomeLabel.setText("Welcome, " + subscriber.getFull_name() + "!");
            navigationStack.clear();
            navigationStack.push(signInForm);
            showOnly(postLoginMenu);
        });
    }
  
    /**
     * displayHistory — Shows parking history for the logged-in subscriber.
     * Description:
     * Clears the history view, sets a greeting header, and displays a list of historical
     * parking entries received from the server..
     * Parameters:
     *   - historyList: List of ParkingHistory objects
     */
    public void displayHistory(List<ParkingHistory> historyList) {
        Platform.runLater(() -> {
            greetingLabelHistory.setText("Hi " + currentSubscriber.getFull_name() + ", here is your parking history:");
            showOnly(historyView); 

            historyListView.getItems().clear(); 

            if (historyList.isEmpty()) {
                historyListView.setPlaceholder(new Label("No parking history found."));
            } else {
                historyListView.getItems().addAll(historyList);
                historyListView.setCellFactory(listView -> new ListCell<>() {
                    @Override
                    protected void updateItem(ParkingHistory h, boolean empty) {
                        super.updateItem(h, empty);
                        if (empty || h == null) {
                            setText(null);
                        } else {
                            setText(formatHistory(h));
                        }
                    }
                });
            }
        });
    }


    private String formatHistory(ParkingHistory h) {
        return String.format(
            "Vehicle Number: %s | Entry: %s at %s | Exit: %s at %s\n",
            h.getVehicleNumber(),
            h.getEntryDate(),
            h.getEntryTime(),
            h.getExitDate(),
            h.getExitTime()
        );
    }

    /**
     * Checks if the parking history view is currently displayed.
     * @return true if the history view is visible; false otherwise.
     */
    public boolean isShowingHistoryView() {
        return historyView.isVisible();
    }
    /**
     * displayReservations — Displays current reservations for the subscriber.
     * Description:
     * Clears the reservations pane, shows a personalized heading, and populates the
     * list with existing reservation records.
     * Parameters:
     *   - reservationList: List of Reservation objects
     */
    public void displayReservations(List<Reservation> reservationList) {
        Platform.runLater(() -> {
            // Always show title and view
            greetingLabelReservations.setText("Hi " + currentSubscriber.getFull_name() + ", here are your current reservations:");
            if (!reservationsView.getChildren().contains(greetingLabelReservations)) {
                reservationsView.getChildren().add(0, greetingLabelReservations);
            }
            showOnly(reservationsView);

            reservationListView.getItems().clear();

            if (reservationList.isEmpty()) {
                reservationListView.setPlaceholder(new Label("No existing reservations found."));
            } else {
                reservationListView.getItems().addAll(reservationList);
                reservationListView.setCellFactory(listView -> new ListCell<>() {
                    @Override
                    protected void updateItem(Reservation r, boolean empty) {
                        super.updateItem(r, empty);
                        if (empty || r == null) {
                            setText(null);
                        } else {
                            setText(formatReservation(r));
                        }
                    }
                });
            }
        });
    }

    
    private String formatReservation(Reservation r) {
        return String.format(
            "Parking Code: %s | From: %s at %s, until: %s at %s\n",
            r.getParkingCode(),
            r.getEntryDate(),
            r.getEntryTime(),
            r.getExitDate(),
            r.getExitTime()
        );
    }
    
    /**
     * Checks if the reservations view is currently displayed.
     * @return true if the reservations view is visible; false otherwise.
     */
    public boolean isShowingReservationView() {
        return reservationsView.isVisible();
    }
    @FXML
    public void handleEditReservation() {
        Reservation selected = reservationListView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showPopup("Please select a reservation to edit.");
            return;
        }

        
        reservationBeingEdited = selected;

     
        dateField.setText(selected.getEntryDate().toString());
        timeField.setText(selected.getEntryTime().toString());

        navigateTo(reservationForm);
    }
    
    @FXML
    public void handleCancelReservation() {
        Reservation selected = reservationListView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showPopup("Please select a reservation to cancel.");
            return;
        }

        String message = "Are you sure you want to cancel this reservation?\n\n" + formatReservation(selected);

        if (showConfirmationPopup(message)) {
            try {
                client.sendToServer("CANCEL_RESERVATION|" + selected.getReservationId());
            } catch (IOException e) {
                e.printStackTrace();
                showPopup("Error sending cancel request.");
            }
        }
    }

    public void refreshReservationList() {
        try {
            client.sendToServer("GET_RESERVATIONS|" + currentSubscriber.getSubscriber_id());
        } catch (IOException e) {
            e.printStackTrace();
            showPopup("Failed to refresh reservations.");
        }
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
   
    /**
     * Handles the submit action for editing email/phone.
     * Performs local validation, sends update request to server, and updates local view if successful.
     */
    @FXML
    private void handleSubmitEdit() {
        String newPhone = editPhoneField.getText().trim();
        String newEmail = editEmailField.getText().trim();

        boolean phoneChanged = !newPhone.equals(currentSubscriber.getPhone());
        boolean emailChanged = !newEmail.equals(currentSubscriber.getEmail());

        if (!phoneChanged && !emailChanged) {
            showPopup("No changes were made.");
            return;
        }
        StringBuilder errors = new StringBuilder();
        if (phoneChanged && !newPhone.matches("\\d{10}")) {
            errors.append("Phone number must be exactly 10 digits.\n");
        }
        if (emailChanged && !newEmail.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            errors.append("Email must be in a valid format.\n");
        }
        if (!errors.isEmpty()) {
            showPopup(errors.toString());
            return;
        }

        try {
            client.sendToServer(new UpdateSubscriberDetailsRequest(
                currentSubscriber.getSubscriber_id(),
                emailChanged ? newEmail : currentSubscriber.getEmail(),
                phoneChanged ? newPhone : currentSubscriber.getPhone()
            ));
        } catch (IOException e) {
            e.printStackTrace();
            showPopup("Error sending update to server.");
            return;
        }
        currentSubscriber = new Subscriber(
            currentSubscriber.getSubscriber_id(),
            currentSubscriber.getFull_name(),
            emailChanged ? newEmail : currentSubscriber.getEmail(),
            phoneChanged ? newPhone : currentSubscriber.getPhone(),
            currentSubscriber.getVehicle_number1(),
            currentSubscriber.getSubscription_code(),
            currentSubscriber.getLateCount(),
            currentSubscriber.getCredit_card()
        );
        setSubscriber(currentSubscriber);
        showPopup("Details updated successfully.");
        navigationStack.push(postLoginMenu); 
        showOnly(personalInfoView); 
    }

    /**
     * Displays the reservation form.
     * @throws IOException 
     */
    @FXML
    private void handleSubmitReservation() {
        String dateInput = dateField.getText().trim();
        String timeInput = timeField.getText().trim();

        LocalDate date;
        LocalTime time;

        try {
            date = LocalDate.parse(dateInput);
        } catch (DateTimeParseException e) {
            showPopup("Invalid date format! Please use yyyy-MM-dd.\nExample: 2025-06-12");
            return;
        }

        try {
            time = LocalTime.parse(timeInput, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            showPopup("Invalid time format! Please use HH:mm.\nExample: 14:30");
            return;
        }

        LocalDate now = LocalDate.now();
        if (date.isBefore(now.plusDays(1)) || date.isAfter(now.plusDays(7))) {
            showPopup("Date must be between 1 to 7 days from today.");
            return;
        }

        try {
            if (reservationBeingEdited != null) {
                UpdateReservationRequest updateRequest = new UpdateReservationRequest(
                    reservationBeingEdited.getReservationId(), 
                    date,
                    time
                );
                client.sendToServer(updateRequest);
                reservationBeingEdited = null;
            } else {
                Reservation newReservation = new Reservation(
                    currentSubscriber.getSubscriber_id(),
                    date,
                    time
                );
                client.sendToServer(newReservation);
            }

            dateField.clear();
            timeField.clear();

        } catch (IOException e) {
            e.printStackTrace();
            showPopup("Error sending reservation to server.");
        }
        
    }

    /**
     * Extends the current parking session if not extended already.
     * Otherwise shows message that extension is not allowed.
     */
    /*
     * @FXML
    private void handleExtend() {
        if (hasExtended) {
            showPopup("You have already extended this parking session.\nFurther extensions are not allowed.");
        } else {
            hasExtended = true;
            showPopup("Parking time extended successfully.");
        }
    }*/
    
    @FXML
    private void handleExtend() {
        if (currentSubscriber != null) {
            client.sendToServerSafe("EXTEND_PARKING|" + currentSubscriber.getSubscriber_id());
        } else {
            showPopup("Subscriber not loaded.");
        }
    }

    /**
     * Quits the app or disconnects the client.
     */
    @FXML
    private void handleExitApp() {
        if (client != null) client.quit();
        else System.exit(0);
    }

    /**
     * Handles the back button. Navigates to the last screen if available.
     * If the stack is empty, returns to the welcome screen.
     */
    @FXML
    private void handleBack() {
        if (!navigationStack.isEmpty()) {
            Pane previous = navigationStack.pop();
            showOnly(previous);
        } else {
            if (isLoggedIn) {
                isLoggedIn = false; //end log-in session
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWelcome.fxml"));
                    Parent root = loader.load();
                    MainWelcomeController controller = loader.getController();
                    controller.showClientSubMenu(); // ⬅️ Choose Access Type
                    Stage stage = (Stage) backButton.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("BPARK - Welcome");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // 👇 זה מה שחסר לך!
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWelcome.fxml"));
                    Parent root = loader.load();
                    MainWelcomeController controller = loader.getController();
                    controller.showClientSubMenu(); // ← מחזיר ל־Choose Access Type
                    Stage stage = (Stage) backButton.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("BPARK - Welcome");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } 
    }

    /**
     * Utility method to show popup alerts in a consistent format.
     * @param message the content of the popup
     */
     void showPopup(String message) {
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
        wrapper.setPrefSize(500, 180);

        alert.getDialogPane().setContent(wrapper);
        alert.showAndWait();
    }
     
     /**
      * Shows a confirmation popup with Yes and Cancel buttons in a consistent format.
      * @param message the content of the popup
      * @return true if user confirmed (clicked Yes), false otherwise
      */
     boolean showConfirmationPopup(String message) {
         Alert alert = new Alert(Alert.AlertType.NONE);
         alert.setTitle("Confirmation");

         ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
         ButtonType cancelButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
         alert.getDialogPane().getButtonTypes().addAll(yesButton, cancelButton);

         Label content = new Label(message);
         content.setWrapText(true);
         content.setMaxWidth(300);
         content.setMinHeight(100);
         content.setStyle("-fx-text-alignment: center; -fx-alignment: center; -fx-font-size: 14px;");

         VBox wrapper = new VBox(content);
         wrapper.setAlignment(Pos.CENTER);
         wrapper.setPrefSize(500, 180);

         alert.getDialogPane().setContent(wrapper);

         Optional<ButtonType> result = alert.showAndWait();
         return result.isPresent() && result.get() == yesButton;
     }


}
