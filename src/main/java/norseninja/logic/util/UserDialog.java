package norseninja.logic.util;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import norseninja.logic.User;
import norseninja.logic.UserDB;

import java.util.HashMap;
import java.util.Optional;

public class UserDialog extends Dialog<User> {
    private final Mode mode;
    private User user;
    private final HashMap<String, String> validInputs;
    private PasswordDialog pwDialog;
    private UserDB userDB;

    public enum Mode {
        NEW, EDIT, INFO
    }

    public UserDialog(UserDB userDB) {
        this.mode = Mode.NEW;
        this.validInputs = new HashMap<>();
        this.userDB = userDB;

        showContent();
    }

    public UserDialog(User user) {
        this.mode = Mode.INFO;
        this.user = user;
        this.pwDialog = new PasswordDialog(this.user);
        this.validInputs = new HashMap<>();

        showContent();
    }

    public UserDialog(User user, UserDB userDB) {
        super();
        this.mode = Mode.EDIT;
        this.user = user;
        this.validInputs = new HashMap<>();
        this.userDB = userDB;

        System.out.println("showing content");
        showContent();
    }


    private void showContent() {

        Stage stage = (Stage) getDialogPane().getScene().getWindow();

        //Create save button
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        //Create gridPane
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10,50,10,10));

        //Create textFields
        TextField usernameTextField = new TextField();
        usernameTextField.setPromptText("username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("password");
        TextField displayNameTextField = new TextField();
        displayNameTextField.setPromptText("display name");
        Label usernamelabel = new Label();
        usernamelabel.setTextFill(Color.RED);
        Label displayNameLabel = new Label();
        displayNameLabel.setTextFill(Color.RED);

        //Construct UI
        gridPane.add(new Label("Username:"),0, 0);
        gridPane.add(usernameTextField,1,0);
        gridPane.add(usernamelabel, 2,0);

        gridPane.add(new Label("Password:"), 0,1);
        gridPane.add(passwordField,1,1);

        gridPane.add(new Label("Display Name:"),0,2);
        gridPane.add(displayNameTextField,1,2);
        gridPane.add(displayNameLabel,2,2);


        //Add functionality to save button
        Node saveButton = getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        usernameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (null == user) {
                if (!newValue.isBlank() && userDB.usernameAvailable(newValue)) {
                    validInputs.put("username", usernameTextField.getText());
                    usernamelabel.setText("");
                } else if (!newValue.isBlank()) {
                    validInputs.remove("username");
                    usernamelabel.setText("username is taken");
                } else {
                    validInputs.remove("username");
                    usernamelabel.setText("");
                }
            } else {
                if (!newValue.isBlank() && userDB.usernameAvailable(newValue)) {
                    validInputs.put("username", usernameTextField.getText());
                    usernamelabel.setText("");
                } else if (newValue.isBlank() && !newValue.equals(user.getUsername())) {
                    usernamelabel.setText("username is taken");
                    validInputs.remove("username");
                } else {
                    usernamelabel.setText("");
                    validInputs.remove("username");
                }
            }
            saveButton.setDisable(validInputs.size() < 3);
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isBlank()) {
                validInputs.put("password", passwordField.getText());
            } else {
                validInputs.remove("password");
            }
            saveButton.setDisable(validInputs.size() < 3);
        });

        displayNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (null == user) {
                if (!newValue.isBlank() && userDB.displayNameAvailable(newValue)) {
                    validInputs.put("displayName", usernameTextField.getText());
                    displayNameLabel.setText("");
                } else if (!newValue.isBlank()) {
                    validInputs.remove("displayName");
                    displayNameLabel.setText("display name is taken");
                } else {
                    validInputs.remove("displayName");
                    displayNameLabel.setText("");
                }
            } else {
                if (!newValue.isBlank() && userDB.displayNameAvailable(newValue)) {
                    validInputs.put("displayName", displayNameTextField.getText());
                    displayNameLabel.setText("");
                } else if (!newValue.isBlank() && !newValue.equals(user.getDisplayName())) {
                    validInputs.remove("displayName");
                    displayNameLabel.setText("display name taken");
                } else {
                    validInputs.remove("displayName");
                    displayNameLabel.setText("");
                }
            }
            saveButton.setDisable(validInputs.size() < 3);
        });

        switch (this.mode) {
            case NEW -> stage.setTitle("Create New User");

            case EDIT -> {
                stage.setTitle("Edit User");
                usernameTextField.setText(user.getUsername());
                displayNameTextField.setText(user.getDisplayName());
                passwordField.setVisible(false);
                saveButton.setDisable(false);
                validInputs.put("username", user.getUsername());
                validInputs.put("displayName", user.getDisplayName());
                validInputs.put("placeholder", "placeholder");

                Button updatePasswordButton = new Button("Change Password");
                updatePasswordButton.setOnAction(e -> {
                    if (updatePassword(user)) {
                        updatePasswordButton.setText("Password Changed");
                    }
                });
                gridPane.add(updatePasswordButton,1,1);
            }

            case INFO -> {
                stage.setTitle("User Information");
                getDialogPane().getButtonTypes().setAll(ButtonType.CLOSE);
                usernameTextField.setEditable(false);
                passwordField.setEditable(false);
                displayNameTextField.setEditable(false);
            }
        }

        getDialogPane().setContent(gridPane);
        usernameTextField.requestFocus();

        setResultConverter(
                (ButtonType button) -> {
                    if (this.mode == Mode.NEW && button == saveButtonType) {
                        String username = usernameTextField.getText();
                        String password = passwordField.getText();
                        String displayName = displayNameTextField.getText();

                        user = new User(username, password, displayName);
                    } else if (this.mode == Mode.EDIT && button == saveButtonType) {
                        user.updateUsername(usernameTextField.getText());
                        user.setDisplayName(displayNameTextField.getText());
                    } else {
                        user = null;
                    }
                    return user;
                }
        );
    }

    private boolean updatePassword(User user) {
        this.pwDialog = new PasswordDialog(user);
        Optional<Boolean> pwUpdated = pwDialog.showAndWait();
        return pwUpdated.orElse(false);
    }
}
