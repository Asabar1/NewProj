package app.sportslink;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.sql.*;

public final class DB {
    private DB(){}

    public static class TBL{
        private TBL(){}
        public static final String USERS = "users";             // Database table for storing users' data
        public static final String SPORTS = "sports";           // Database table for storing sports' names and ids
    }

    public static class COL{
        private COL(){}
        public static class USERS {                             // Database table for storing users' data
            private USERS(){}
            public static final String ID = "user_id";
            public static final String USERNAME = "username";
            public static final String EMAIL = "email";
            public static final String PASSWORD = "password";
            public static final String FIRST_NAME = "firstname";
            public static final String LAST_NAME = "lastname";
            public static final String ZIP_CODE = "zipcode";
            public static final String VISIBILITY = "visibility";
            public static final String CONFIRM_CODE = "confirm_code";
            public static final String IS_CONFIRMED = "confirmed";
            public static final String TS_CREATED = "ts_created";
        }
        public static class SPORTS {                             // Database table for storing sports' names and ids
            private SPORTS() {}
            public static final String ID = "sport_id";
            public static final String NAME = "sport";
        }
    }

    private static final String CONN_DB_NAME = "group_1";
    private static final String CONN_DRIVER = "com.mysql.cj.jdbc.Driver";
//  private static final String CONN_URL = STR."jdbc:mysql://localhost:3306/\{CONN_DB_NAME}?user=group_1&password=1_324BoDSkdaHCW2D3uLkv&allowPublicKeyRetrieval=true&useSSL=false";
    private static final String CONN_URL = STR."jdbc:mysql://149.255.39.15:3306/\{CONN_DB_NAME}?user=group_1&password=1_324BoDSkdaHCW2D3uLkv&useSSL=false";
    private static final String ERROR_MSG_BASE = "Database error:\n";
    private static Connection _conn = null;
    private static String _errorMsg = null;
    private static boolean _noError = true;


    private static synchronized boolean refreshConn(){
        if(_noError) {
            try {
                if (_conn == null || !_conn.isValid(0)) {
                    Class.forName(CONN_DRIVER);
                    _conn = DriverManager.getConnection(CONN_URL);
                }
                return _conn.isValid(0);
            } catch (Exception e) {
                setError(e.getMessage());
            }
        }
        return false;
    }

    public static synchronized void setError(String errorMessage){
        _errorMsg = ERROR_MSG_BASE + errorMessage;
        _noError = false;
    }

    public static synchronized boolean isError(){ return !_noError; }

    public static synchronized String getError(){
        if(isError()) return _errorMsg;
        return null;
    }

    public static synchronized void clearError(){
        if(isError()) {
            _noError = true;
            _errorMsg = null;
        }
    }

    // Returns the maximum character length than can be stored in a column
    public static synchronized int getMaxLength(String table, String column){
        if(refreshConn() && _noError){
            try {
                PreparedStatement query = _conn.prepareStatement(
                    "SELECT `CHARACTER_MAXIMUM_LENGTH` FROM `information_schema`.`COLUMNS` WHERE `TABLE_SCHEMA`=? AND `TABLE_NAME`=? AND `COLUMN_NAME`=?;");
                query.setString(1, CONN_DB_NAME);
                query.setString(2, table);
                query.setString(3, column);
                ResultSet rs = query.executeQuery();
                if(rs.next()) return rs.getInt(1);
            } catch (SQLException e){
                setError(e.getMessage());
            }
        }
        return 0;
    }

    // One column, single value existence verification
    public static synchronized boolean exists (String table, String column, String value){
        if(refreshConn() && _noError){
            try {
                PreparedStatement query = _conn.prepareStatement(STR."SELECT 1 FROM `\{table}` WHERE `\{column}`=? LIMIT 1;");
                query.setString(1, value);
                ResultSet rs = query.executeQuery();
                return rs.next();
            } catch (SQLException e){
                setError(e.getMessage());
            }
        }
        return false;
    }

    // Two columns, pair values existence verification
    public static synchronized boolean exists (String table, String column1, String column2, String value1, String value2){
        if(refreshConn() && _noError){
            try {
                PreparedStatement query = _conn.prepareStatement(STR."SELECT 1 FROM `\{table}` WHERE `\{column1}`=? AND `\{column2}`=? LIMIT 1;");
                query.setString(1, value1);
                query.setString(2, value2);
                ResultSet rs = query.executeQuery();
                return rs.next();
            } catch (SQLException e){
                setError(e.getMessage());
            }
        }
        return false;
    }

    // One column, single int value return - search by single condition on multiple column values
    // It assumes fields.size() == values.size(), otherwise an error will be generated and stored
    public static synchronized int getInt (String table, String column, String condition, ArrayList<String> fields, ArrayList<String> values){
        if(refreshConn() && _noError){
            try {
                StringBuilder sql = new StringBuilder(STR."SELECT `\{column}` FROM `\{table}` WHERE ");
                for (int i = 0; i< fields.size(); ++i){
                    if(i > 0) sql.append(STR."\{condition} ");
                    sql.append(STR."`\{fields.get(i)}`=? ");
                }
                sql.append("LIMIT 1;");
                PreparedStatement query = _conn.prepareStatement(sql.toString());
                for (int i = 0; i< values.size(); ++i) {
                    query.setString(i + 1, values.get(i));
                }
                ResultSet rs = query.executeQuery();
                if(rs.next()) return rs.getInt(column);
            } catch (SQLException e){
                setError(e.getMessage());
            }
        }
        return 0;
    }

    // One column, single Timestamp value return - search by int value (e.g. userID), returns null if value is not found in field
    public static synchronized Timestamp getTs(String table, String column, String field, int value){
        if(refreshConn() && _noError){
            try {
                PreparedStatement query = _conn.prepareStatement(STR."SELECT `\{column}` FROM `\{table}` WHERE `\{field}`=? LIMIT 1;");
                query.setInt(1, value);
                ResultSet rs = query.executeQuery();
                if(rs.next()) return rs.getTimestamp(column);
            } catch (SQLException e){
                setError(e.getMessage());
            }
        }
        return null;
    }

    // Returns the newly generated userID on success, or 0 on failure and stores the error
    public static synchronized int addUser(User user){
        if(refreshConn() && _noError){
            try {
                PreparedStatement query = _conn.prepareStatement(
                        STR."INSERT INTO `\{TBL.USERS}`(`\{COL.USERS.USERNAME}`, `\{COL.USERS.EMAIL}`, `\{COL.USERS.PASSWORD}`, `\{
                        COL.USERS.FIRST_NAME}`, `\{COL.USERS.LAST_NAME}`, `\{COL.USERS.ZIP_CODE}`, `\{COL.USERS.VISIBILITY}`, `\{
                        COL.USERS.CONFIRM_CODE}`) VALUES (?, ?, ?, ?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
                query.setString(1, user.getUsername());
                query.setString(2, user.getEmail());
                query.setString(3, user.getPassword());
                query.setString(4, user.getFirstName());
                query.setString(5, user.getLastName());
                query.setString(6, user.getZipCode());
                query.setShort(7, user.getVisibility());
                query.setString(8, user.getConfirmCode());
                query.executeUpdate();
                ResultSet rs = query.getGeneratedKeys();
                if(rs.next()) return rs.getInt(1);
            } catch (SQLException e){
                setError(e.getMessage());
            }
        }
        return 0;
    }

    public static synchronized User getUser(int userID){
        if(refreshConn() && _noError){
            try{
                PreparedStatement query = _conn.prepareStatement(STR."SELECT * FROM `\{TBL.USERS}` where `\{COL.USERS.ID}`=? LIMIT 1;");
                query.setInt(1, userID);
                ResultSet rs = query.executeQuery();
                if(rs.next()) {
                    return new User(
                            rs.getBoolean(COL.USERS.IS_CONFIRMED),
                            rs.getInt(COL.USERS.ID),
                            rs.getShort(COL.USERS.VISIBILITY),
                            rs.getString(COL.USERS.CONFIRM_CODE),
                            rs.getString(COL.USERS.USERNAME),
                            rs.getString(COL.USERS.EMAIL),
                            rs.getString(COL.USERS.PASSWORD),
                            rs.getString(COL.USERS.FIRST_NAME),
                            rs.getString(COL.USERS.LAST_NAME),
                            rs.getString(COL.USERS.ZIP_CODE),
                            rs.getTimestamp(COL.USERS.TS_CREATED));
                }
            } catch (SQLException e){
                setError(e.getMessage());
            }
        }
        return null;
    }

    public static synchronized ObservableList<User.ComboBoxOption> getCBOptions(String table, String idColumn, String nameColumn){
        if(refreshConn() && _noError){
            try{
                PreparedStatement query = _conn.prepareStatement(STR."SELECT `\{idColumn}`, `\{nameColumn}` FROM `\{table}` ORDER BY `\{nameColumn}` ASC;");
                ResultSet rs = query.executeQuery();
                ObservableList<User.ComboBoxOption> options = FXCollections.observableArrayList();
                while(rs.next()) {
                    options.add(new User.ComboBoxOption(rs.getShort(idColumn), rs.getString(nameColumn)));
                }
                return options;
            } catch (SQLException e){
                setError(e.getMessage());
            }
        }
        return null;
    }

    // Returns true on successful update, false otherwise and stores the error
    public static synchronized boolean setFlag (String table, String column, boolean flag, String field, int value){
        if(refreshConn() && _noError){
            try {
                PreparedStatement query = _conn.prepareStatement(STR."UPDATE `\{table}` SET `\{column}`=? WHERE `\{field}`=?;");
                query.setBoolean(1, flag);
                query.setInt(2, value);
                query.executeUpdate();
                return true;
            } catch (SQLException e){
                setError(e.getMessage());
            }
        }
        return false;
    }
}