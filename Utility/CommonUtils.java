package chatapp_combined.Utility;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class CommonUtils {

    // Constants
    private static final int START_IDX = 0; // Array starting index
    private static final int BYTES_FOR_INTEGER = 4; // Number of bytes to represent an integer
    private static final int BYTES_FOR_LONG = 8; // Number of bytes to represent a long

    private static final String DATE_FORMAT = "HH:mm:ss";


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
     * Converts a long value to a byte array.
     *
     * @param l The long value to be converted.
     * @return The byte array representation of the long value.
     */
    public static byte[] convertLongToByteArray(final long l) {
        ByteBuffer buffer = ByteBuffer.allocate(BYTES_FOR_LONG);
        buffer.putLong(l);

        return buffer.array();
    }

    /**
     * Converts a byte array to a long value.
     *
     * @param byteArray The byte array to be converted.
     * @return The long value represented by the byte array.
     */
    public static long convertByteArrayToLong(final byte[] byteArray) {
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);

        return buffer.getLong();
    }

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
        byte[] totalLengthCommandByteArray = new byte[BYTES_FOR_INTEGER]; // first 4 bytes
        in.readFully(totalLengthCommandByteArray, START_IDX, BYTES_FOR_INTEGER);

        // Convert the byte array to an integer
        return convertByteArrayToInt(totalLengthCommandByteArray);
    }

}
