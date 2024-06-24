package chatapp_combined.Utility;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static chatapp_combined.Utility.CommonUtils.getLength;
import static chatapp_combined.Utility.CommonUtils.getTimeString;

public class ReceivingFileUtils {

    // Constants
    private static final int BYTES_FOR_ONE_LONG = 8;
    private static final int START_IDX = 0;
    private static final int CHUNK_SIZE = 1024;

    private static final String DIRECTORY_TO_RECEIVE = "receivedFiles";
    private static final String CHECKSUM_ALGORITHM = "MD5";

    /**
     * Receives a file from the sender through the DataInputStream.
     *
     * @param senderName The name of the sender.
     * @param in The DataInputStream from which the file is read.
     * @throws IOException If an I/O error occurs.
     */
    public static void receiveFile(String senderName, DataInputStream in) throws IOException {
        // Read the total length of the file
        long fileLength = getFileLength(in);

        // Read the total fileName length
        int fileNameLength = getLength(in);

        // Read the fileName
        String fileName = getFileName(fileNameLength, in);

        // Create a file object to receive the file
        File fileToReceive = new File(DIRECTORY_TO_RECEIVE, fileName);

        // Receive the file in chunks of 1024 bytes (1KB)
        receiveFileInChunks(fileLength, fileToReceive, in, senderName);
    }

    /**
     * Receives the file in chunks of specified size and writes it to the disk.
     *
     * @param fileLength The total length of the file.
     * @param fileToReceive The file object to write the received file.
     * @param in The DataInputStream from which the file is read.
     * @param senderName The name of the sender.
     */
    private static void receiveFileInChunks(long fileLength, File fileToReceive, DataInputStream in, String senderName) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileToReceive)) {
            System.out.println(getTimeString() + "Receiving file...");

            // Set the chunk size for reading the file
            final int chunkSize = CHUNK_SIZE;      // sends 9.67GB for 4 min and 56 seconds
            //final int chunkSize = CHUNK_SIZE * 4;  // sends 9.67GB for 5 min and 38 seconds
            //final int chunkSize = CHUNK_SIZE * 8;  // sends 9.67GB for 4 min and 56 seconds
            //final int chunkSize = CHUNK_SIZE * 16; // checksum failed
            //final int chunkSize = CHUNK_SIZE * 32; // checksum failed

            byte[] buffer = new byte[chunkSize];

            // Create a MessageDigest instance for checksum calculation
            MessageDigest md = MessageDigest.getInstance(CHECKSUM_ALGORITHM);

            int bytesRead;
            long totalBytesLeft = fileLength;
            long end = (fileLength / chunkSize) + 1;

            // Read the file in chunks
            for (int i = 0; i < end; i++) {
                bytesRead = in.read(buffer, START_IDX, (int) Math.min(totalBytesLeft, chunkSize));
                fileOutputStream.write(buffer, START_IDX, bytesRead);
                md.update(buffer, START_IDX, bytesRead); // Update the file hash with the new chunk
                totalBytesLeft -= chunkSize;
            }

            // Receive the hash of the file as byte[]
            final int HASH_SIZE = 16; // MD5 generates a hash of 16 bytes (128 bits)
            byte[] receivedFileHash = new byte[HASH_SIZE];
            in.readFully(receivedFileHash);
            System.out.println("received file checkSum:   " + new String(receivedFileHash, StandardCharsets.UTF_16)); // For testing

            // Calculate the hash of the received file
            byte[] calculatedFileHash = md.digest();
            System.out.println("calculated file checkSum: " + new String(calculatedFileHash, StandardCharsets.UTF_16)); // For testing

            // Compare received hash and calculated hash to verify file integrity
            if (Arrays.equals(receivedFileHash, calculatedFileHash)) {
                System.out.printf("%sFile received by %s!" + System.lineSeparator(), getTimeString(), senderName);
            } else {
                System.out.println(getTimeString() +
                        " Received file hash doesn't match calculated hash. File may be corrupted." +
                        " Try to send it again.");
            }
        } catch (IOException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Reads the file name from the DataInputStream.
     *
     * @param fileNameLength The length of the file name.
     * @param in The DataInputStream from which the file name is read.
     * @return The file name as a String.
     * @throws IOException If an I/O error occurs.
     */
    private static String getFileName(int fileNameLength, DataInputStream in) throws IOException {
        byte[] fileNameBytes = new byte[fileNameLength];
        in.readFully(fileNameBytes, START_IDX, fileNameLength);
        return new String(fileNameBytes, StandardCharsets.UTF_8);
    }

    /**
     * Reads and retrieves the length of the file sent by the other user.
     *
     * @param in The DataInputStream from which the file length is read.
     * @return The length of the file received from the other user.
     * @throws IOException If an I/O error occurs.
     */
    private static long getFileLength(DataInputStream in) throws IOException {
        byte[] totalFileLengthByteArray = new byte[BYTES_FOR_ONE_LONG];
        in.readFully(totalFileLengthByteArray, START_IDX, BYTES_FOR_ONE_LONG);
        return SendingFileUtils.convertByteArrayToLong(totalFileLengthByteArray);
    }
}
