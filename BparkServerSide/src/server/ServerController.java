package server;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

/**
 * ServerController manages the server-side GUI interactions.
 * It allows the user to start the server, view connected clients,
 * and exit the application..
 */
public class ServerController {

    @FXML
    private Button connectButton;

    @FXML
    private Button showInfoButton;

    @FXML
    private Button exitButton;

    @FXML
    private TextArea textArea;

    private EchoServer echoServer;

    /**
     * Called when the "Connect EchoServer" button is pressed.
     * Starts the server on a separate thread and updates the UI.
     */
    @FXML
    private void handleConnect() {
        new Thread(() -> {
            echoServer = new EchoServer(5555);
            try {
                appendInfo("Starting server on port 5555...");
                echoServer.listen();
                appendInfo("Server is now listening.");
            } catch (Exception e) {
                appendInfo("Failed to start server: " + e.getMessage());
            }
        }).start();

        connectButton.setVisible(false);
        connectButton.setManaged(false);

        showInfoButton.setVisible(true);
        showInfoButton.setManaged(true);

        exitButton.setVisible(true);
        exitButton.setManaged(true);
    }

    /**
     * Appends informational messages to the text area in a thread-safe way.
     * @param text The message to display.
     */
    public void appendInfo(String text) {
        javafx.application.Platform.runLater(() -> {
            textArea.appendText(text + "\n");
        });
    }

    /**
     * Called when the "Show Info" button is pressed.
     * Displays information about currently connected clients.
     */
    @FXML
    private void handleShowInfo() {
        String info = echoServer.getAllClientsInfo();
        textArea.setText("");
        textArea.appendText(info + "\n");
    }

    /**
     * Called when the "Exit" button is pressed.
     * Closes the server and exits the application.
     */
    @FXML
    private void handleExit() {
        try {
            if (echoServer != null) {
                echoServer.close();
            }
        } catch (Exception e) {
            appendInfo("Error stopping server: " + e.getMessage());
        }
        System.exit(0);
    }
}
