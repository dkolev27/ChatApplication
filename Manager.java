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

import static chatapp_combined.Utility.CommonUtils.ANSI_CYAN;
import static chatapp_combined.Utility.CommonUtils.ANSI_RESET;

/**
 * The type Manager class handles the setup and management of the chat application,
 * including establishing connections and managing message and stream operations.
 *
 * @author Dimitar Kolev
 */
public final class Manager {

    private static final int PORT = 4444;

    private static final String USER_1 = "USER_1";
    private static final String USER_2 = "USER_2";


    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static DataInputStream inputStream;
    private static DataOutputStream outputStream;

    /**
     * Private constructor does not allow an instance to be created
     */
    private Manager() {

    }


    /**
     * Manage.manage()
     * Main business logic for managing the connection
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    public static void manage() throws IOException, InterruptedException {
        // If we want to choose names for us
        // String username;

        try {
            serverSocket = new ServerSocket(PORT);
            clientSocket = serverSocket.accept();
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
            inputStream = new DataInputStream(clientSocket.getInputStream());

            System.out.println(ANSI_CYAN + USER_1 + " has joined the chat!" + ANSI_RESET);

            execute(USER_1);
        } catch (IOException | InterruptedException e) {
            clientSocket = new Socket("localhost", PORT);
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
            inputStream = new DataInputStream(clientSocket.getInputStream());

            System.out.println(ANSI_CYAN + USER_2 + " has joined the chat!" + ANSI_RESET);

            execute(USER_2);
        } finally {
            // Close all streams and sockets
            final Stream stream = new Stream(serverSocket, clientSocket, inputStream, outputStream);
            final CloseStream closeStream = new CloseStream(stream);
            closeStream.execute();
        }
    }

    private static void execute(String username) throws InterruptedException, IOException {
        startChatting(username);

        Thread.currentThread().join();
    }

    private static void startChatting(String username) {
        final Message message = new Message(inputStream, outputStream, username);

        // Send a message using a separate thread
        final SendMessage sendMessage = new SendMessage(message);
        sendMessage.execute();

        // Receive a message using a separate thread
        final ReceiveMessage receiveMessage = new ReceiveMessage(message);
        receiveMessage.execute();
    }

}
