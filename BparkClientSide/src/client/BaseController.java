package client;

/**
 * Base interface for all GUI controllers that need a ChatClient connection.
 * Ensures a unified way to inject the client into each controller.
 */ 
public interface BaseController {
    /**
     * Injects the ChatClient into the controller after the FXML is loaded.
     * 
     * @param client The ChatClient used for server communication..
     */
    void setClient(ChatClient client);
}
