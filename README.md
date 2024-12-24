# JavaFX Chat Application with Group Chat Functionality

This project implements a real-time chat application using JavaFX for the client-side GUI and a custom server built with Java sockets.  It supports both one-on-one and group chat functionalities, allowing users to connect, exchange messages, create groups, and invite other users to join.

![Chat Application Screenshot](screenshot.png)  *(Replace screenshot.png with an actual screenshot of your application)*

## Features

* **Real-time Chat:**  Users can exchange messages instantly with other connected users.
* **One-on-One Chat:** Private conversations between two users.
* **Group Chat:** Create and join groups to chat with multiple users simultaneously.
* **User List:** Displays a list of currently connected users.
* **Chat Requests:** Users can send chat requests and respond to incoming requests.
* **Group Invites:**  Invite users to join created groups.
* **User-Friendly Interface:**  Intuitive and easy-to-use GUI built with JavaFX.

## Implementation Details

### Client-Side (JavaFX)

* **FXML for UI Design:**  Uses FXML to define the user interface layout and structure, separating the UI design from the application logic.
* **Controllers:**  Leverages JavaFX controllers to handle user interactions and update the UI dynamically.
* **Multithreading:**  Employs multithreading to handle network communication and UI updates concurrently, preventing the UI from freezing.
* **Client-Server Communication:** Uses Java's `Socket` and `ServerSocket` classes to establish a connection with the server and exchange messages.

### Server-Side (Java Sockets)

* **Multithreaded Server:** Handles multiple client connections concurrently using threads, ensuring responsiveness.
* **Client Management:** Maintains a list of connected clients and their associated information.
* **Group Management:** Stores and manages group information, including members and messages.
* **Message Broadcasting:**  Distributes messages to the appropriate recipients (individual users or group members).
* **Protocol:** Uses a simple text-based protocol for communication between the client and server.  Messages are prefixed with keywords like `CHAT_REQUEST`, `SEND_MESSAGE`, `GROUP_INVITE`, etc.

## Technologies Used

* **Java:** Core programming language.
* **JavaFX:**  Used for building the client-side graphical user interface.
* **Java Sockets:**  Provides the networking capabilities for client-server communication.
* **FXML:**  XML-based markup language for defining the user interface structure.
* **Multithreading:**  Enables concurrent execution of tasks, improving performance and responsiveness.

## Project Structure
chat-application/
├── client/
│ ├── ChatClient.java
│ ├── ChatController.java
│ ├── LoginController.java
│ └── ... (FXML files and other resources)
└── server/
└── ChatServer.java


## Getting Started

1. **Clone the repository:** `git clone https://github.com/your-username/chat-application.git`  *(Replace with your actual repository URL)*
2. **Compile the code:**  Use a Java IDE or command-line tools to compile both the client and server code.
3. **Run the server:**  Execute the `ChatServer.java` file.
4. **Run the client:**  Execute the `ChatClient.java` file.


## Future Enhancements

* **Enhanced UI:** Improve the user interface with more features and customization options.
* **File Sharing:** Implement file transfer capabilities within the chat.
* **Notifications:** Add desktop notifications for new messages.
* **User Authentication:** Integrate a user authentication system for secure login.
* **Encryption:**  Encrypt messages for enhanced privacy.


This README provides a comprehensive overview of the JavaFX chat application project. Feel free to explore the code and contribute to its development!
