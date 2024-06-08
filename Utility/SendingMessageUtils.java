package chatapp_combined.Utility;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SendingMessageUtils {

    // Constants
    private static final int BYTES_FOR_INTEGER = 4; // Number of bytes to represent an integer
    private static final int START_IDX = 0; // Starting index for array operations

    // Utility methods for sending messages
    /**
     * Converts an integer to a byte array.
     *
     * @param i The integer to convert.
     * @return A byte array representing the integer.
     */
    public static byte[] convertIntToByteArray(final int i) {
        ByteBuffer buffer = ByteBuffer.allocate(BYTES_FOR_INTEGER);
        buffer.putInt(i);
        return buffer.array();
    }

    /**
     * Converts a byte array to an integer.
     *
     * @param byteArray The byte array to convert.
     * @return An integer represented by the byte array.
     */
    public static int convertByteArrayToInt(final byte[] byteArray) {
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        return buffer.getInt();
    }

    /**
     * Sends a message as bytes through a DataOutputStream.
     *
     * @param messageToSend The message to send.
     * @param out The DataOutputStream to send the message through.
     * @throws IOException If an I/O error occurs.
     */
    public static void sendMessageBytes(String messageToSend, DataOutputStream out) throws IOException {
        byte[] toSend = bytesToSend(messageToSend);
        out.write(toSend);
        out.flush();
    }

    /**
     * Prepares a message to be sent by converting it to a byte array.
     *
     * @param message The message to convert.
     * @return A byte array representing the command and the message.
     */
    public static byte[] bytesToSend(String message) {
        // Define the index where the command part of the message ends
        final int END_COMMAND_IDX = 2;

        // Extract the command part from the message (e.g., "-m" or "-f")
        String command = new String(Arrays.copyOfRange(message.getBytes(), START_IDX, END_COMMAND_IDX));

        // Convert the length of the command to a byte array
        int totalLengthCommand = command.length();
        byte[] totalLengthCommandAsByteArray = convertIntToByteArray(totalLengthCommand);

        // Convert the command itself to a byte array
        byte[] commandBytes = command.getBytes();

        // Calculate the length of the actual message (excluding the command and a separating space)
        int totalLengthMessage = message.length() - totalLengthCommand - 1;
        byte[] totalLengthMessageAsByteArray = convertIntToByteArray(totalLengthMessage);

        // Extract the actual message part by trimming off the command and the separating space
        final int FROM = totalLengthCommand + 1;
        final int TO = totalLengthMessage + totalLengthCommand + 1;
        String trimmedMessage = new String(Arrays.copyOfRange(message.getBytes(), FROM, TO));
        byte[] messageBytes = trimmedMessage.getBytes();

        // Calculate the total length of all byte arrays combined
        int allBytes = totalLengthCommandAsByteArray.length + commandBytes.length +
                totalLengthMessageAsByteArray.length + messageBytes.length;

        // Merge all byte arrays into a single byte array and return it
        return fillAllBytesArray(totalLengthCommandAsByteArray, commandBytes, totalLengthMessageAsByteArray,
                messageBytes, allBytes);
    }

    /**
     * Merges multiple byte arrays into a single byte array.
     *
     * @param totalLengthCommandAsByteArray byte array representing the length of the command
     * @param commandBytes byte array of the command itself
     * @param totalLengthMessageAsByteArray byte array representing the length of the message
     * @param messageBytes byte array of the message itself
     * @param allBytes total length of the final merged byte array
     * @return a single byte array containing all provided byte arrays in sequence
     */
    private static byte[] fillAllBytesArray(byte[] totalLengthCommandAsByteArray, byte[] commandBytes,
                                            byte[] totalLengthMessageAsByteArray, byte[] messageBytes,
                                            int allBytes) {
        // Create a new byte array to hold all the parts
        byte[] allBytesArray = new byte[allBytes];

        // Index to keep track of the current position in the final byte array
        int idx = START_IDX;

        // Copy each part into the final byte array sequentially
        for (byte b : totalLengthCommandAsByteArray) {
            allBytesArray[idx++] = b;
        }

        for (byte b : commandBytes) {
            allBytesArray[idx++] = b;
        }

        for (byte b : totalLengthMessageAsByteArray) {
            allBytesArray[idx++] = b;
        }

        for (byte b : messageBytes) {
            allBytesArray[idx++] = b;
        }

        // Return the fully assembled byte array
        return allBytesArray;
    }

}
