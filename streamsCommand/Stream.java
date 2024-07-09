package chatapp_combined.streamsCommand;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * The type Stream.
 * The Class handles the input and output streams for network communication
 *
 * @author Dimitar Kolev
 */
public class Stream {

    private final ServerSocket serverSocket;
    private final Socket clientSocket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    /**
     * Instantiates a new Stream.
     *
     * @param serverSocket the server socket
     * @param clientSocket the client socket
     * @param inputStream  the in
     * @param outputStream the out
     */
    public Stream(final ServerSocket serverSocket, final Socket clientSocket, final DataInputStream inputStream, final DataOutputStream outputStream) {
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    /**
     * Close all streams and sockets.
     */
    public void closeEverything() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }

            if (outputStream != null) {
                outputStream.close();
            }

            if (clientSocket != null) {
                clientSocket.close();
            }

            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Logger.getLogger("Failed closure.");
        }
    }

}
