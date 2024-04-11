package app.sportslink;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class SportsLink extends Application {
    public static User USER = null;
    private static Stage _stage = null;


// new changes
    public static class SceneData{
        public final String FILE;
        public final String TITLE;
        SceneData(String file, String title){
            FILE = file;
            TITLE = title;
        }
    }

    public static class SCENE{
        private SCENE(){}
        public static final SceneData LOGIN = new SceneData("login.fxml", "SportsLink - Login");
        public static final SceneData SIGNUP = new SceneData("signup.fxml", "SportsLink - New User Sign up");
        public static final SceneData UNCONFIRMED = new SceneData("unconfirmed.fxml", "SportsLink - Unconfirmed User");
        public static final SceneData DASHBOARD = new SceneData("dashboard.fxml", "SportsLink - User Dashboard");
    };


    public static void setScene(SceneData scene){
        Parent root = null;
        try {
            root = FXMLLoader.load(Objects.requireNonNull(SportsLink.class.getResource(scene.FILE)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        _stage.setTitle(scene.TITLE);
        _stage.setScene(new Scene(root, 600, 400));
        _stage.show();
    }

    @Override
    public void start(Stage stage) {
        _stage = stage;
        setScene(SCENE.LOGIN);
    }

    public static void main(String[] args) {
        launch();
    }
}