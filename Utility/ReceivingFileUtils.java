package chatapp_combined.Utility;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static chatapp_combined.Utility.CommonUtils.*;


/**
 * The type Receiving file utils.
 *
 * @author Dimitar Kolev
 */
public final class ReceivingFileUtils {

    /**
     * LONG_SIZE represents the number of bytes a long has,
     * a long has 8 bytes
     */
    private static final int LONG_SIZE = 8;
    private static final int START_IDX = 0;
    private static final int CHUNK_SIZE = 1024;

    private static final String DIR_TO_RECEIVE = "receivedFiles";
    private static final String CHECKSUM_ALGORITHM = "MD5";

    /**
     * Private constructor does not allow an instance to be created
     */
    private ReceivingFileUtils() {

    }


    /**
     * Receives a file from the sender through the DataInputStream.
     *
     * @param senderName  The name of the sender.
     * @param inputStream The DataInputStream from which the file is read.
     * @throws IOException If an I/O error occurs.
     */
    public static void receiveFile(final String senderName, final DataInputStream inputStream) throws IOException {
        // Read the total length of the file
        final long fileLength = getFileLength(inputStream);

        // Read the total fileName length
        final int fileNameLength = getLength(inputStream);

        // Read the fileName
        final String fileName = getFileName(fileNameLength, inputStream);

        // Create a file object to receive the file
        final File fileToReceive = new File(DIR_TO_RECEIVE, fileName);

        // Receive the file inputStream chunks of 1024 bytes (1KB)
        receiveFileInChunks(fileLength, fileToReceive, inputStream, senderName);
    }

    /**
     * Receives the file inputStream chunks of specified size and writes it to the disk.
     *
     * @param fileLength    The total length of the file.
     * @param fileToReceive The file object to write the received file.
     * @param inputStream   The DataInputStream from which the file is read.
     * @param senderName    The name of the sender.
     */
    private static void receiveFileInChunks(final long fileLength, final File fileToReceive, final DataInputStream inputStream,
                                            final String senderName) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileToReceive)) {
            System.out.println(ANSI_YELLOW + getTimeString() + "Receiving file..." + ANSI_RESET);

            // Set the chunk size for reading the file
            //final int chunkSize = CHUNK_SIZE;      // sends 9.67GB for 4 min and 56 seconds
            //final int chunkSize = CHUNK_SIZE * 4;  // sends 9.67GB for 5 min and 38 seconds
            //final int chunkSize = CHUNK_SIZE * 8;  // sends 9.67GB for 4 min and 56 seconds
            //final int chunkSize = CHUNK_SIZE * 16; // checksum failed
            //final int chunkSize = CHUNK_SIZE * 32; // checksum failed

            final byte[] buffer = new byte[CHUNK_SIZE];

            // Create a MessageDigest instance for checksum calculation
            final MessageDigest messageDigest = MessageDigest.getInstance(CHECKSUM_ALGORITHM);

            int bytesRead;
            long totalBytesLeft = fileLength;
            final long end = (fileLength / CHUNK_SIZE) + 1;

            // Read the file inputStream chunks
            for (int i = 0; i < end; i++) {
                bytesRead = inputStream.read(buffer, START_IDX, (int) Math.min(totalBytesLeft, CHUNK_SIZE));
                fileOutputStream.write(buffer, START_IDX, bytesRead);
                messageDigest.update(buffer, START_IDX, bytesRead); // Update the file hash with the new chunk
                totalBytesLeft -= CHUNK_SIZE;
            }

            // Receive the hash of the file as byte[]
            final int hashSize = 16; // MD5 generates a hash of 16 bytes (128 bits)
            final byte[] receivedFileHash = new byte[hashSize];
            inputStream.readFully(receivedFileHash);
            // System.out.println("received file checkSum:   " + new String(receivedFileHash, StandardCharsets.UTF_16)); // For testing

            // Calculate the hash of the received file
            final byte[] calcFileHash = messageDigest.digest();
            // System.out.println("calculated file checkSum: " + new String(calcFileHash, StandardCharsets.UTF_16)); // For testing

            // Compare received hash and calculated hash to verify file integrity
            if (Arrays.equals(receivedFileHash, calcFileHash)) {
                System.out.printf(ANSI_YELLOW + "%sFile received by %s!" + ANSI_RESET + System.lineSeparator(),
                        getTimeString(), senderName);
            } else {
                System.out.println(ANSI_RED + getTimeString() +
                        " Received file hash doesn't match calculated hash. File may be corrupted." +
                        " Try to send it again." + ANSI_RESET);
            }
        } catch (IOException ex) {
            System.out.println(ANSI_RED + "Nothing to be received. Connection lost!" + ANSI_RESET);
            fileToReceive.deleteOnExit();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads the file name from the DataInputStream.
     *
     * @param fileNameLength The length of the file name.
     * @param inputStream    The DataInputStream from which the file name is read.
     * @return The file name as a String.
     * @throws IOException If an I/O error occurs.
     */
    private static String getFileName(final int fileNameLength, final DataInputStream inputStream) throws IOException {
        final byte[] fileNameBytes = new byte[fileNameLength];
        inputStream.readFully(fileNameBytes, START_IDX, fileNameLength);

        return new String(fileNameBytes, StandardCharsets.UTF_8);
    }

    /**
     * Reads and retrieves the length of the file sent by the other user.
     *
     * @param inputStream The DataInputStream from which the file length is read.
     * @return The length of the file received from the other user.
     * @throws IOException If an I/O error occurs.
     */
    private static long getFileLength(final DataInputStream inputStream) throws IOException {
        final byte[] fileLength = new byte[LONG_SIZE];
        inputStream.readFully(fileLength, START_IDX, LONG_SIZE);

        return convertByteArrayToLong(fileLength);
    }

}
