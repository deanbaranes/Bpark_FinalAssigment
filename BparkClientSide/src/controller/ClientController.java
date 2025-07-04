package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import request.LoginRequest;
import request.PasswordResetRequest;
import request.UpdateReservationRequest;
import request.UpdateSubscriberDetailsRequest;
import response.PasswordResetResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Stack;

import clientSide.ChatClient;
import entities.ParkingHistory;
import entities.Reservation;
import entities.Subscriber;

/**
 * ClientController manages the flow, logic, and UI interaction for the client-side
 * of the BPARK application.
 * This controller is responsible for:
 * Handling navigation between different screens (panes) in the client app.
 * Processing user actions such as login, password reset, viewing parking history and reservations, editing details, and making or canceling reservations.
 * Communicating with the server via the ChatClient to send requests and receive responses.
 * Maintaining session-specific state, such as the currently logged-in subscriber and their data.
 * Ensuring that all UI updates are performed safely on the JavaFX Application Thread.
 * This controller supports both navigation using a stack (for back/forward behavior)
 * and direct screen switching via showOnly(Pane).
 * It implements BaseController, allowing unified handling across multiple controller types.
 */
public class ClientController implements BaseController {

    private ChatClient client;
    private Subscriber currentSubscriber;
    private boolean isLoggedIn = false;
    private Reservation reservationBeingEdited = null;
    private static ClientController instance;

    /**
     * Stack to manage the user's navigation history across panes (screens).
     * Each time the user navigates forward, the current pane is saved here.
     * When 'Back' is clicked, the top pane from the stack is shown again.
     * This allows navigating backward through previous screens.
     */
    private final Stack<Pane> navigationStack = new Stack<>();
    

    /**
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
     * Constructs a new instance of ClientController and sets it as the singleton instance.
     * This constructor is typically invoked by the JavaFX framework when loading the FXML file.
     * It ensures that a reference to the controller is globally accessible via getInstance().
     */
    public ClientController() {
        instance = this;
    }

    /**
     * Returns the singleton instance of the ClientController.
     * This allows other parts of the application to access the currently active
     * controller and interact with its methods or UI elements.
     * @return the current instance of ClientController, or null if it has not yet been initialized.
     */
    public static ClientController getInstance() {
        return instance;
    }
    
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

    
    /**
     * Handles the "Sign In" button click from the welcome screen.
     *
     * Clears the ID and code input fields, then navigates to the sign-in form.
     * This is the entry point for subscriber login.
     */
     @FXML 
    private void handleSignInClick() {
    	idField.clear();
        codeField.clear();
    	navigateTo(signInForm);
    }
    
    /**
     * Handles the "Forgot Password" hyperlink click from the login screen.
     *
     * Clears the ID and code input fields and navigates to the
     * password reset form where the user can request a reset email.
     */
    @FXML 
    private void handleShowForgotPasswordView() {
    	idField.clear();
        codeField.clear();
        navigateTo(forgotPasswordView);
    	
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
    
    /**
     * Handles the "Show Available Spots" button click.
     *
     * Navigates to the available parking spots screen and sends a request
     * to the server to retrieve the list of currently available spots.
     * The request is sent via the connected ChatClient.
     */
    @FXML
    private void handleShowSpotsClick() {
        navigateTo(spotsView);
        try {
			client.sendToServer("REQUEST_AVAILABLE_SPOTS");
		} catch (IOException e) {
			e.printStackTrace();
		}

    }
    
    /**
     * Handles the "Personal Info" button click.
     *
     * Navigates from the post-login menu to the personal information view.
     * Also pushes the current screen (post-login menu) onto the navigation stack
     * to allow returning back when the user clicks "Back".
     */
    @FXML 
    private void handlePersonalInfo() { 
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
     * Handles the "Edit Info" button click.
     * This method:
     * Loads the current subscriber's email and phone number into the corresponding text fields
     *       to allow editing.
     * Only performs this action if a subscriber is currently logged in.
     * Transitions the UI to the edit information form using navigateTo(editInfoForm).
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
     * Sets the currently logged-in subscriber and updates the UI with their information.
     * This method is called after a successful login and performs the following:
     * Stores the subscriber in the controller for future access.
     * Marks the session as logged in.
     * Updates the UI labels (name, email, phone, car number, credit card) with the subscriber's details.
     * Displays a personalized welcome message.
     * Clears the navigation stack and sets the post-login menu as the active screen.

     * @param subscriber the Subscriber object containing the user's data
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
            navigationStack.push(mainMenu);
            navigationStack.push(signInForm);

            showOnly(postLoginMenu);
        });
    }
  
    /**
     * Displays the parking history for the current subscriber in the UI.
     * This method performs the following:
     * Sets a personalized greeting based on the subscriber's name.
     * Clears previous items from the history list view.
     * If the list is empty, shows a placeholder message.
     * If the list contains items, populates the list with ParkingHistory entries
     *       and sets each cell's text using the {@code toString()} method of the object.
     * Ensures all UI updates are run on the JavaFX Application Thread via Platform.runLater().
     * @param historyList a list of ParkingHistory objects retrieved from the server
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
                        	setText(h.toString());
                        }
                    }
                });
            }
        });
    }

    /**
     * Checks if the parking history view is currently displayed.
     * @return true if the history view is visible; false otherwise.
     */
    public boolean isShowingHistoryView() {
        return historyView.isVisible();
    }
    
    /**
     * Displays the list of current reservations for the logged-in subscriber.
     * This method is responsible for:
     * Setting a personalized greeting label with the subscriber's name.
     * Ensuring the greeting label is added to the reservations view only once.
     * Clearing any previous reservation entries from the reservationListView.
     * If the list is empty, displaying a placeholder message.
     * If the list is not empty, populating the list view with Reservation objects
     *       and using their toString() method for display.
     * Ensuring all UI updates occur on the JavaFX Application Thread via Platform.runLater().
     * @param reservationList a list of Reservation objects retrieved from the server
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
            editReservationButton.setVisible(false);
            editReservationButton.setManaged(false);
            cancelReservationButton.setVisible(false);
            cancelReservationButton.setManaged(false);
            
            if (reservationList.isEmpty()) {
                reservationListView.setPlaceholder(new Label("No existing reservations found."));
            } else {
            	editReservationButton.setVisible(true);
                editReservationButton.setManaged(true);
                cancelReservationButton.setVisible(true);
                cancelReservationButton.setManaged(true);
                reservationListView.getItems().addAll(reservationList);
                reservationListView.setCellFactory(listView -> new ListCell<>() {
                    @Override
                    protected void updateItem(Reservation r, boolean empty) {
                        super.updateItem(r, empty);
                        if (empty || r == null) {
                            setText(null);
                        } else {
                        	setText(r.toString());
                        }
                    }
                });
            }
        });
    }

    
    /**
     * Checks if the reservations view is currently displayed.
     * @return true if the reservations view is visible; false otherwise.
     */
    public boolean isShowingReservationView() {
        return reservationsView.isVisible();
    }
    @FXML
    
    /**
     * Handles the "Edit Reservation" button click event.
     * This method performs the following steps:
     * Retrieves the selected reservation from the reservation list view.
     * If no reservation is selected, displays a popup prompting the user to select one.
     * If a reservation is selected, stores it in reservationBeingEdited.
     * Populates the reservation form fields dateField, timeField with the existing values.
     * Navigates the user to the reservation form for editing.
     * This method prepares the UI for updating an existing reservation rather than creating a new one.
     */
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
    
    /**
     * Handles the cancellation of a selected reservation.
     * This method is triggered when the user clicks the "Cancel Reservation" button.
     * It performs the following steps:
     * Retrieves the currently selected reservation from the reservation list view.
     * If no reservation is selected, displays a popup prompting the user to select one.
     * Shows a confirmation dialog asking the user to confirm the cancellation.
     * If the user confirms, sends a cancellation request to the server using the reservation ID.
     * If an IOException occurs during communication, an error popup is displayed.
     */
    @FXML
    public void handleCancelReservation() {
        Reservation selected = reservationListView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showPopup("Please select a reservation to cancel.");
            return;
        }

        String message = "Are you sure you want to cancel this reservation?\n\n" + selected.toString();

        showConfirmationPopup(message, () -> {
            try {
                client.sendToServer("CANCEL_RESERVATION|" + selected.getReservationId());
            } catch (IOException e) {
                e.printStackTrace();
                showPopup("Error sending cancel request.");
            }
        });
    }

    /**
     * Sends a request to the server to refresh the subscriber's reservation list.
     *
     * This method retrieves the current subscriber's ID and sends a message to the server
     * requesting updated reservation data. If the request fails,
     * a popup is shown to inform the user of the failure.
     */
    public void refreshReservationList() {
        try {
            client.sendToServer("GET_RESERVATIONS|" + currentSubscriber.getSubscriber_id());
        } catch (IOException e) {
            e.printStackTrace();
            showPopup("Failed to refresh reservations.");
        }
    }

    /**
     * Updates the UI to display a list of available parking spots.
     * This method is called after receiving a list of available spots from the server.
     * It ensures the UI is updated on the JavaFX Application Thread using Platform.runLater.
     * If the list is empty, it shows a default message indicating no availability.
     * Otherwise, it displays the list of spots in a formatted multiline text area.
     *
     * @param availableSpots a list of strings representing available parking spot identifiers
     */
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
        if (postLoginMenu.isVisible()) {
            showConfirmationPopup("Are you sure you want to log out?", () -> {
                if (!navigationStack.isEmpty()) {
                    Pane previous = navigationStack.pop();
                    showOnly(previous);
                } else {
                    if (isLoggedIn) {
                        isLoggedIn = false; // end log-in session
                    }   try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/mainWelcome.fxml"));
                            Parent root = loader.load();
                            MainWelcomeController controller = loader.getController();
                            controller.showClientSubMenu();

                            Scene scene = new Scene(root);
                            scene.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());

                            Stage stage = (Stage) backButton.getScene().getWindow();
                            stage.setScene(scene);
                            stage.setTitle("BPARK - Welcome");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            });
        } else {
            if (!navigationStack.isEmpty()) {
                Pane previous = navigationStack.pop();
                showOnly(previous);
            } else {
                if (isLoggedIn) {
                    isLoggedIn = false; // end log-in session
                }   try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/mainWelcome.fxml"));
                        Parent root = loader.load();
                        MainWelcomeController controller = loader.getController();
                        controller.showClientSubMenu();

                        Scene scene = new Scene(root);
                        scene.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());

                        Stage stage = (Stage) backButton.getScene().getWindow();
                        stage.setScene(scene);
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
      * Shows a confirmation popup with Yes and Cancel buttons in a consistent format.
      * @param message the content of the popup
      * @return true if user confirmed (clicked Yes), false otherwise
      */
     public void showConfirmationPopup(String message,Runnable onConfirm) {
         Alert alert = new Alert(Alert.AlertType.NONE);
         alert.setTitle("Confirmation");
         alert.getDialogPane().setStyle(
        		    "-fx-background-color: linear-gradient(to right, #041958, #0458c0);" +
        		    "-fx-background-radius: 10;" +
        		    "-fx-padding: 20;");
         ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
         ButtonType cancelButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
         alert.getDialogPane().getButtonTypes().addAll(yesButton, cancelButton);

         Label content = new Label(message);
         content.setWrapText(true);
         content.setMaxWidth(300);
         content.setMinHeight(100);
         content.setStyle("-fx-text-alignment: center; -fx-alignment: center; -fx-font-size: 14px; -fx-text-fill: white;");

         VBox wrapper = new VBox(content);
         wrapper.setAlignment(Pos.CENTER);
         wrapper.setPrefSize(500, 180);

         alert.getDialogPane().setContent(wrapper);

         alert.showAndWait().ifPresent(result -> {
             if (result == yesButton) {
                 onConfirm.run();  
             }
         });
     }


}
