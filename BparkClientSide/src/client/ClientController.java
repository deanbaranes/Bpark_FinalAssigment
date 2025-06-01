/**
 * ClientController manages the flow and UI of the remote client app.
 * It switches between views using visibility toggles and handles user input,
 * form submissions, and mock data logic until database integration.
 */
package client;

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
import java.util.Stack;
import java.util.UUID;
import java.util.regex.Pattern;

public class ClientController implements BaseController {

    private ChatClient client;

    /**
     * Stack to manage the user's navigation history across panes (screens).
     * Each time the user navigates forward, the current pane is saved here.
     * When 'Back' is clicked, the top pane from the stack is shown again.
     * This allows navigating backward through previous screens.
     */
    private final Stack<Pane> navigationStack = new Stack<>();

    // ===== MOCK DATA SECTION =====
    private final String mockId = "123";
    private final String mockCode = "abc";
    private final String mockUsername = "Carmel";
    private final String mockEmail = "user@example.com";
    private final String mockPhone = "0501234567";
    private final String mockCar1 = "12-345-67";
    private final String mockCar2 = "89-012-34";
    private final String mockCredit = "**** **** **** 1234";
    private boolean hasExtended = false;
    // =============================

    /**
     * A Pane in JavaFX is a layout container for organizing UI elements (buttons, labels, etc.).
     * We use multiple panes to represent different "screens" within the same window.
     * Only one pane is visible at a time to simulate switching between screens.
     */
    @FXML private VBox mainMenu, signInForm, spotsView, postLoginMenu, personalInfoView,
            editInfoForm, activityMenu, historyView, reservationsView,
            reservationForm, extendInfo;

    @FXML private Button signInButton, showSpotsButton, personalInfoButton, activityButton,
            scheduleButton, extendButton, logoutButton, editInfoButton,
            submitButton, submitEditButton, historyButton, reservationsButton,
            reserveSubmitButton, backButton;

    @FXML private Label usernameLabel, emailLabel, phoneLabel,
            car1Label, car2Label, creditCardLabel;

    @FXML private TextField idField, editPhoneField, editEmailField,
            dateField, timeField;

    @FXML private PasswordField codeField;

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
    }

    /**
     * Makes only the given pane visible and hides all others.
     * This is used to display a specific screen in the UI.
     *
     * @param target the pane to show
     */
    private void showOnly(Pane target) {
        for (Pane pane : new Pane[]{mainMenu, signInForm, spotsView, postLoginMenu, personalInfoView,
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
    @FXML private void handleSignInClick() { navigateTo(signInForm); }
    @FXML private void handleShowSpotsClick() { navigateTo(spotsView); }
    @FXML private void handlePersonalInfo() { navigateTo(personalInfoView); }
    @FXML private void handleEditInfo() { navigateTo(editInfoForm); }
    @FXML private void handleActivity() { navigateTo(activityMenu); }
    @FXML private void handleHistory() { navigateTo(historyView); }
    @FXML private void handleReservations() { navigateTo(reservationsView); }
    @FXML private void handleSchedule() { navigateTo(reservationForm); }

    /**
     * Handles login form submission.
     * Checks mock ID and code, loads user data if successful, or shows error popup.
     */
    @FXML
    private void handleSubmitLogin() {
        String id = idField.getText().trim();
        String code = codeField.getText().trim();

        if (id.isEmpty() || code.isEmpty()) {
            showPopup("Please enter both ID and Subscriber Code.");
            return;
        }

        if (id.equals(mockId) && code.equals(mockCode)) {
            emailLabel.setText("Email: " + mockEmail);
            phoneLabel.setText("Phone: " + mockPhone);
            usernameLabel.setText("Username: " + mockUsername);
            car1Label.setText("Primary Car: " + mockCar1);
            car2Label.setText("Secondary Car: " + mockCar2);
            creditCardLabel.setText("Credit Card: " + mockCredit);

            navigationStack.clear();
            showOnly(postLoginMenu);
        } else {
            showPopup("Invalid ID or Code.\nPlease try again.");
        }
    }

    /**
     * Handles the submission of personal info edits.
     * Validates input and shows appropriate messages.
     */
    @FXML
    private void handleSubmitEdit() {
        String newPhone = editPhoneField.getText().trim();
        String newEmail = editEmailField.getText().trim();

        boolean phoneChanged = !newPhone.isEmpty();
        boolean emailChanged = !newEmail.isEmpty();

        if (!phoneChanged && !emailChanged) {
            showPopup("Please edit at least one field.");
            return;
        }

        StringBuilder errors = new StringBuilder();
        if (phoneChanged && !newPhone.matches("\\d{10}")) {
            errors.append("Phone number must be exactly 10 digits.\n");
        }
        if (emailChanged && !Pattern.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", newEmail)) {
            errors.append("Email must be in a valid format.\n");
        }

        if (!errors.isEmpty()) {
            showPopup(errors.toString());
            return;
        }

        if (phoneChanged) phoneLabel.setText("Phone: " + newPhone);
        if (emailChanged) emailLabel.setText("Email: " + newEmail);

        showPopup("Details updated successfully.\nYou can now click Back.");
        showOnly(personalInfoView);
    }

    /**
     * Displays the reservation form.
     */
    @FXML
    private void handleSubmitReservation() {
        String dateInput = dateField.getText().trim();
        String timeInput = timeField.getText().trim();

        try {
            LocalDate date = LocalDate.parse(dateInput);
            LocalDate now = LocalDate.now();
            if (date.isBefore(now.plusDays(1)) || date.isAfter(now.plusDays(7))) {
                showPopup("Date must be between 1 to 7 days from now.");
                return;
            }
            LocalTime time = LocalTime.parse(timeInput, DateTimeFormatter.ofPattern("HH:mm"));

            String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            showPopup("Reservation confirmed.\nYour parking code is: " + code +
                    "\nPlease note: If you do not arrive within 15 minutes of your reservation time, your reservation will be cancelled.");
            showOnly(postLoginMenu);
        } catch (DateTimeParseException e) {
            showPopup("Please enter a valid date (yyyy-mm-dd) and time (hh:mm).\nExample: 2025-06-03 14:30");
        }
    }

    /**
     * Extends the current parking session if not extended already.
     * Otherwise shows message that extension is not allowed.
     */
    @FXML
    private void handleExtend() {
        if (hasExtended) {
            showPopup("You have already extended this parking session.\nFurther extensions are not allowed.");
        } else {
            hasExtended = true;
            showPopup("Parking time extended successfully.");
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

    /**
     * Utility method to show popup alerts in a consistent format.
     * @param message the content of the popup
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

}
