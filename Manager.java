package chatapp_combined;

import chatapp_combined.messagesCommand.Message;
import chatapp_combined.messagesCommand.ReceiveMessage;
import chatapp_combined.messagesCommand.SendMessage;
import chatapp_combined.streamsCommand.CloseStream;
import chatapp_combined.streamsCommand.Stream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// The Manager class handles the setup and management of the chat application,
// including establishing connections and managing message and stream operations.
public class Manager {

    // Constants
    private static final int PORT = 4444;

    private static final String USER_1 = "USER_1";
    private static final String USER_2 = "USER_2";


    // Static fields
    private static ServerSocket serverSocket = null;
    private static Socket clientSocket = null;
    private static DataInputStream in = null;
    private static DataOutputStream out = null;


    // Main business logic for managing the connection
    public void manage() throws IOException, InterruptedException {
        // If we want to choose names for us
        // String username;

        try {
            serverSocket = new ServerSocket(PORT);
            clientSocket = serverSocket.accept();
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());

            System.out.println(USER_1 + " has joined the chat!");

            execute(USER_1);
        } catch (IOException | InterruptedException e) {
            clientSocket = new Socket("localhost", PORT);
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());

            System.out.println(USER_2 + " has joined the chat!");

            execute(USER_2);
        } finally {
            // Close all streams and sockets
            Stream stream = new Stream(serverSocket, clientSocket, in, out);
            CloseStream cs = new CloseStream(stream);
            cs.execute();
        }
    }

    private static void execute(String username) throws InterruptedException, IOException {
        Message message = new Message(in, out, username);

        // Send a message using a separate thread
        SendMessage sendMessage = new SendMessage(message);
        sendMessage.execute();

        // Receive a message using a separate thread
        ReceiveMessage receiveMessage = new ReceiveMessage(message);
        receiveMessage.execute();

        Thread.currentThread().join();
    }

}
