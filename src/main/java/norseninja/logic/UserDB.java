package norseninja.logic;

import java.util.*;
import java.util.stream.Collectors;

public class UserDB {
    private final HashMap<String, User> users;

    public UserDB() {
        this.users = new HashMap<>();
    }

    public boolean addUser(String username, String password, String displayName) {
        if (this.users.containsKey(username)) {
            return false;
        } else {
            this.users.put(username, new User(username, password, displayName));
            return true;
        }
    }

    public boolean addUser(User user) {
        if (this.users.containsKey(user.getUsername())) {
            return false;
        } else {
            this.users.put(user.getUsername(), user);
            return true;
        }
    }

    public Map<String, User> getAllUsers() {
        return users;
    }

    public List<User> getUserList() {
        return this.users.values().stream().sorted().collect(Collectors.toList());
    }

    public List<User> getSignedIn() {
        ArrayList<User> signedInUsers = new ArrayList<>();
        this.users.values().forEach(user -> {
            if (user.isSignedIn()) {
                signedInUsers.add(user);
            }
        });
        return signedInUsers;
    }

    /**
     * Searches for a user by display- or user name.
     * @param name the displayName to find.
     * @return {@code User} found user, or {@code NULL} if no user is found.
     */
    public User getUserByDisplayName(String name) {
        User returnUser = null;
        Iterator<User> it = users.values().iterator();
        boolean searching = true;
        while (it.hasNext() && searching) {
            User u = it.next();
            if (u.getDisplayName().equals(name)) {
                returnUser = u;
                searching = false;
            }
        }
        return returnUser;
    }

    public boolean editUsername(String oldUsername, String newUsername) {
        User user = this.users.get(oldUsername);
        if (null != user && !usernameAvailable(newUsername)) {
                user.updateUsername(newUsername);
                return true;
        }
        return false;
    }

    public boolean editDisplayName(String username, String newDisplayName) {
        User user = this.users.get(username);
        if (null != user && !displayNameAvailable(newDisplayName)) {
            user.setDisplayName(newDisplayName);
            return true;
        }
        return false;
    }

    public boolean removeUser(String username) {
        if (this.users.containsKey(username)) {
            this.users.remove(username);
            return true;
        } else {
            return false;
        }
    }

    public User login(String username, String password) {
        if (users.containsKey(username) && users.get(username).checkPassword(password)) {
            return users.get(username);
        }
        return null;
    }

    public boolean usernameAvailable(String username) {
        User user = this.users.get(username);
        return null == user;
    }

    public boolean displayNameAvailable(String displayName) {
        for (User user : this.users.values()) {
            if (user.getDisplayName().equalsIgnoreCase(displayName)) {
                return false;
            }
        }
        return true;
    }
}
