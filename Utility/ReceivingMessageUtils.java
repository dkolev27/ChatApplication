package chatapp_combined.Utility;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static chatapp_combined.Utility.CommonUtils.getLength;
import static chatapp_combined.Utility.CommonUtils.getTimeString;


/**
 * The type Receiving message utils.
 *
 * @author Dimitar Kolev
 */
public final class ReceivingMessageUtils {

    private static final int START_IDX = 0;


    /**
     * Private constructor does not allow an instance to be created
     */
    private ReceivingMessageUtils() {

    }


    /**
     * Receives a message from the other user and prints it to the console.
     *
     * @param senderName  the sender name
     * @param inputStream the input stream
     * @throws IOException If an I/O error occurs.
     */
    public static void receiveMessage(final String senderName, final DataInputStream inputStream) throws IOException {
        // Read the total length of the message
        final int messageLength = getLength(inputStream);

        // Read the message bytes and construct a String representing the message
        final String message = getMessage(messageLength, inputStream);

        // Print the received message along with the sender's name and timestamp
        System.out.println(getTimeString() + senderName + ": " + message);
    }

    /**
     * Reads and retrieves the command sent by the other user.
     *
     * @param commandLength The length of the command to be read.
     * @param inputStream   the input stream
     * @return The command received from the other user.
     * @throws IOException If an I/O error occurs.
     */
    public static String getCommand(final int commandLength, final DataInputStream inputStream) throws IOException {
        // Read the bytes representing the command and construct a String
        final byte[] commandBytes = new byte[commandLength];
        inputStream.readFully(commandBytes, START_IDX, commandLength);

        return new String(commandBytes, StandardCharsets.UTF_8);
    }

    /**
     * Reads and retrieves the message sent by the other user.
     *
     * @param messageLength The length of the message to be read.
     * @return The message received from the other user.
     * @throws IOException If an I/O error occurs.
     */
    private static String getMessage(final int messageLength, final DataInputStream inputStream) throws IOException {
        // Read the bytes representing the message and construct a String
        final byte[] messageBytes = new byte[messageLength];
        inputStream.readFully(messageBytes, START_IDX, messageLength);

        return new String(messageBytes, StandardCharsets.UTF_8);
    }

}
