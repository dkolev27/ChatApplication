package chatapp_combined.Utility;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


/**
 * The type Common utils.
 *
 * @author Dimitar Kolev
 */
public final class CommonUtils {

    /**
     * The constants for colors.
     */
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";


    private static final int START_IDX = 0; // Array starting index
    private static final int BYTES_FOR_INTEGER = 4; // Number of bytes to represent an integer
    private static final int BYTES_FOR_LONG = 8; // Number of bytes to represent a long
    private static final String DATE_FORMAT = "HH:mm:ss";


    /**
     * Private constructor does not allow to create an instance
     */
    private CommonUtils() {

    }


    /**
     * Converts an integer to a byte array.
     *
     * @param value The integer to convert.
     * @return A byte array representing the integer.
     */
    public static byte[] convertIntToByteArray(final int value) {
        final ByteBuffer buffer = ByteBuffer.allocate(BYTES_FOR_INTEGER);
        buffer.putInt(value);

        return buffer.array();
    }

    /**
     * Converts a byte array to an integer.
     *
     * @param byteArray The byte array to convert.
     * @return An integer represented by the byte array.
     */
    public static int convertByteArrayToInt(final byte[] byteArray) {
        final ByteBuffer buffer = ByteBuffer.wrap(byteArray);

        return buffer.getInt();
    }

    /**
     * Converts a long value to a byte array.
     *
     * @param value The long value to be converted.
     * @return The byte array representation of the long value.
     */
    public static byte[] convertLongToByteArray(final long value) {
        final ByteBuffer buffer = ByteBuffer.allocate(BYTES_FOR_LONG);
        buffer.putLong(value);

        return buffer.array();
    }

    /**
     * Converts a byte array to a long value.
     *
     * @param byteArray The byte array to be converted.
     * @return The long value represented by the byte array.
     */
    public static long convertByteArrayToLong(final byte[] byteArray) {
        final ByteBuffer buffer = ByteBuffer.wrap(byteArray);

        return buffer.getLong();
    }

    /**
     * Generates a string representation of the current time in the specified format.
     *
     * @return A string representing the current time.
     */
    public static String getTimeString() {
        final LocalTime currentTime = LocalTime.now();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        final String formattedTime = currentTime.format(formatter);

        return "[" + formattedTime + "] ";
    }

    /**
     * Reads and retrieves the length of a message or command.
     *
     * @param inputStream the inputStream
     * @return The length of the message or command.
     * @throws IOException If an I/O error occurs.
     */
    public static int getLength(final DataInputStream inputStream) throws IOException {
        // Read the bytes representing the length of the message or command
        final byte[] commandLength = new byte[BYTES_FOR_INTEGER]; // first 4 bytes
        inputStream.readFully(commandLength, START_IDX, BYTES_FOR_INTEGER);

        // Convert the byte array to an integer
        return convertByteArrayToInt(commandLength);
    }

}
