package client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ChatController {
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField messageField;
    @FXML
    private ListView<String> userListView;
    @FXML
    private Button sendButton;
    @FXML
    private Button createGroupButton;
    @FXML
    private VBox groupsVBox;

    private ChatClient client;
    private String currentChatPartner;
    private String currentGroupChat;
    private boolean isGroupChat = false;

    private Map<String, Set<String>> groups = new HashMap<>();

    public void setClient(ChatClient client) {
        this.client = client;
        createGroupButton.setOnAction(event -> showCreateGroupDialog());
    }

    private void showCreateGroupDialog() {
    Dialog<Set<String>> dialog = new Dialog<>();
    dialog.setTitle("Create Group");
    dialog.setHeaderText("Select users for the group");

    ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

    ListView<String> userSelectionList = new ListView<>();
    userSelectionList.getItems().addAll(
        userListView.getItems().stream()
            .filter(user -> !user.equals(client.getUsername()) && 
                    (currentGroupChat == null || !groups.get(currentGroupChat).contains(user)))
            .collect(Collectors.toList())
    );
    userSelectionList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    dialog.getDialogPane().setContent(userSelectionList);

    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == createButtonType) {
            return userSelectionList.getSelectionModel().getSelectedItems().stream().collect(Collectors.toSet());
        }
        return null;
    });

    dialog.showAndWait().ifPresent(selectedUsers -> {
        if (!selectedUsers.isEmpty()) {
            TextInputDialog groupNameDialog = new TextInputDialog();
            groupNameDialog.setTitle("Group Name");
            groupNameDialog.setHeaderText("Enter a name for your group");
            groupNameDialog.setContentText("Group Name:");

            groupNameDialog.showAndWait().ifPresent(groupName -> {
                client.createGroup(groupName);

                for (String user : selectedUsers) {
                    client.inviteToGroup(groupName, user);
                }
            });
        }
    });
}

    public void updateUserList(String[] users) {
        Platform.runLater(() -> {
            userListView.getItems().clear();
            userListView.getItems().addAll(users);

            userListView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    String selectedUser = userListView.getSelectionModel().getSelectedItem();
                    if (selectedUser != null) {
                        
                        isGroupChat = false;
                        client.sendChatRequest(selectedUser);
                    }
                }
            });
        });
    }

    public void startChat(String chatPartner) {
        Platform.runLater(() -> {
            isGroupChat = false;
            currentChatPartner = chatPartner;
            currentGroupChat = null;
            chatArea.clear();
            chatArea.appendText("Chat started with " + chatPartner + "\n");
            sendButton.setDisable(false);
        });
    }

    public void showChatRequest(String requester) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Chat Request");
            alert.setHeaderText(requester + " wants to chat with you");
            alert.setContentText("Do you want to accept?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    client.sendChatResponse(requester, true);
                    startChat(requester);
                } else {
                    client.sendChatResponse(requester, false);
                }
            });
        });
    }

    public void showChatRejected(String rejectedUser) {
        Platform.runLater(() -> {
            chatArea.appendText(rejectedUser + " rejected your chat request.\n");
        });
    }

    public void receiveMessage(String sender, String message, boolean isGroupMessage) {
        Platform.runLater(() -> {
            if (isGroupMessage || isGroupChat) {
                chatArea.appendText(sender + ": " + message + "\n");
            } else if (sender.equals(currentChatPartner)) {
                chatArea.appendText(sender + ": " + message + "\n");
            }
        });
    }

    @FXML
    private void onSendButtonClicked() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            if (isGroupChat && currentGroupChat != null) {
                
                client.sendGroupMessage(currentGroupChat, message);
                chatArea.appendText("You: " + message + "\n");
            } else if (currentChatPartner != null) {
                
                client.sendMessage(currentChatPartner, message);
                chatArea.appendText("You: " + message + "\n");
            }
            messageField.clear();
        }
    }

    public void showGroupInvite(String groupName, String inviter) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Group Invite");
            alert.setHeaderText(inviter + " invited you to group: " + groupName);
            alert.setContentText("Do you want to join?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    client.acceptGroupInvite(groupName);
                    enterGroupChat(groupName);
                }
            });
        });
    }

    public void groupCreated(String groupName) {
        Platform.runLater(() -> {
            groups.put(groupName, new HashSet<>());
            enterGroupChat(groupName);
        });
    }

    private void enterGroupChat(String groupName) {
        Platform.runLater(() -> {
            isGroupChat = true;
            currentGroupChat = groupName;
            currentChatPartner = null;
            chatArea.clear();
            chatArea.appendText("--- Entered Group Chat: " + groupName + " ---\n");
            userListView.getSelectionModel().clearSelection();
        });
    }

    public void receiveGroupMessage(String groupName, String sender, String message) {
        Platform.runLater(() -> {
            if (currentGroupChat != null && currentGroupChat.equals(groupName)) {
                chatArea.appendText(sender + ": " + message + "\n");
            }
        });
    }

    public void userJoinedGroup(String groupName, String username) {
        Platform.runLater(() -> {
            if (currentGroupChat != null && currentGroupChat.equals(groupName)) {
                chatArea.appendText("--- " + username + " joined the group ---\n");
            }
        });
    }

    public void userLeftGroup(String groupName, String username) {
        Platform.runLater(() -> {
            if (currentGroupChat != null && currentGroupChat.equals(groupName)) {
                chatArea.appendText("--- " + username + " left the group ---\n");
            }
        });
    }
    public void addUserToGroup() {
    if (currentGroupChat == null) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("No Active Group");
        alert.setHeaderText("Please enter a group chat first");
        alert.showAndWait();
        return;
    }

    Dialog<Set<String>> dialog = new Dialog<>();
    dialog.setTitle("Add Users to Group");
    dialog.setHeaderText("Select users to add to " + currentGroupChat);

    ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

    ListView<String> userSelectionList = new ListView<>();
    userSelectionList.getItems().addAll(
        userListView.getItems().stream()
            .filter(user -> !user.equals(client.getUsername()) && 
                    !groups.get(currentGroupChat).contains(user))
            .collect(Collectors.toList())
    );
    userSelectionList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    dialog.getDialogPane().setContent(userSelectionList);

    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == addButtonType) {
            return userSelectionList.getSelectionModel().getSelectedItems().stream().collect(Collectors.toSet());
        }
        return null;
    });

    dialog.showAndWait().ifPresent(selectedUsers -> {
        if (!selectedUsers.isEmpty()) {
            for (String user : selectedUsers) {
                client.inviteToGroup(currentGroupChat, user);
            }
        }
    });
}
}
