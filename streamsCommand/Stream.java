package chatapp_combined.streamsCommand;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// The Stream class handles the input and output streams for network communication
public class Stream {

    // Fields
    private final ServerSocket serverSocket;  // The server socket
    private final Socket clientSocket;        // The client socket
    private final DataInputStream in;         // Input stream for reading data
    private final DataOutputStream out;       // Output stream for sending data

    // Constructor
    public Stream(ServerSocket serverSocket, Socket clientSocket, DataInputStream in, DataOutputStream out) {
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.in = in;
        this.out = out;
    }

    // Method to close all streams and sockets
    public void closeEverything() {
        try {
            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }

            if (clientSocket != null) {
                clientSocket.close();
            }

            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
