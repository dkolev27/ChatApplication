package chatapp_combined.messagesCommand;

import chatapp_combined.Utility.SendingFileUtils;
import chatapp_combined.Utility.SendingMessageUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Represents a message exchanged between users in the chat application.
 */
public class Message implements Serializable {

    // to be sure that this current version of the class can be serialized/deserialized
    // if we update the class, we must generate a new value
    @Serial
    private static final long serialVersionUID = -2417267367232152732L;


    // Constants
    private final int BYTES_FOR_ONE_INTEGER = 4;
    private final int BYTES_FOR_ONE_LONG = 8;
    private final String SENDING_MESSAGE_COMMAND = "-m";
    private final String SENDING_FILE_COMMAND = "-f";
    private final int START_IDX = 0;
    private final int CHUNK_SIZE = 1024;

    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RESET = "\u001B[0m";

    private static final String DIRECTORY_TO_RECEIVE = "receivedFiles";
    private static final String CHECKSUM_ALGORITHM = "MD5";
    private static final String DATE_FORMAT = "HH:mm:ss";


    // Fields
    private ServerSocket serverSocket; // MAX capacity = 8192 bytes (8 kilobytes) (in most of the cases)
    private Socket clientSocket;       // MAX capacity = 8192 bytes (8 kilobytes) (in most of the cases)
    private DataInputStream in;        // MAX capacity = 8192 bytes (8 kilobytes)
    private DataOutputStream out;      // MAX capacity = 8192 bytes (8 kilobytes)
    private String senderName;
    private final Scanner scanner = new Scanner(System.in);


    // Constructor
    public Message(ServerSocket serverSocket, Socket clientSocket, DataInputStream in, DataOutputStream out,
                   String senderName) {
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.in = in;
        this.out = out;
        this.senderName = senderName;
    }


    // Methods
    /**
     * Initiates the sending of messages.
     */
    public synchronized void send() {
        new Thread(() -> {
            Thread.currentThread().setName("Send Message Thread");

            while (true) {
                sendToOtherUser();
            }
        }).start();
    }

    /**
     * Initiates the receiving of messages.
     */
    public synchronized void receive() {
        new Thread(() -> {
            Thread.currentThread().setName("Receive Message Thread");

            try {
                while (true) {
                    receiveFromOtherUser();
                }
            } catch (EOFException e) {
                System.out.println(getTimeString() + senderName + " disconnected!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Sends a message or a file to the other user.
     */
    public void sendToOtherUser() {
        String msg = scanner.nextLine();
        String messageToSend = msg;
        String[] msgArr = msg.split(" ");
        if (msgArr[START_IDX].equals(SENDING_MESSAGE_COMMAND) ||
                msgArr[START_IDX].equals(SENDING_FILE_COMMAND)) {

            String command = msgArr[START_IDX];
            msgArr = Arrays.copyOfRange(msgArr, 1, msgArr.length);
            msg = String.join(" ", msgArr);

            if (command.equals(SENDING_MESSAGE_COMMAND)) {
                System.out.println(ANSI_BLUE + getTimeString() + "Me: " + msg + ANSI_RESET);
            }

            try {
                switch (command) {
                    case SENDING_MESSAGE_COMMAND -> SendingMessageUtils.sendMessageBytes(messageToSend, out);
                    case SENDING_FILE_COMMAND    -> SendingFileUtils.sendFileBytes(messageToSend, out);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid command! Try again.");
        }
    }

    /**
     * Receives a message from the other user.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void receiveFromOtherUser() throws IOException {
        // Read the length of the command (4 bytes)
        int commandLength = getLength();

        // Read the command bytes and construct a String representing the command
        String command = getCommand(commandLength);

        // Determine the action to take based on the received command
        runCommand(command);
    }

    /**
     * Executes the appropriate action based on the received command.
     *
     * @param command The command received from the other user.
     * @throws IOException If an I/O error occurs.
     */
    private void runCommand(String command) throws IOException {
        // Execute different actions based on the command received
        switch (command) {
            case SENDING_MESSAGE_COMMAND -> receiveMessage();
            case SENDING_FILE_COMMAND    -> receiveFile();
        }
    }

    /**
     * Receives a message from the other user and prints it to the console.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void receiveMessage() throws IOException {
        // Read the total length of the message
        int messageLength = getLength();

        // Read the message bytes and construct a String representing the message
        String message = getMessage(messageLength);

        // Print the received message along with the sender's name and timestamp
        System.out.println(getTimeString() + senderName + ": " + message);
    }


    private void receiveFile() throws IOException {
        // Read the total length of the file
        long fileLength = getFileLength();

        // Read the total fileName length
        int fileNameLength = getLength();

        // Read the fileName
        String fileName = getFileName(fileNameLength);

        // now, we have to receive the file in chunks
        File fileToReceive = new File(DIRECTORY_TO_RECEIVE, fileName);

        // receive the file in chunks of 1024 bytes (1KB)
        receiveFileInChunks(fileLength, fileToReceive);
    }

    private void receiveFileInChunks(long fileLength, File fileToReceive) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileToReceive)) {
            System.out.println(getTimeString() + "Receiving file...");

            int chunkSize = CHUNK_SIZE;      // sends 9.67GB for 4 min and 56 seconds
            //int chunkSize = CHUNK_SIZE * 4;  // sends 9.67GB for 5 min and 38 seconds
            //int chunkSize = CHUNK_SIZE * 8;  // sends 9.67GB for 4 min and 56 seconds
            //int chunkSize = CHUNK_SIZE * 16; // checksum failed
            //int chunkSize = CHUNK_SIZE * 32; // checksum failed

            byte[] buffer = new byte[chunkSize];

            MessageDigest md = MessageDigest.getInstance(CHECKSUM_ALGORITHM);

            int bytesRead;
            long totalBytesLeft = fileLength;
            long end = (fileLength / chunkSize) + 1;
            for (int i = 0; i < end; i++) {
                bytesRead = in.read(buffer, START_IDX, (int) Math.min(totalBytesLeft, chunkSize));
                fileOutputStream.write(buffer, START_IDX, bytesRead);
                md.update(buffer, START_IDX, bytesRead); // update the file hash every time we read a new chunk from the stream
                totalBytesLeft -= chunkSize;
            }

            // receive the hash of the file as byte[]
            final int HASH_SIZE = 16; // because the MD5 algorithm generates a byte[] which has a size of the hash value 16 bytes (128 bits)
            byte[] receivedFileHash = new byte[HASH_SIZE];
            // read the hash value of the file that has been sent and write it into receivedFileHash
            in.readFully(receivedFileHash);
            System.out.println("received file checkSum:   " + new String(receivedFileHash, StandardCharsets.UTF_16)); //  FOR TESTING

            // calculate the hash file that has been read from the stream
            byte[] calculatedFileHash = md.digest();
            System.out.println("calculated file checkSum: " + new String(calculatedFileHash, StandardCharsets.UTF_16)); // FOR TESTING

            // compare receivedFileHash and calculatedFileHash
            if (Arrays.equals(receivedFileHash, calculatedFileHash)) {
                System.out.println(getTimeString() +
                        " File received!");
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
     * Reads and retrieves the command sent by the other user.
     *
     * @param commandLength The length of the command to be read.
     * @return The command received from the other user.
     * @throws IOException If an I/O error occurs.
     */
    private String getCommand(int commandLength) throws IOException {
        // Read the bytes representing the command and construct a String
        byte[] commandBytes = new byte[commandLength];
        in.readFully(commandBytes, START_IDX, commandLength);
        return new String(commandBytes, StandardCharsets.UTF_8);
    }

    /**
     * Reads and retrieves the length of a message or command.
     *
     * @return The length of the message or command.
     * @throws IOException If an I/O error occurs.
     */
    private int getLength() throws IOException {
        // Read the bytes representing the length of the message or command
        byte[] totalLengthCommandByteArray = new byte[BYTES_FOR_ONE_INTEGER]; // first 4 bytes
        in.readFully(totalLengthCommandByteArray, START_IDX, BYTES_FOR_ONE_INTEGER);
        // Convert the byte array to an integer
        return SendingMessageUtils.convertByteArrayToInt(totalLengthCommandByteArray);
    }

    /**
     * Reads and retrieves the message sent by the other user.
     *
     * @param messageLength The length of the message to be read.
     * @return The message received from the other user.
     * @throws IOException If an I/O error occurs.
     */
    private String getMessage(int messageLength) throws IOException {
        // Read the bytes representing the message and construct a String
        byte[] messageBytes = new byte[messageLength];
        in.readFully(messageBytes, START_IDX, messageLength);
        return new String(messageBytes, StandardCharsets.UTF_8);
    }

    /**
     * Receives and retrieves the file name sent by the other user.
     *
     * @param fileNameLength The length of the file name to be read.
     * @return The file name received from the other user.
     * @throws IOException If an I/O error occurs.
     */
    private String getFileName(int fileNameLength) throws IOException {
        byte[] fileNameBytes = new byte[fileNameLength];
        in.readFully(fileNameBytes, START_IDX, fileNameLength);
        return new String(fileNameBytes, StandardCharsets.UTF_8);
    }

    /**
     * Reads and retrieves the length of the file sent by the other user.
     *
     * @return The length of the file received from the other user.
     * @throws IOException If an I/O error occurs.
     */
    private long getFileLength() throws IOException {
        byte[] totalFileLengthByteArray = new byte[BYTES_FOR_ONE_LONG];
        in.readFully(totalFileLengthByteArray, START_IDX, BYTES_FOR_ONE_LONG);
        return SendingFileUtils.convertByteArrayToLong(totalFileLengthByteArray);
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

}
