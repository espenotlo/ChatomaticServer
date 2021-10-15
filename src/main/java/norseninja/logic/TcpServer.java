package norseninja.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.List;

public class TcpServer {
    private final UserDB userDB;
    private final MessageDB messageDB;
    private static final int PORT = 1301;
    private ServerSocket welcomeSocket;
    private boolean running = false;

    public TcpServer(UserDB userDB, MessageDB messageDB) {
        this.userDB = userDB;
        this.messageDB = messageDB;
    }

    public void run() throws IOException {
        this.running = true;
        try {
            this.welcomeSocket = new ServerSocket(PORT);
            log("Server started on port " + PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (running) {
            Socket connectionSocket = welcomeSocket.accept();
            log("new client on port: " + connectionSocket.getPort());
            establishConnectionOnNewThread(connectionSocket);
        }
        log("ERROR: the server should never go out of the run() method! After handling one client");
    }

    public boolean isRunning() {
        return this.running;
    }

    public void stop() throws IOException {
        this.running = false;
        welcomeSocket.close();
        log("Server shut down");
    }

    private void establishConnectionOnNewThread(Socket connectionSocket) {
        Runnable taskToBeExecutedOnAnotherThread = () -> {
            try {
                long threadId = Thread.currentThread().getId();
                log("Starting a client on thread #" + threadId);
                handleClient(connectionSocket);
                log("Done processing client on thread #" + threadId);
            } catch (IOException e) {
                Thread.currentThread().interrupt();
            }
        };
        Thread t = new Thread(taskToBeExecutedOnAnotherThread);
        t.setDaemon(true);
        t.start();
    }

    private void handleClient(Socket connectionSocket) throws IOException {
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        PrintWriter outToClient = new PrintWriter(connectionSocket.getOutputStream(), true);
        String clientSentence;
        String returnSentence = "";
        User user = null;
        boolean loggedIn = false;

        while (!connectionSocket.isClosed() && running) {
            clientSentence = inFromClient.readLine();
            String[] clientArray = clientSentence.split("/%");

            //Make sure user logs in first
            if (!loggedIn && clientArray[0].equals("login")) {
                if (userDB.getAllUsers().containsKey(clientArray[1])) {
                    user = userDB.login(clientArray[1], clientArray[2]);
                    if (null != user) {
                        loggedIn = true;
                        user.setSignedIn(true);
                        returnSentence = "ok: logged in as " + user.getDisplayName();
                    } else {
                        returnSentence = "error: invalid credentials";
                    }
                }
            } else if (!loggedIn && clientArray[0].equals("end")) {
                disconnect(connectionSocket);
            } else if (!loggedIn) {
                returnSentence = "error: not logged in";
            } else {
                //User is logged in
                switch (clientArray[0]) {
                    case "getmsg" -> returnSentence = deliverMessages(user, clientArray[1]);
                    case "getactive" -> returnSentence = getActiveUsers();
                    case "getusers" -> returnSentence = getAllUsers();
                    case "message" -> returnSentence = readMessage(user, clientArray);
                    case "logout" -> returnSentence = logout(user, connectionSocket);
                    case "getme" -> returnSentence = user.getDisplayName();
                    case "password" -> returnSentence = checkPassword(user, clientArray[1]);
                    case "editname" -> returnSentence = editDisplayName(user, clientArray[1]);
                    case "editpw" -> returnSentence = editPassword(user, clientArray[1], clientArray[2]);
                    default -> returnSentence = "error: unrecognized command";
                }
            }
            outToClient.println(returnSentence);
        }
        if (!running) {
            disconnect(connectionSocket);
        }
    }

    private String editDisplayName(User user, String name) {
        user.setDisplayName(name);
        return "ok: name changed";
    }

    private String editPassword(User user, String oldPassword, String newPassword) {
        if (user.setPassword(oldPassword,newPassword)) {
            return "ok: password updated";
        } else {
            return "error: wrong password";
        }
    }

    private String checkPassword(User user, String password) {
        if (user.checkPassword(password)) {
            return "ok";
        } else {
            return "error: wrong password";
        }
    }

    private void disconnect(Socket connectionSocket) {
        try {
            connectionSocket.close();
            log("Connection to client on port " + connectionSocket.getPort() + " terminated");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String logout(User user, Socket connectionSocket) {
        user.setSignedIn(false);
        disconnect(connectionSocket);
        return "ok: logged out";
    }

    private String readMessage(User fromUser, String[] array) {
        String returnSentence;
        User recipient = userDB.getUserByDisplayName(array[1]);
        if (null != recipient) {
            messageDB.addMessage(new Message(fromUser.getUsername(), recipient.getUsername(), array[2]));
            returnSentence = "ok: message received";
        } else if (array[1].equalsIgnoreCase("all")) {
            messageDB.addMessage(new Message(fromUser.getUsername(), array[1], array[2]));
            returnSentence = "ok: message received";
        } else {
            returnSentence = "error: message failed";
        }
        return returnSentence;
    }

    private String getActiveUsers() {
        StringBuilder sb = new StringBuilder("ok/%all");
        userDB.getSignedIn().forEach(u -> sb.append("/%").append(u.getDisplayName()));
        return sb.toString();
    }

    private String getAllUsers() {
        StringBuilder sb = new StringBuilder("ok/%all");
        userDB.getUserList().forEach(u -> sb.append("/%").append(u.getDisplayName()));
        return sb.toString();
    }

    private void log(String message) {
        String threadID = "THREAD #" + Thread.currentThread().getId() + ": ";
        System.out.println(threadID + message);
    }


    private String deliverMessages(User user, String lastDelivered) {
        StringBuilder sb = new StringBuilder("ok");
        List<Message> messages;
        if (!lastDelivered.equalsIgnoreCase("null")) {
            messages = this.messageDB.getUndelivered(user, LocalTime.parse(lastDelivered));
        } else {
            messages = this.messageDB.getAllToOrFromUser(user);
        }
        messages.forEach(message -> sb.append("/%").append(message.getTimeStamp())
                .append("/%").append(message.getFromUser())
                .append("/%").append(message.getToUser())
                .append("/%").append(message.getMessageText()));
        return sb.toString();
    }
}
