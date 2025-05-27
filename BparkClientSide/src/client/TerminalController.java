package client;

/**
 * Controller for the terminal interface.
 */
public class TerminalController implements BaseController {
 
    private ChatClient client;

    @Override
    public void setClient(ChatClient client) {
        this.client = client;
    }

    // TODO: Add @FXML elements and logic
}
