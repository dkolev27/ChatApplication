package chatapp_combined.Utility;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static chatapp_combined.Utility.CommonUtils.getLength;
import static chatapp_combined.Utility.CommonUtils.getTimeString;


public class ReceivingMessageUtils {

    // Constant
    private static final int START_IDX = 0;


    /**
     * Receives a message from the other user and prints it to the console.
     *
     * @throws IOException If an I/O error occurs.
     */
    public static void receiveMessage(String senderName, DataInputStream in) throws IOException {
        // Read the total length of the message
        int messageLength = getLength(in);

        // Read the message bytes and construct a String representing the message
        String message = getMessage(messageLength, in);

        // Print the received message along with the sender's name and timestamp
        System.out.println(getTimeString() + senderName + ": " + message);
    }

    /**
     * Reads and retrieves the command sent by the other user.
     *
     * @param commandLength The length of the command to be read.
     * @return The command received from the other user.
     * @throws IOException If an I/O error occurs.
     */
    public static String getCommand(int commandLength, DataInputStream in) throws IOException {
        // Read the bytes representing the command and construct a String
        byte[] commandBytes = new byte[commandLength];
        in.readFully(commandBytes, START_IDX, commandLength);

        return new String(commandBytes, StandardCharsets.UTF_8);
    }

    /**
     * Reads and retrieves the message sent by the other user.
     *
     * @param messageLength The length of the message to be read.
     * @return The message received from the other user.
     * @throws IOException If an I/O error occurs.
     */
    private static String getMessage(int messageLength, DataInputStream in) throws IOException {
        // Read the bytes representing the message and construct a String
        byte[] messageBytes = new byte[messageLength];
        in.readFully(messageBytes, START_IDX, messageLength);

        return new String(messageBytes, StandardCharsets.UTF_8);
    }

}
