package chatapp_combined.Utility;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static chatapp_combined.Utility.CommonUtils.convertIntToByteArray;

/**
 * The type Sending message utils.
 *
 * @author Dimitar Kolev
 */
public final class SendingMessageUtils {

    private static final int START_IDX = 0;


    /**
     * Private constructor does not allow an instance to be created
     */
    private SendingMessageUtils() {

    }


    /**
     * Sends a message as bytes through a DataOutputStream.
     *
     * @param messageToSend The message to send.
     * @param outputStream  The DataOutputStream to send the message through.
     * @throws IOException If an I/O error occurs.
     */
    public static void sendMessageBytes(final String messageToSend, final DataOutputStream outputStream) throws IOException {
        final byte[] toSend = bytesToSend(messageToSend);
        outputStream.write(toSend);
        outputStream.flush();
    }

    /**
     * Prepares a message to be sent by converting it to a byte array.
     *
     * @param message The message to convert.
     * @return A byte array representing the command and the message.
     */
    private static byte[] bytesToSend(final String message) {
        // Define the index where the command part of the message ends
        final int endCommandIdx = 2;

        // Extract the command part from the message (e.g., "-m" or "-f")
        final String command = new String(Arrays.copyOfRange(message.getBytes(), START_IDX, endCommandIdx));

        // Convert the length of the command to a byte array
        final int totalLengthCommand = command.length();
        final byte[] totalLengthCommandAsByteArray = convertIntToByteArray(totalLengthCommand);

        // Convert the command itself to a byte array
        final byte[] commandBytes = command.getBytes();

        // Calculate the length of the actual message (excluding the command and a separating space)
        final int totalLengthMessage = message.length() - totalLengthCommand - 1;
        final byte[] totalLengthMessageAsByteArray = convertIntToByteArray(totalLengthMessage);

        // Extract the actual message part by trimming off the command and the separating space
        final int startIdx = totalLengthCommand + 1;
        final int endIdx = totalLengthMessage + totalLengthCommand + 1;
        final String trimmedMessage = new String(Arrays.copyOfRange(message.getBytes(), startIdx, endIdx));
        final byte[] messageBytes = trimmedMessage.getBytes();

        // Calculate the total length of all byte arrays combined
        final int allBytes = totalLengthCommandAsByteArray.length + commandBytes.length +
                totalLengthMessageAsByteArray.length + messageBytes.length;

        // Merge all byte arrays into a single byte array and return it
        return fillAllBytesArray(totalLengthCommandAsByteArray, commandBytes, totalLengthMessageAsByteArray,
                messageBytes, allBytes);
    }

    /**
     * Merges multiple byte arrays into a single byte array.
     *
     * @param totalLengthCommandAsByteArray byte array representing the length of the command
     * @param commandBytes                  byte array of the command itself
     * @param totalLengthMessageAsByteArray byte array representing the length of the message
     * @param messageBytes                  byte array of the message itself
     * @param allBytes                      total length of the final merged byte array
     * @return a single byte array containing all provided byte arrays in sequence
     */
    private static byte[] fillAllBytesArray(final byte[] totalLengthCommandAsByteArray, final byte[] commandBytes,
                                            final byte[] totalLengthMessageAsByteArray, final byte[] messageBytes,
                                            final int allBytes) {
        // Create a new byte array to hold all the parts
        final byte[] allBytesArray = new byte[allBytes];

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
