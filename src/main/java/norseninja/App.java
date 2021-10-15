package norseninja;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import norseninja.logic.TcpServer;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private TcpServer tcpServer;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("primary.fxml"));
        scene = new Scene(fxmlLoader.load());
        PrimaryController primaryController = fxmlLoader.getController();
        tcpServer = primaryController.getTcpServer();
        stage.setScene(scene);
        stage.setTitle("Chatomatic Server");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() throws IOException {
        if (tcpServer.isRunning()) {
            tcpServer.stop();
        }
    }

}