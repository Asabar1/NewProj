package app.sportslink;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.net.URL;
import java.util.ResourceBundle;

public class CtrlUnconfirmed implements Initializable{
    @FXML private Button button_logout;
    @FXML private Label label_welcome;
    @FXML private Label label_welcome_2;
    @FXML private Label label_welcome_3;
    @FXML private Label label_fav_sports;

    @Override
    public void initialize(URL location, ResourceBundle resources){
        label_welcome.setText(STR."Welcome \{SportsLink.USER.getUsername()}!");
        button_logout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //Utils.changeScene(event, "login.fxml", "Sign in", null,null);


            }
        });

    }
}
