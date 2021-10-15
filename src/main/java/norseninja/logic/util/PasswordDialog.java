package norseninja.logic.util;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import norseninja.logic.User;

public class PasswordDialog extends Dialog<Boolean> {
    private boolean running = true;
    private boolean passwordValidated = false;
    PasswordField oldPwField;
    User user;

    public PasswordDialog(User user) {
        this.user = user;
        GridPane pwGridPane = new GridPane();

        Label label = new Label();

        oldPwField = new PasswordField();
        oldPwField.setPromptText("old password");

        PasswordField newPwField = new PasswordField();
        newPwField.setPromptText("new password");

        pwGridPane.add(label,0,1);
        pwGridPane.add(oldPwField, 0, 1);
        pwGridPane.add(newPwField, 0, 2);

        getDialogPane().setContent(pwGridPane);
        getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

        setResultConverter(
                (ButtonType button) -> {
                    if (button == ButtonType.APPLY) {
                        if (validatePassword()) {
                            user.setPassword(oldPwField.getText(),newPwField.getText());
                            passwordValidated = true;
                            return true;
                        } else {
                            label.setText("Old password incorrect");
                        }
                    } else {
                        running = false;
                    }
                    return false;
                }
        );
    }

    private boolean validatePassword() {
        return user.checkPassword(oldPwField.getText());
    }
}
