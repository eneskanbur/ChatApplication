package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField usernameField;

    private ChatClient mainApp;

    public void setMainApp(ChatClient mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleLogin() {
    String username = usernameField.getText().trim();
    if (!username.isEmpty()) {
        try {
            Socket checkSocket = new Socket("localhost", 8888);
            BufferedReader checkInput = new BufferedReader(new InputStreamReader(checkSocket.getInputStream()));
            PrintWriter checkOutput = new PrintWriter(checkSocket.getOutputStream(), true);

            checkOutput.println("CHECK_USERNAME:" + username);
            
            String response = checkInput.readLine();
            checkSocket.close();

            if ("USERNAME_AVAILABLE".equals(response)) {
                mainApp.showChatWindow(username);
                
                ((Stage)usernameField.getScene().getWindow()).close();
            } else {
                
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Error");
                alert.setHeaderText("Username already taken");
                alert.setContentText("Please choose a different username.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
}
