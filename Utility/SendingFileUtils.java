package chatapp_combined.Utility;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static chatapp_combined.Utility.CommonUtils.*;

/**
 * Utility class for sending files in a chat application.
 *
 * @author Dimitar Kolev
 */
public final class SendingFileUtils {

    /**
     * The constant CHUNK_SIZE.
     */
    public static final int CHUNK_SIZE = 1024;
    private static final int START_IDX = 0;
    private static final String CHECKSUM_ALGORITHM = "MD5";
    private static final String FILES_DIR = "filesToSend";


    /**
     * Private constructor does not allow an instance to be created
     */
    private SendingFileUtils() {

    }


    /**
     * Sends a file over a DataOutputStream.
     *
     * @param message      The message containing the file path.
     * @param outputStream The DataOutputStream for sending data.
     * @throws IOException If an I/O error occurs.
     */
    public static void sendFileBytes(final String message, final DataOutputStream outputStream) throws IOException {
        final int endCommandIdx = 2;
        // Separate the command from the full inputted string
        final String command = new String(Arrays.copyOfRange(message.getBytes(), START_IDX, endCommandIdx));
        // Separate the filepath from the full inputted string
        final String path = new String(Arrays.copyOfRange(message.getBytes(), endCommandIdx + 1, message.length()));

        // Get the length of the command in byte[]
        final int totalLengthCommand = command.length();
        final byte[] totalLengthCommandAsByteArray = convertIntToByteArray(totalLengthCommand);

        // Get the command in byte[]
        final byte[] commandBytes = command.getBytes();

        final File file = new File(path);

        // Checks if the file exists
        if (doesFileNotExist(file.getName())) {
            return;
        }

        // Get the file length in byte[]
        final long fileLength = file.length();
        final byte[] fileLengthAsByteArray = convertLongToByteArray(fileLength);

        final String fileName = file.getName();

        // Get the fileName length in byte[]
        final int totalLengthFileName = fileName.length();
        final byte[] totalLengthFileNameAsByteArray = convertIntToByteArray(totalLengthFileName);

        // Get the fileName in byte[]
        final byte[] fileNameBytes = fileName.getBytes();

        // Construct a byte[] consists of: the bytes of command length, the command bytes, the length of the file,
        // the length of the filename, and the filename itself
        final byte[] allBytesArray = fillAllBytesArray(totalLengthCommandAsByteArray, commandBytes, fileLengthAsByteArray,
                totalLengthFileNameAsByteArray, fileNameBytes);

        // At first, send the bytes that represent the command length, the command, the length of the file as byte[],
        // the length of the fileName, and the fileName in byte[]
        sendAllBytesArray(outputStream, allBytesArray);

        // Now, send the file in chunks
        sendFileInChunks(outputStream, file, fileLength);
    }

    private static boolean doesFileNotExist(final String path) {
        final File directory = new File(FILES_DIR);
        final File fileToCheck = new File(directory, path);

        if (!fileToCheck.exists()) {
            System.out.println(ANSI_RED + "File does not exist!" + ANSI_RESET);
            return true;
        }
        return false;
    }

    /**
     * Sends the file in chunks over a DataOutputStream.
     *
     * @param outputStream The DataOutputStream for sending data.
     * @param file         The file to be sent.
     * @param fileLength   The length of the file.
     */
    private static void sendFileInChunks(final DataOutputStream outputStream, final File file, final long fileLength)
            throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            System.out.println(ANSI_YELLOW + getTimeString() + " Sending file..." + ANSI_RESET);

            final int chunkSize = CHUNK_SIZE; // Size of each chunk

            final byte[] buffer = new byte[chunkSize];

            MessageDigest md = MessageDigest.getInstance(CHECKSUM_ALGORITHM);

            int bytesRead;
            long totalBytesLeft = fileLength;
            final long end = (fileLength / chunkSize) + 1;
            for (int i = 0; i < end; i++) {
                bytesRead = fileInputStream.read(buffer, START_IDX, (int) Math.min(totalBytesLeft, chunkSize));
                outputStream.write(buffer, START_IDX, bytesRead);
                md.update(buffer, START_IDX, bytesRead); // Update the file hash every time we write a new chunk to the stream
                totalBytesLeft -= chunkSize;
            }

            // Completes the hash computation and returns the final value of the hash as byte[]
            final byte[] fileHash = md.digest();

            // Send the hash of the file as byte[]
            outputStream.write(fileHash);
            outputStream.flush();

            System.out.println(ANSI_YELLOW + getTimeString() + " File sent!" + ANSI_RESET);
        } catch (IOException | NoSuchAlgorithmException ex) {
            System.out.println(ANSI_RED + "Connection lost! The file may be corrupted." + ANSI_RESET);
        }
    }

    /**
     * Sends a byte array over a DataOutputStream.
     *
     * @param outputStream  The DataOutputStream for sending data.
     * @param allBytesArray The byte array to be sent.
     */
    private static void sendAllBytesArray(final DataOutputStream outputStream, final byte[] allBytesArray) {
        try {
            outputStream.write(allBytesArray);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fills a byte array with various components of the file to be sent.
     *
     * @param totalLengthCommandAsByteArray  The length of the command.
     * @param commandBytes                   The command bytes.
     * @param fileLengthAsByteArray          The length of the file.
     * @param totalLengthFileNameAsByteArray The length of the file name.
     * @param fileNameBytes                  The file name bytes.
     * @return The filled byte array.
     */
    private static byte[] fillAllBytesArray(final byte[] totalLengthCommandAsByteArray, final byte[] commandBytes,
                                            final byte[] fileLengthAsByteArray, final byte[] totalLengthFileNameAsByteArray,
                                            final byte[] fileNameBytes) {
        final int allLengths = totalLengthCommandAsByteArray.length + commandBytes.length +
                fileLengthAsByteArray.length + totalLengthFileNameAsByteArray.length + fileNameBytes.length;
        final byte[] allBytesArray = new byte[allLengths];

        int idx = START_IDX;

        for (byte b : totalLengthCommandAsByteArray) {
            allBytesArray[idx++] = b;
        }

        for (byte b : commandBytes) {
            allBytesArray[idx++] = b;
        }

        for (byte b : fileLengthAsByteArray) {
            allBytesArray[idx++] = b;
        }

        for (byte b : totalLengthFileNameAsByteArray) {
            allBytesArray[idx++] = b;
        }

        for (byte b : fileNameBytes) {
            allBytesArray[idx++] = b;
        }

        return allBytesArray;
    }

}
