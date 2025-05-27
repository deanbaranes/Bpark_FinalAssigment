package client;

/**
 * Controller for the management (staff) interface.
 */
public class ManagementController implements BaseController {

    private ChatClient client;

    @Override
    public void setClient(ChatClient client) {
        this.client = client;
    }

    // TODO: Add @FXML elements and logic
}
