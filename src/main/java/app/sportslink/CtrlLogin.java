package app.sportslink;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class CtrlLogin implements Initializable{
    @FXML private Button button_login;
    @FXML private Button button_sign_up;
    @FXML private TextField tf_username;
    @FXML private TextField tf_password;

    @Override
    public void initialize(URL location, ResourceBundle resources){
        button_login.setOnAction(_ -> {
            if (User.isValidLogin(tf_username.getText(), tf_password.getText())) {
                SportsLink.USER = DB.getUser(User.getUserID(tf_username.getText(), tf_password.getText()));
                if(SportsLink.USER != null) {
                    if (SportsLink.USER.isConfirmed())
                        SportsLink.setScene(SportsLink.SCENE.DASHBOARD);
                    else
                        SportsLink.setScene(SportsLink.SCENE.UNCONFIRMED);
                }
                else
                    Utils.showAlert(Alert.AlertType.ERROR, "Unknown Error",
                            "Your user account details cannot be loaded right now.\n\t\t\tPlease try again.");
            } else
                Utils.showAlert(Alert.AlertType.ERROR, "Invalid Credentials",
                        "The username / password you entered are incorrect.\n\t\t\tPlease try again.");
        });
        button_sign_up.setOnAction(_ -> {
            SportsLink.setScene(SportsLink.SCENE.SIGNUP);
        });
    }
}