package norseninja.logic;

public class User implements Comparable<User> {
    private String username;
    private String password;
    private String displayName;
    private boolean signedIn;

    public User(String username, String password, String displayName) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.signedIn = false;
    }

    public void updateUsername(String username) {
        this.username = username;
    }

    public boolean setPassword(String oldPassword, String newPassword) {
        if (oldPassword.equals(this.password)) {
            this.password = newPassword;
            return true;
        } else {
            return false;
        }
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUsername() {
        return this.username;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public boolean checkPassword(String password) {
        return (password.equals(this.password));
    }

    public void setSignedIn(boolean status) {
        this.signedIn = status;
    }

    public String getSignedIn() {
        if (this.signedIn) {
            return "online";
        } else {
            return "offline";
        }
    }

    public boolean isSignedIn() {
        return this.signedIn;
    }

    @Override
    public int compareTo(User user) {
        return this.displayName.compareTo(user.displayName);
    }
}
