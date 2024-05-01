package chatapp_combined.Utility;

import chatapp_combined.messagesCommand.Message;
import com.sun.tools.javac.Main;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;

public class SendingFileUtils {

    public static final int BYTES_FOR_LONG = 8;
    public static final int CHUNK_SIZE = 1024;
    public static final int START_IDX = 0;

    public static byte[] convertLongToByteArray(final long l) {
        ByteBuffer buffer = ByteBuffer.allocate(BYTES_FOR_LONG);
        buffer.putLong(l);
        return buffer.array();
    }

    public static long convertByteArrayToLong(final byte[] byteArray) {
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        return buffer.getLong();
    }

    public static void sendFileBytes(String message, DataOutputStream out) throws IOException {
        final int END_IDX = 2;
        String command = new String(Arrays.copyOfRange(message.getBytes(), START_IDX, END_IDX)); // separate the command from the full inputted string
        //String path = new String(Arrays.copyOfRange(message.getBytes(), END + 1, message.length()));

        // get the length of the command in byte[]
        int totalLengthCommand = command.length();
        byte[] totalLengthCommandAsByteArray = SendingMessageUtils.convertIntToByteArray(totalLengthCommand);

        // get the command in byte[]
        byte[] commandBytes = command.getBytes();

        //File file = new File("D:\\Java\\Abalta\\Network Programming\\src\\chatapp_combined\\filesToSend\\mitkoPhoto.jpg");      // 314KB - done
        File file = new File("D:\\Java\\Abalta\\Network Programming\\src\\chatapp_combined\\filesToSend\\indianaJonesFilm.mkv");      // 9.67GB - done


        // get the file length in byte[]
        long fileLength = file.length();
        byte[] fileLengthAsByteArray = convertLongToByteArray(fileLength);

        String fileName = file.getName();

        // get the fileName length in byte[]
        int totalLengthFileName = fileName.length();
        byte[] totalLengthFileNameAsByteArray = SendingMessageUtils.convertIntToByteArray(totalLengthFileName);

        // get the fileName in byte[]
        byte[] fileNameBytes = fileName.getBytes();


        // construct a byte[] consists of: the bytes of command length, the command bytes, the length of the file,
        // the length of the filename and the filename itself
        byte[] allBytesArray = fillAllBytesArray(totalLengthCommandAsByteArray, commandBytes, fileLengthAsByteArray,
                totalLengthFileNameAsByteArray, fileNameBytes);

        // at first, send the bytes that represent the command length, the command, the length of the file as byte[],
        // the length of the fileName and the fileName in byte[]
        sendAllBytesArray(out, allBytesArray);

        // now, we have to send the file in chunks
        sendFileInChunks(out, file, fileLength);
    }

    public static void sendFileInChunks(DataOutputStream out, File file, long fileLength) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            System.out.println(Message.getTimeString() + " Sending file...");

            int chunkSize = CHUNK_SIZE;      // sends 9.67GB for 4 min and 56 seconds
            //int chunkSize = CHUNK_SIZE * 4;  // sends 9.67GB for 5 min and 38 seconds
            //int chunkSize = CHUNK_SIZE * 8;  // sends 9.67GB for 4 min and 56 seconds
            //int chunkSize = CHUNK_SIZE * 16; // checksum failed

            byte[] buffer = new byte[chunkSize];

            MessageDigest md = MessageDigest.getInstance("MD5");

            int bytesRead;
            long totalBytesLeft = fileLength;
            long end = (fileLength / chunkSize) + 1;
            for (int i = 0; i < end; i++) {
                bytesRead = fileInputStream.read(buffer, START_IDX, (int) Math.min(totalBytesLeft, chunkSize));
                out.write(buffer, START_IDX, bytesRead);
                md.update(buffer, START_IDX, bytesRead); // update the file hash every time we write a new chunk to the stream
                totalBytesLeft -= chunkSize;
            }

            // completes the hash computation and returns the final value of the hash as byte[]
            byte[] fileHash = md.digest();
            // send the hash of the file as byte[]
            out.write(fileHash);

            out.flush();

            System.out.println(Message.getTimeString() + " File sent!");
        } catch (IOException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
    }

    private static void sendAllBytesArray(DataOutputStream out, byte[] allBytesArray) {
        try {
            out.write(allBytesArray);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] fillAllBytesArray(byte[] totalLengthCommandAsByteArray, byte[] commandBytes,
                                            byte[] fileLengthAsByteArray,         byte[] totalLengthFileNameAsByteArray,
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
