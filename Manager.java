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

public class Manager {

    // Constant
    private static final int PORT = 4444;

    private static final String USER_1 = "Ivan";
    private static final String USER_2 = "Mitko";


    // Static fields
    private static ServerSocket serverSocket = null;
    private static Socket clientSocket = null;
    private static DataInputStream in = null;
    private static DataOutputStream out = null;


    // main business logic
    // how the connection is made
    public void manage() throws IOException, InterruptedException {
        String username;
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
            Stream stream = new Stream(serverSocket, clientSocket, in, out);
            CloseStream cs = new CloseStream(stream);
            cs.execute();
        }
    }

    private static void execute(String username) throws InterruptedException {
        Message message = new Message(serverSocket, clientSocket, in, out, username);

        // send message to the peer2 (separate thread)
        SendMessage sendMessage = new SendMessage(message);
        sendMessage.execute();

        // receive message from the peer2 (separate thread)
        ReceiveMessage receiveMessage = new ReceiveMessage(message);
        receiveMessage.execute();

        Thread.currentThread().join();
    }

}
