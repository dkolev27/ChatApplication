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
    static private ServerSocket serverSocket = null;
    static private Socket clientSocket = null;
    static private DataInputStream in = null;
    static private DataOutputStream out = null;

    private static final int PORT = 4444;

    public void manage() throws IOException, InterruptedException {
        String username;
        try {
            serverSocket = new ServerSocket(PORT);
            clientSocket = serverSocket.accept();
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());

            username = "Ivan";
            System.out.println(username + " has joined the chat!");

            execute(username);

        } catch (IOException | InterruptedException e) {
            clientSocket = new Socket("localhost", PORT);
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());

            username = "Mitko";
            System.out.println(username + " has joined the chat!");

            execute(username);

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
