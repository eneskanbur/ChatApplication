package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatClient extends Application {
    private static Socket socket;
    private static BufferedReader input;
    private static PrintWriter output;
    private static String username;
    private static ChatController chatController;
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
   
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/view/login-view.fxml"));
        Parent loginRoot = loginLoader.load();
        LoginController loginController = loginLoader.getController();
        loginController.setMainApp(this);
        Stage loginStage = new Stage();
        loginStage.setTitle("Login");
        loginStage.setScene(new Scene(loginRoot, 300, 200));
        loginStage.show();
    }

    public void showChatWindow(String username) throws Exception {
        ChatClient.username = username; 
        socket = new Socket("localhost", 8888);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
        output.println(username);

        
        FXMLLoader chatLoader = new FXMLLoader(getClass().getResource("/view/chat-view.fxml"));
        Parent chatRoot = chatLoader.load();
        chatController = chatLoader.getController();
        chatController.setClient(this);

        Stage chatStage = new Stage();
        chatStage.setTitle("Chat Application - " + username);
        chatStage.setScene(new Scene(chatRoot, 800, 600));
        chatStage.show();
        new Thread(this::listenForMessages).start();
    }

    private void listenForMessages() {
        try {
            String message;
            while ((message = input.readLine()) != null) {
                final String finalMessage = message;
                Platform.runLater(() -> handleServerMessage(finalMessage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleServerMessage(String message) {
        if (message.startsWith("USER_LIST:")) {
            String[] users = message.substring(10).split(",");
            chatController.updateUserList(users);
        } else if (message.startsWith("INCOMING_CHAT_REQUEST:")) {
            String requester = message.substring(22);
            chatController.showChatRequest(requester);
        } else if (message.startsWith("CHAT_REQUEST_ACCEPT:")) {
            String acceptedUser = message.substring(20);
            chatController.startChat(acceptedUser);
        } else if (message.startsWith("CHAT_REQUEST_REJECT:")) {
            String rejectedUser = message.substring(20);
            chatController.showChatRejected(rejectedUser);
        } else if (message.startsWith("MESSAGE:")) {
            String[] parts = message.substring(8).split(":", 3);
            boolean isGroupMessage = parts.length == 3 && parts[2].equals("GROUP");
            chatController.receiveMessage(parts[0],
                    isGroupMessage ? parts[1] : parts[1],
                    isGroupMessage
            );
        } else if (message.startsWith("GROUP_INVITE:")) {
            String[] parts = message.split(":");
            chatController.showGroupInvite(parts[1], parts[2]);
        } else if (message.startsWith("GROUP_CREATED:")) {
            String groupName = message.substring(14);
            chatController.groupCreated(groupName);
        } else if (message.startsWith("GROUP_MESSAGE:")) {
            String[] parts = message.split(":", 4);
            chatController.receiveGroupMessage(parts[1], parts[2], parts[3]);
        } else if (message.startsWith("USER_JOINED_GROUP:")) {
            String[] parts = message.split(":");
            chatController.userJoinedGroup(parts[1], parts[2]);
        } else if (message.startsWith("USER_LEFT_GROUP:")) {
            String[] parts = message.split(":");
            chatController.userLeftGroup(parts[1], parts[2]);
        }
    }

    public void sendChatRequest(String targetUser) {
        output.println("CHAT_REQUEST:" + targetUser);
    }

    public void sendChatResponse(String requester, boolean accepted) {
        output.println("CHAT_RESPONSE:" + requester + ":" + (accepted ? "ACCEPT" : "REJECT"));
    }

    public void sendMessage(String targetUser, String message) {
        output.println("SEND_MESSAGE:" + targetUser + ":" + message);
    }

    public void sendGroupMessage(String groupName, String message) {
        output.println("SEND_GROUP_MESSAGE:" + groupName + ":" + message);
    }

    public void createGroup(String groupName) {
        output.println("CREATE_GROUP:" + groupName);
    }

    public void inviteToGroup(String groupName, String username) {
        output.println("INVITE_TO_GROUP:" + groupName + ":" + username);
    }

    public void acceptGroupInvite(String groupName) {
        output.println("ACCEPT_GROUP_INVITE:" + groupName);
    }

    public void leaveGroup(String groupName) {
        output.println("LEAVE_GROUP:" + groupName);
    }

    public void disconnect() {
        output.println("DISCONNECT");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void addUserToGroup(String groupName, String username) {
    output.println("ADD_TO_GROUP:" + groupName + ":" + username);
}
    public String getUsername() {
    return username;
}
}
