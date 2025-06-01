package client;

import javafx.scene.control.TextArea;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 * Controller for the terminal interface.
 */
public class TerminalController implements BaseController {
 
    private ChatClient client;
    
    @FXML private Button btndropoff, btnpickup, btnexit, btnbackinsertparkingcode, btnbackdropoff, btnLoginFirstTer, 
           btnViewFirstParkingSpot,btnFirstBack;
    
    @FXML private Label labelchooseservice, labelpickupcar, labeldropoddcar, labelnoparkingavailable, labelinsetparkingcode;
    
    @FXML private TextField textfieldparkingcode;
    
    @FXML private TextArea textfareadisplaypcode;
    
    @FXML private Hyperlink hyperlinkforgetpass;
    
    @FXML private Text textleavecaronconveyor, text4hoursanddelay;
    
    

    @Override
    public void setClient(ChatClient client) 
    {
        this.client = client;
    }
    
    
    @FXML
    private void handleExit() 
    {
        if (client != null) {
            client.quit();
        } else {
            System.exit(0);
        }
    }
    

    // TODO: Add @FXML elements and logic
}
