package app.sportslink;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class CtrlSignup implements Initializable {
    @FXML private Button button_signup;
    @FXML private Button button_log_in;
    @FXML private TextField tf_password;
    @FXML private TextField tf_password_verify;
    @FXML private TextField tf_username;
    @FXML private TextField tf_email_address;
    @FXML private TextField tf_first_name;
    @FXML private TextField tf_last_name;
    @FXML private TextField tf_us_zip_code;
    @FXML public CheckBox chk_terms;
    @FXML public ComboBox<User.ComboBoxOption> cb_visibility;

    @Override
    public void initialize(URL location, ResourceBundle resources){
        cb_visibility.setItems(User.getVisibilityOpts());
        cb_visibility.getSelectionModel().selectFirst();

        button_signup.setOnAction(event -> {
            String firstName = tf_first_name.getText().strip();
            String lastName = tf_last_name.getText().strip();
            String email = tf_email_address.getText().strip();
            String zipCode = tf_us_zip_code.getText().strip();
            String username = tf_username.getText().strip();
            String password = tf_password.getText();
            String passwordVerify = tf_password_verify.getText();
            short visibility = cb_visibility.getValue().getId();
            String errorMsg = User.verifySignup(firstName, lastName, email, zipCode, username, password, passwordVerify, chk_terms.isSelected());
            User user = null;
            if (errorMsg.isEmpty())
                user = new User(username, email, password, firstName, lastName, zipCode, visibility);
            if (user != null && !user.isError()) {
                Utils.showAlert(Alert.AlertType.INFORMATION, "User account created",
                        "Your user account has been created successfully. Check your email for a confirmation code, you will be asked for it when you log in for the first time.");
                SportsLink.setScene(SportsLink.SCENE.LOGIN);
            } else {
                if (user != null) errorMsg += STR."\n\{user.getError()}";
                Utils.showAlert(Alert.AlertType.ERROR, "Error(s) creating user account", errorMsg);
            }
        });

        button_log_in.setOnAction(event -> {
            SportsLink.setScene(SportsLink.SCENE.LOGIN);

        });

    }
}