package app.sportslink;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

public class User {
    public static class CFG{
        private CFG(){}
        public static final int USERNAME_MIN_LEN = 2;
        public static final int PASSWORD_MIN_LEN = 3;
        public static final int EMAIL_MIN_LEN = 5;
        public static final int FIRST_NAME_MIN_LEN = 1;
        public static final int LAST_NAME_MIN_LEN = 1;
        public static final int ZIP_CODE_MIN_LEN = 5;
        public static final int USERNAME_MAX_LEN = DB.getMaxLength(DB.TBL.USERS, DB.COL.USERS.USERNAME);
        public static final int PASSWORD_MAX_LEN = DB.getMaxLength(DB.TBL.USERS, DB.COL.USERS.PASSWORD);
        public static final int EMAIL_MAX_LEN = DB.getMaxLength(DB.TBL.USERS, DB.COL.USERS.EMAIL);
        public static final int FIRST_NAME_MAX_LEN = DB.getMaxLength(DB.TBL.USERS, DB.COL.USERS.FIRST_NAME);
        public static final int LAST_NAME_MAX_LEN = DB.getMaxLength(DB.TBL.USERS, DB.COL.USERS.LAST_NAME);
        public static final int ZIP_CODE_MAX_LEN = DB.getMaxLength(DB.TBL.USERS, DB.COL.USERS.ZIP_CODE);
        public static final String MATCH_ANY = "^.*$";                          // Matches anything, including empty strings, does NOT match newline/carriage return/line feed
        public static final String MATCH_USERNAME = "^[a-zA-Z0-9.\\-_]+$";      // Matches any letters, numbers, and chars . (dot), _ (underscore), and - (minus)
        public static final String MATCH_EMAIL = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";       // Matches email addresses according to OWASP Validation Regex Repository
        public static final String MATCH_ZIP_CODE = "^[0-9]+$";                  // Matches any number, with any number of digits
    }

    public static class ComboBoxOption{
        private final short _id;
        private final String _name;
        public ComboBoxOption(short id, String name){
            _id = id;
            _name = name;
        }
        public short getId(){ return _id; }
        public String getName(){ return _name; }
        @Override
        public String toString() {
            return this.getName();
        }
    }

    private boolean _confirmed;                 // true = email confirmation code entered successfully and account is active
    private int _userID;                        // user idx, as stored in the database
    private short _visibility;                  // 0 = private, 1 = public, 2 = protected (2 -> To be implemented later)
    private String _confirmCode;                // email confirmation code for signup and password reset
    private String _username;
    private String _email;
    private String _password;                   // stores SHA-256 of real password
    private String _firstName;
    private String _lastName;
    private String _zipCode;
    private Timestamp _tsCreated;               // timestamp when user account was created -> ToDo: convert to user's local time
    private static String _errorMsg = null;
    private boolean _noError = true;            // true = user account is valid, created successfully, code confirmation email sent, etc., false = any error

    private static ObservableList<ComboBoxOption> _cbOptsVisibility = null;
    private static ObservableList<ComboBoxOption> _cbOptsSports = null;
    private static ObservableList<ComboBoxOption> _cbOptsLevels = null;

    // Create a new user and add it to the database + send email with code to confirm email address:
    User(String username, String email, String password, String firstName, String lastName, String zipCode, short visibility){
        _password = Utils.getSHA(password);
        if(_password == null){
            _noError = false;
            _errorMsg = "The password you chose cannot be encrypted properly. Please try a different one.";
        }
        else {
            _confirmed = false;
            _confirmCode = newConfirmCode();
            _visibility = visibility;
            _username = username;
            _email = email;
            _firstName = firstName;
            _lastName = lastName;
            _zipCode = zipCode;
            _userID = DB.addUser(this);
            if(_userID == 0){
                _noError = false;
                _errorMsg = "There seems to be a database error when trying to create your user account. Please try again.";
            }
            else {
                _tsCreated = getTsCreated(_userID);
                if(_tsCreated == null){
                    _noError = false;
                    _errorMsg = "There seems to be a database error when trying to confirm your user account creation. Please contact us.";
                }
                else if(!Email.sendAccountCreated(this)){
                    _noError = false;
                    _errorMsg = "Your email confirmation code could not be sent. Please log in and select the option to send you another confirmation code.";
                }
            }
        }
    }

    // Create a User object based on data received as parameters (from the database, from an existing user's data):
    User(boolean isConfirmed, int userID, short visibility, String confirmCode, String username, String email, String password, String firstName, String lastName, String zipCode, Timestamp tsCreated){
        _confirmed = isConfirmed;
        _userID = userID;
        _visibility = visibility;
        _confirmCode = confirmCode;
        _username = username;
        _email = email;
        _password = password;
        _firstName = firstName;
        _lastName = lastName;
        _zipCode = zipCode;
        _tsCreated = tsCreated;
    }

    public boolean isError(){ return !_noError; }

    public String getError(){
        if(isError()) return _errorMsg;
        return null;
    }

    // Generate a new email confirmation/password reset code:
    private String newConfirmCode(){
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; ++i) code.append((int)(Math.random() * 10));
        return code.toString();
    }

    public boolean isConfirmed(){ return _confirmed; }
    public int getID(){ return _userID; }
    public short getVisibility(){ return _visibility; }
    public String getConfirmCode(){ return _confirmCode; }
    public String getUsername(){ return _username; }
    public String getEmail(){ return _email; }
    public String getPassword(){ return _password; }
    public String getFirstName(){ return _firstName; }
    public String getLastName(){ return _lastName; }
    public String getZipCode(){return _zipCode; }
    public Timestamp getTsCreated(){ return _tsCreated; }

    // Confirms user account with email code, returns true on successful update, false otherwise
    public boolean setConfirmed(String code){
        if(!_confirmed && _confirmCode.equals(code)) {
            _confirmed = DB.setFlag(DB.TBL.USERS, DB.COL.USERS.IS_CONFIRMED, true, DB.COL.USERS.ID, _userID);
            return _confirmed;
        }
        return false;
    }

    // Verify user input data format according to min & max length + chars allowed
    // Does NOT remove or escape any characters, all fields except password should be stripped
    public static boolean isInvalidForm(String str, int minLen, int maxLen, String match){
        return !(str.length() >= minLen && str.length() <= maxLen && str.matches(match));
    }

    // Verify if username already exists in database
    public static boolean usernameExists(String username){
        return DB.exists(DB.TBL.USERS, DB.COL.USERS.USERNAME, username);
    }

    // Verify if email address already exists in database
    public static boolean emailExists(String email){
        return DB.exists(DB.TBL.USERS, DB.COL.USERS.EMAIL, email);
    }

    // Verify if the username + password combination represents a valid login
    public static boolean isValidLogin(String username, String password){
        return DB.exists(DB.TBL.USERS, DB.COL.USERS.USERNAME, DB.COL.USERS.PASSWORD, username, Utils.getSHA(password));
    }

    // Returns a valid userID from the database, returns 0 if not found or not matching username & password
    public static int getUserID(String username, String password){
        ArrayList<String> fields = new ArrayList<>(Arrays.asList(DB.COL.USERS.USERNAME, DB.COL.USERS.PASSWORD));
        ArrayList<String> values = new ArrayList<>(Arrays.asList(username, Utils.getSHA(password)));
        return DB.getInt(DB.TBL.USERS, DB.COL.USERS.ID, "AND", fields, values);
    }

    // Returns a valid userID's account created timestamp from the database, returns null if userID doesn't exist
    public static Timestamp getTsCreated(int userID){
        return DB.getTs(DB.TBL.USERS, DB.COL.USERS.TS_CREATED, DB.COL.USERS.ID, userID);
    }

    // Returns a String to be outputted to the user if the new account signup fields are filled incorrectly, if String isEmpty() then all OK
    // The String arguments received by this function should be already strip() processed, except password and passwordVerify
    public static String verifySignup(String firstName, String lastName, String email, String zipCode, String username, String password, String passwordVerify, boolean termsAgree){
        StringBuilder errorMsg = new StringBuilder();
        if(User.isInvalidForm(firstName, User.CFG.FIRST_NAME_MIN_LEN, User.CFG.FIRST_NAME_MAX_LEN, User.CFG.MATCH_ANY))
            errorMsg.append(STR."First name must be at least \{User.CFG.FIRST_NAME_MIN_LEN} and at most \{User.CFG.FIRST_NAME_MAX_LEN} characters long.\n");
        if(User.isInvalidForm(lastName, User.CFG.LAST_NAME_MIN_LEN, User.CFG.LAST_NAME_MAX_LEN, User.CFG.MATCH_ANY))
            errorMsg.append(STR."Last name must be at least \{User.CFG.LAST_NAME_MIN_LEN} and at most \{User.CFG.LAST_NAME_MAX_LEN} characters long.\n");
        if(User.isInvalidForm(email, User.CFG.EMAIL_MIN_LEN, User.CFG.EMAIL_MAX_LEN, User.CFG.MATCH_EMAIL))
            errorMsg.append("The email address you entered is invalid.\n");
        else if (User.emailExists(email))
            errorMsg.append("Your email has already been registered. To access the account, you can follow the \"Forgot Password\" procedure.\n");
        if(User.isInvalidForm(zipCode, User.CFG.ZIP_CODE_MIN_LEN, User.CFG.ZIP_CODE_MAX_LEN, User.CFG.MATCH_ZIP_CODE))
            errorMsg.append("The ZIP code you entered is invalid. Only 5-digit US ZIP codes are allowed.\n");
        if(User.isInvalidForm(username, User.CFG.USERNAME_MIN_LEN, User.CFG.USERNAME_MAX_LEN, User.CFG.MATCH_USERNAME))
            errorMsg.append(STR."Your username must be at least \{User.CFG.USERNAME_MIN_LEN} and at most \{User.CFG.USERNAME_MAX_LEN} characters long. Only letters, numbers, and characters \".\", \"_\", and \"-\" are allowed.\n");
        else if (User.usernameExists(username))
            errorMsg.append("The username you entered already belongs to another account. Please choose a different username.\n");
        if(User.isInvalidForm(password, User.CFG.PASSWORD_MIN_LEN, User.CFG.PASSWORD_MAX_LEN, User.CFG.MATCH_ANY))
            errorMsg.append(STR."Your password must be at least \{User.CFG.PASSWORD_MIN_LEN} and at most \{User.CFG.PASSWORD_MAX_LEN} characters long.\n");
        else if(!password.equals(passwordVerify))
            errorMsg.append("The passwords you entered don't match. Please make sure the passwords in both fields are identical.\n");
        if(!termsAgree)
            errorMsg.append("You need to agree with the \"Terms and Conditions\" to be able to register.\n");
        return errorMsg.toString();
    }

    public static ObservableList<ComboBoxOption> getVisibilityOpts(){
        if (_cbOptsVisibility == null) {
            _cbOptsVisibility = FXCollections.observableArrayList();
            _cbOptsVisibility.add(new ComboBoxOption((short) 1, "Public (Recommended)"));
            _cbOptsVisibility.add(new ComboBoxOption((short) 0, "Private"));
        }
        return _cbOptsVisibility;
    }

    public static ObservableList<ComboBoxOption> getSportsOpts(){
        if (_cbOptsSports == null) {
            _cbOptsSports = DB.getCBOptions(DB.TBL.SPORTS, DB.COL.SPORTS.ID, DB.COL.SPORTS.NAME);
            if (_cbOptsSports != null)
                _cbOptsSports.addFirst(new ComboBoxOption((short) 0, "All Sports"));
        }
        return _cbOptsSports;
    }

    public static ObservableList<ComboBoxOption> getLevelOpts(){
        if (_cbOptsLevels == null) {
            _cbOptsLevels = FXCollections.observableArrayList();
            _cbOptsLevels.add(new ComboBoxOption((short) 0, "All Levels"));
            _cbOptsLevels.add(new ComboBoxOption((short) 1, "Beginner"));
            _cbOptsLevels.add(new ComboBoxOption((short) 2, "Intermediate"));
            _cbOptsLevels.add(new ComboBoxOption((short) 3, "Expert"));
        }
        return _cbOptsLevels;
    }
}