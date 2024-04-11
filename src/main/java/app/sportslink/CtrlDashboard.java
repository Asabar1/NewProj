package app.sportslink;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class CtrlDashboard implements Initializable {
    @FXML private Button button_show_activity_events;
    @FXML private ComboBox<User.ComboBoxOption> cb_sports;
    @FXML private ComboBox<User.ComboBoxOption> cb_levels;
    @FXML private TextField tf_zip_code;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cb_sports.setItems(User.getSportsOpts());
        cb_sports.getSelectionModel().selectFirst();
        cb_levels.setItems(User.getLevelOpts());
        cb_levels.getSelectionModel().selectFirst();
    }
}
