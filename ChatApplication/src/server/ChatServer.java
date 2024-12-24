package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 8888;
    private static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static Map<String, Set<ClientHandler>> groups = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Chat Server is running on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader input;
        private PrintWriter output;
        private String username;
        private String ipAddress;
        private Set<String> joinedGroups = new HashSet<>();

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.ipAddress = socket.getInetAddress().getHostAddress();
        }

        @Override
        public void run() {
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            String initialMessage = input.readLine();
            if (initialMessage.startsWith("CHECK_USERNAME:")) {
                String checkUsername = initialMessage.split(":")[1];
                if (clients.containsKey(checkUsername)) {
                    output.println("USERNAME_TAKEN");
                    socket.close();
                    return;
                }
                output.println("USERNAME_AVAILABLE");
                return;
            }

            
                username = initialMessage;
                
                clients.put(username, this);
                broadcastUserList();

                String message;
                while ((message = input.readLine()) != null) {
                    if (message.startsWith("CHAT_REQUEST:")) {
                        handleChatRequest(message);
                    } else if (message.startsWith("CHAT_RESPONSE:")) {
                        handleChatResponse(message);
                    } else if (message.startsWith("SEND_MESSAGE:")) {
                        forwardMessage(message);
                    } else if (message.startsWith("SEND_GROUP_MESSAGE:")) {
                        forwardGroupMessage(message);
                    } else if (message.startsWith("CREATE_GROUP:")) {
                        createGroup(message);
                    } else if (message.startsWith("INVITE_TO_GROUP:")) {
                        inviteToGroup(message);
                    } else if (message.startsWith("ACCEPT_GROUP_INVITE:")) {
                        acceptGroupInvite(message);
                    } else if (message.startsWith("LEAVE_GROUP:")) {
                        leaveGroup(message);
                    } else if (message.equals("DISCONNECT")) {
                        break;
                    }
                    else if (message.startsWith("ADD_TO_GROUP:")) {
                      addToGroup(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally { 
                clients.remove(username);
                broadcastUserList();
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void createGroup(String message) {
            String[] parts = message.split(":");
            String groupName = parts[1];

            if (groups.containsKey(groupName)) {
                output.println("GROUP_CREATE_FAILED:Group already exists");
                return;
            }

            Set<ClientHandler> groupMembers = new HashSet<>();
            groupMembers.add(this);
            groups.put(groupName, groupMembers);
            joinedGroups.add(groupName);

            output.println("GROUP_CREATED:" + groupName);
        }

        private void inviteToGroup(String message) {
            String[] parts = message.split(":");
            String groupName = parts[1];
            String invitedUser = parts[2];

            ClientHandler invitedClient = clients.get(invitedUser);
            if (invitedClient != null) {
                invitedClient.output.println("GROUP_INVITE:" + groupName + ":" + username);
            }
        }

        private void acceptGroupInvite(String message) {
            String[] parts = message.split(":");
            String groupName = parts[1];

            if (groups.containsKey(groupName)) {
                Set<ClientHandler> groupMembers = groups.get(groupName);
                groupMembers.add(this);
                joinedGroups.add(groupName);

                for (ClientHandler member : groupMembers) {
                    member.output.println("USER_JOINED_GROUP:" + groupName + ":" + username);
                }
            }
        }

        private void leaveGroup(String message) {
            String[] parts = message.split(":");
            String groupName = parts[1];

            if (groups.containsKey(groupName)) {
                Set<ClientHandler> groupMembers = groups.get(groupName);
                groupMembers.remove(this);
                joinedGroups.remove(groupName);

                for (ClientHandler member : groupMembers) {
                    member.output.println("USER_LEFT_GROUP:" + groupName + ":" + username);
                }

                if (groupMembers.isEmpty()) {
                    groups.remove(groupName);
                }
            }
        }

        private void forwardGroupMessage(String message) {
            String[] parts = message.split(":", 3);
            String groupName = parts[1];
            String chatMessage = parts[2];

            Set<ClientHandler> groupMembers = groups.get(groupName);
            if (groupMembers != null) {
                for (ClientHandler member : groupMembers) {
                    if (!member.username.equals(username)) {
                        member.output.println("GROUP_MESSAGE:" + groupName + ":" + username + ":" + chatMessage);
                    }
                }
            }
        }

        private void handleChatRequest(String message) {
            String targetUser = message.split(":")[1];
            ClientHandler targetClient = clients.get(targetUser);

            if (targetClient != null) {
                targetClient.output.println("INCOMING_CHAT_REQUEST:" + username);
            }
        }

        private void handleChatResponse(String message) {
            String[] parts = message.split(":");
            String requesterUsername = parts[1];
            String response = parts[2];

            ClientHandler requesterClient = clients.get(requesterUsername);
            if (requesterClient != null) {
                requesterClient.output.println("CHAT_REQUEST_" + response + ":" + username);
            }
        }

        private void forwardMessage(String message) {
            String[] parts = message.split(":", 3);
            String targetUser = parts[1];
            String chatMessage = parts[2];

            ClientHandler targetClient = clients.get(targetUser);
            if (targetClient != null) {
                targetClient.output.println("MESSAGE:" + username + ":" + chatMessage);
            }
        }

        private void broadcastGroupMessage(String message) {
            for (ClientHandler client : clients.values()) {
                if (!client.username.equals(username)) {
                    client.output.println("MESSAGE:" + username + ":" + message + ":GROUP");
                }
            }
        }

        private void broadcastUserList() {
            String userList = String.join(",", clients.keySet());
            for (ClientHandler client : clients.values()) {
                client.output.println("USER_LIST:" + userList);
            }
        }
        private void addToGroup(String message) {
    String[] parts = message.split(":");
    String groupName = parts[1];
    String invitedUser = parts[2];

    if (groups.containsKey(groupName)) {
        ClientHandler invitedClient = clients.get(invitedUser);
        if (invitedClient != null) {
            invitedClient.output.println("GROUP_INVITE:" + groupName + ":" + username);
        }
    }
}
    }
}
