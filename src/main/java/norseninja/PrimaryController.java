package norseninja;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import norseninja.logic.*;
import norseninja.logic.util.UserDialog;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Optional;

public class PrimaryController {

    @FXML private Label statusLabel;
    @FXML private ScrollPane mainScrollPane;
    @FXML private Button button1;
    @FXML private Button button2;
    @FXML private Button button3;

    private TcpServer tcpServer;
    private TableView<User> userTableView;
    private TableView<Message> messageTableView;
    private UserDB userDB;
    private MessageDB messageDB;
    private ObservableList<User> userListWrapper;
    private ObservableList<Message> messageListWrapper;
    private boolean running = true;

    @FXML
    private void initialize() {
        this.userDB = new UserDB();
        this.messageDB = new MessageDB();
        this.tcpServer = new TcpServer(this.userDB, this.messageDB);
        setUserTable();

    }

    public TcpServer getTcpServer() {
        return this.tcpServer;
    }

    private void updateListWrappers() {
        if (this.mainScrollPane.getContent().equals(this.userTableView)) {
            this.userListWrapper.setAll(this.userDB.getAllUsers().values());
        } else if (this.mainScrollPane.getContent().equals(this.messageTableView)){
            this.messageListWrapper.setAll(this.messageDB.getMessages());
        }
    }

    private void setUserTable() {
        TableColumn<User, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setMinWidth(75);
        usernameColumn.setMaxWidth(150);

        TableColumn<User, String> displayNameColumn = new TableColumn<>("Display Name");
        displayNameColumn.setMinWidth(100);
        displayNameColumn.setMaxWidth(150);
        displayNameColumn.setCellValueFactory(new PropertyValueFactory<>("displayName"));


        TableColumn<User, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setMinWidth(75);
        statusColumn.setMaxWidth(100);
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("signedIn"));
        statusColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!isEmpty()) {
                    this.setTextFill(Color.RED);
                    if (item.contains("online"))
                        this.setTextFill(Color.GREEN);
                    setText(item);
                } else {
                    this.setText(null);
                }
            }
        });

        this.userListWrapper = FXCollections.observableArrayList(userDB.getAllUsers().values());

        this.userTableView = new TableView<>();
        this.userTableView.setPrefSize(Region.USE_COMPUTED_SIZE,Region.USE_COMPUTED_SIZE);
        this.userTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.userTableView.getColumns().clear();
        this.userTableView.getColumns().addAll(usernameColumn,displayNameColumn, statusColumn);
        this.userTableView.setItems(userListWrapper);

        mainScrollPane.setContent(this.userTableView);
    }

    private void setMessagesTable() {
        TableColumn<Message, LocalTime> timeStampColumn = new TableColumn<>("Timestamp");
        timeStampColumn.setCellValueFactory(new PropertyValueFactory<>("timeStamp"));
        timeStampColumn.setMinWidth(75);
        timeStampColumn.setMaxWidth(150);

        TableColumn<Message, String> fromUserColumn = new TableColumn<>("From");
        fromUserColumn.setCellValueFactory(new PropertyValueFactory<>("fromUser"));
        fromUserColumn.setMinWidth(75);
        fromUserColumn.setMaxWidth(150);

        TableColumn<Message, String> toUserColumn = new TableColumn<>("To");
        toUserColumn.setCellValueFactory(new PropertyValueFactory<>("toUser"));
        toUserColumn.setMinWidth(75);
        toUserColumn.setMaxWidth(150);

        TableColumn<Message, String> messageTextColumn = new TableColumn<>("Message");
        messageTextColumn.setCellValueFactory(new PropertyValueFactory<>("messageText"));
        messageTextColumn.setMinWidth(75);

        this.messageListWrapper = FXCollections.observableArrayList(messageDB.getMessages());

        this.messageTableView = new TableView<>();
        this.messageTableView.setPrefSize(Region.USE_COMPUTED_SIZE,Region.USE_COMPUTED_SIZE);
        this.messageTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.messageTableView.getColumns().clear();
        this.messageTableView.getColumns().addAll(timeStampColumn,fromUserColumn,toUserColumn,messageTextColumn);
        this.messageTableView.setItems(messageListWrapper);

        mainScrollPane.setContent(this.messageTableView);
    }

    @FXML
    private void addUserButtonClicked() {
        UserDialog userDialog = new UserDialog(this.userDB);

        Optional<User> result = userDialog.showAndWait();
        result.ifPresent(user -> {
            if (this.userDB.addUser(user)) {
                this.statusLabel.setText("Successfully added " + user.getUsername());
                this.userListWrapper.setAll(userDB.getAllUsers().values());
            } else {
                this.statusLabel.setText("Failed to add " + user.getUsername());
            }
        });
    }

    @FXML
    private void editUserButtonClicked() {
        User user = this.userTableView.getSelectionModel().getSelectedItem();
        if (null != user) {
            UserDialog userDialog = new UserDialog(user, this.userDB);
            userDialog.showAndWait();
            this.userListWrapper.setAll(userDB.getAllUsers().values());
        }
    }

    private void addMessageButtonClicked() {
        this.messageDB.addMessage(new Message("me", "you", "Hello!"));
        this.messageDB.addMessage(new Message("you", "me", "Hello back!"));
        this.messageListWrapper.setAll(messageDB.getMessages());
    }


    private void editMessageButtonClicked() {

    }

    public void updateGUI() {
        Runnable taskToBeExecutedOnAnotherThread = () -> {
            while (this.running) {
                Platform.runLater(this::updateListWrappers);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        Thread t = new Thread(taskToBeExecutedOnAnotherThread);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void startOrStopTcpServer() throws IOException {
        if (!this.tcpServer.isRunning()) {
            this.running = true;
            updateGUI();
            this.button3.setText("Stop Server");
            Runnable taskToBeExecutedOnAnotherThread = () -> {
                try {
                    this.tcpServer.run();
                } catch (IOException e) {
                    Thread.currentThread().interrupt();
                }
            };
            Thread t = new Thread(taskToBeExecutedOnAnotherThread);
            t.setDaemon(true);
            t.start();
        } else {
            this.running = false;
            this.tcpServer.stop();
            this.button3.setText("Start Server");
        }
    }

    @FXML
    private void showUsersButtonClicked() {
        setUserTable();
        button1.setText("Add User");
        button2.setText("Edit User");

        button1.setOnAction(e -> addUserButtonClicked());
        button2.setOnAction(e -> editUserButtonClicked());

        button1.setDisable(false);
        button2.setDisable(false);
    }

    @FXML
    private void showMessagesButtonClicked() {
        setMessagesTable();

        button1.setText("New Message");
        button2.setText("Edit Message");

        button1.setOnAction(e -> addMessageButtonClicked());
        button2.setOnAction(e -> editMessageButtonClicked());

        button1.setDisable(true);
        button2.setDisable(true);
    }
}
