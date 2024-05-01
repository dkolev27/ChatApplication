package chatapp_combined.streamsCommand;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Stream {
    private final ServerSocket serverSocket;
    private final Socket clientSocket;
    private final DataInputStream in;
    private final DataOutputStream out;

    public Stream(ServerSocket serverSocket, Socket clientSocket, DataInputStream in, DataOutputStream out) {
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.in = in;
        this.out = out;
    }

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

            System.out.println("closed"); // FOR TESTING
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
