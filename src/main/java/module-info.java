module app.sportslink {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires jakarta.mail;
    requires jakarta.activation;

    opens app.sportslink to javafx.fxml;
    exports app.sportslink;
}