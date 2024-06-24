package chatapp_combined.Utility;

import java.io.DataInputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CommonUtils {

    // Constants
    private static final int START_IDX = 0;
    private static final int BYTES_FOR_ONE_INTEGER = 4;

    private static final String DATE_FORMAT = "HH:mm:ss";

    /**
     * Generates a string representation of the current time in the specified format.
     *
     * @return A string representing the current time.
     */
    public static String getTimeString() {
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        String formattedTime = currentTime.format(formatter);

        return "[" + formattedTime + "] ";
    }

    /**
     * Reads and retrieves the length of a message or command.
     *
     * @return The length of the message or command.
     * @throws IOException If an I/O error occurs.
     */
    public static int getLength(DataInputStream in) throws IOException {
        // Read the bytes representing the length of the message or command
        byte[] totalLengthCommandByteArray = new byte[BYTES_FOR_ONE_INTEGER]; // first 4 bytes
        in.readFully(totalLengthCommandByteArray, START_IDX, BYTES_FOR_ONE_INTEGER);
        // Convert the byte array to an integer
        return SendingMessageUtils.convertByteArrayToInt(totalLengthCommandByteArray);
    }

}
