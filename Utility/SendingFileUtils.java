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
 */
public class SendingFileUtils {

    // Constants
    public static final int CHUNK_SIZE = 1024;
    private static final int START_IDX = 0;
    private static final String CHECKSUM_ALGORITHM = "MD5";
    private static final String DIRECTORY_WITH_FILES = "filesToSend";


    /**
     * Sends a file over a DataOutputStream.
     *
     * @param message The message containing the file path.
     * @param out     The DataOutputStream for sending data.
     * @throws IOException If an I/O error occurs.
     */
    public static void sendFileBytes(String message, DataOutputStream out) throws IOException {
        final int END_COMMAND_IDX = 2;
        // Separate the command from the full inputted string
        String command = new String(Arrays.copyOfRange(message.getBytes(), START_IDX, END_COMMAND_IDX));
        // Separate the filepath from the full inputted string
        String path = new String(Arrays.copyOfRange(message.getBytes(), END_COMMAND_IDX + 1, message.length()));

        // Get the length of the command in byte[]
        int totalLengthCommand = command.length();
        byte[] totalLengthCommandAsByteArray = convertIntToByteArray(totalLengthCommand);

        // Get the command in byte[]
        byte[] commandBytes = command.getBytes();

        File file = new File(path);

        // Checks if the file exists
        if (doesFileNotExist(path)) return;

        // Get the file length in byte[]
        long fileLength = file.length();
        byte[] fileLengthAsByteArray = convertLongToByteArray(fileLength);

        String fileName = file.getName();

        // Get the fileName length in byte[]
        int totalLengthFileName = fileName.length();
        byte[] totalLengthFileNameAsByteArray = convertIntToByteArray(totalLengthFileName);

        // Get the fileName in byte[]
        byte[] fileNameBytes = fileName.getBytes();

        // Construct a byte[] consists of: the bytes of command length, the command bytes, the length of the file,
        // the length of the filename, and the filename itself
        byte[] allBytesArray = fillAllBytesArray(totalLengthCommandAsByteArray, commandBytes, fileLengthAsByteArray,
                totalLengthFileNameAsByteArray, fileNameBytes);

        // At first, send the bytes that represent the command length, the command, the length of the file as byte[],
        // the length of the fileName, and the fileName in byte[]
        sendAllBytesArray(out, allBytesArray);

        // Now, send the file in chunks
        sendFileInChunks(out, file, fileLength);
    }

    private static boolean doesFileNotExist(String path) {
        File directory = new File(DIRECTORY_WITH_FILES);
        File fileToCheck = new File(directory, path);

        if (!fileToCheck.exists()) {
            System.out.println(ANSI_RED + "File does not exist!" + ANSI_RESET);
            return true;
        }
        return false;
    }

    /**
     * Sends the file in chunks over a DataOutputStream.
     *
     * @param out        The DataOutputStream for sending data.
     * @param file       The file to be sent.
     * @param fileLength The length of the file.
     */
    private static void sendFileInChunks(DataOutputStream out, File file, long fileLength) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            System.out.println(getTimeString() + " Sending file...");

            int chunkSize = CHUNK_SIZE; // Size of each chunk

            byte[] buffer = new byte[chunkSize];

            MessageDigest md = MessageDigest.getInstance(CHECKSUM_ALGORITHM);

            int bytesRead;
            long totalBytesLeft = fileLength;
            long end = (fileLength / chunkSize) + 1;
            for (int i = 0; i < end; i++) {
                bytesRead = fileInputStream.read(buffer, START_IDX, (int) Math.min(totalBytesLeft, chunkSize));
                out.write(buffer, START_IDX, bytesRead);
                md.update(buffer, START_IDX, bytesRead); // Update the file hash every time we write a new chunk to the stream
                totalBytesLeft -= chunkSize;
            }

            // Completes the hash computation and returns the final value of the hash as byte[]
            byte[] fileHash = md.digest();

            // Send the hash of the file as byte[]
            out.write(fileHash);
            out.flush();

            System.out.println(getTimeString() + " File sent!");
        } catch (IOException | NoSuchAlgorithmException ex) {
            System.out.println("No such file existing!");
        }
    }

    /**
     * Sends a byte array over a DataOutputStream.
     *
     * @param out           The DataOutputStream for sending data.
     * @param allBytesArray The byte array to be sent.
     */
    private static void sendAllBytesArray(DataOutputStream out, byte[] allBytesArray) {
        try {
            out.write(allBytesArray);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fills a byte array with various components of the file to be sent.
     *
     * @param totalLengthCommandAsByteArray    The length of the command.
     * @param commandBytes                     The command bytes.
     * @param fileLengthAsByteArray            The length of the file.
     * @param totalLengthFileNameAsByteArray   The length of the file name.
     * @param fileNameBytes                    The file name bytes.
     * @return The filled byte array.
     */
    private static byte[] fillAllBytesArray(byte[] totalLengthCommandAsByteArray, byte[] commandBytes,
                                            byte[] fileLengthAsByteArray, byte[] totalLengthFileNameAsByteArray,
                                            byte[] fileNameBytes) {
        int allLengths = totalLengthCommandAsByteArray.length + commandBytes.length +
                fileLengthAsByteArray.length + totalLengthFileNameAsByteArray.length + fileNameBytes.length;
        byte[] allBytesArray = new byte[allLengths];

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
