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

public class Message implements Serializable {
    // to be sure that this current version of the class can be serialized/deserialized
    // if we update the class, we must generate a new value
    @Serial
    private static final long serialVersionUID = -2417267367232152732L;

    public final int BYTES_FOR_ONE_INTEGER = 4;
    public final int BYTES_FOR_ONE_LONG = 8;
    public final String SENDING_MESSAGE_COMMAND = "-m";
    public final String SENDING_FILE_COMMAND = "-f";
    public final int START_IDX = 0;


    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RESET = "\u001B[0m";


    public ServerSocket serverSocket; // MAX capacity = 8192 bytes (8 kilobytes) (in most of the cases)
    public Socket clientSocket;       // MAX capacity = 8192 bytes (8 kilobytes) (in most of the cases)
    public DataInputStream in;        // MAX capacity = 8192 bytes (8 kilobytes)
    public DataOutputStream out;      // MAX capacity = 8192 bytes (8 kilobytes)
    public String senderName;
    public final Scanner scanner = new Scanner(System.in);


    public Message(ServerSocket serverSocket, Socket clientSocket, DataInputStream in, DataOutputStream out, String senderName) {
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.in = in;
        this.out = out;
        this.senderName = senderName;
    }

    public synchronized void send() {
        new Thread(() -> {
            Thread.currentThread().setName("Send Message Thread");

            while (true) {
                sendToOtherUser();
            }
        }).start();
    }

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

    private void receiveFromOtherUser() throws IOException {
        // read first 4 bytes that they construct one int, which is the length of the command
        int commandLength = getLength();

        // read the command bytes, they construct a String that holds the command
        String command = getCommand(commandLength);

        // if the received command is "-m" it will send a message
        // if the received command is "-f" it will send a file
        runCommand(command);
    }

    private void runCommand(String command) throws IOException {
        switch (command) {
            case SENDING_MESSAGE_COMMAND -> receiveMessage();
            case SENDING_FILE_COMMAND    -> receiveFile();
        }
    }

    private void receiveMessage() throws IOException {
        // Read the total length of the message
        int messageLength = getLength();

        // Read the message bytes
        String message = getMessage(messageLength);
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
        String directoryToReceive = "D:\\Java\\Abalta\\Network Programming\\src\\chatapp_combined\\receivedFiles";
        File fileToReceive = new File(directoryToReceive, fileName);

        // receive the file in chunks of 1024 bytes (1KB)
        receiveFileInChunks(fileLength, fileToReceive);
    }

    private void receiveFileInChunks(long fileLength, File fileToReceive) {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileToReceive)) {
            System.out.println(getTimeString() + "Receiving file...");

            int chunkSize = SendingFileUtils.CHUNK_SIZE;      // sends 9.67GB for 4 min and 56 seconds
            //int chunkSize = SendingFileUtils.CHUNK_SIZE * 4;  // sends 9.67GB for 5 min and 38 seconds
            //int chunkSize = SendingFileUtils.CHUNK_SIZE * 8;  // sends 9.67GB for 4 min and 56 seconds
            //int chunkSize = SendingFileUtils.CHUNK_SIZE * 16; // checksum failed
            //int chunkSize = SendingFileUtils.CHUNK_SIZE * 32; // checksum failed

            byte[] buffer = new byte[chunkSize];

            MessageDigest md = MessageDigest.getInstance("MD5");

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
            // read the sent hash value of the file and write it into receivedFileHash
            in.readFully(receivedFileHash);
            System.out.println("received file checkSum:   " + new String(receivedFileHash, StandardCharsets.UTF_16)); // TESTING

            // calculate the hash file that has been read from the stream
            byte[] calculatedFileHash = md.digest();
            System.out.println("calculated file checkSum: " + new String(calculatedFileHash, StandardCharsets.UTF_16)); // TESTING

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

    private String getCommand(int commandLength) throws IOException {
        byte[] commandBytes = new byte[commandLength];
        in.readFully(commandBytes, START_IDX, commandLength);
        return new String(commandBytes, StandardCharsets.UTF_8);
    }

    private int getLength() throws IOException {
        byte[] totalLengthCommandByteArray = new byte[BYTES_FOR_ONE_INTEGER]; // first 4 bytes
        in.readFully(totalLengthCommandByteArray, START_IDX, BYTES_FOR_ONE_INTEGER);
        return SendingMessageUtils.convertByteArrayToInt(totalLengthCommandByteArray);
    }

    private String getMessage(int messageLength) throws IOException {
        byte[] messageBytes = new byte[messageLength];
        in.readFully(messageBytes, START_IDX, messageLength);
        return new String(messageBytes, StandardCharsets.UTF_8);
    }

    private String getFileName(int fileNameLength) throws IOException {
        byte[] fileNameBytes = new byte[fileNameLength];
        in.readFully(fileNameBytes, START_IDX, fileNameLength);
        return new String(fileNameBytes, StandardCharsets.UTF_8);
    }

    private long getFileLength() throws IOException {
        byte[] totalFileLengthByteArray = new byte[BYTES_FOR_ONE_LONG];
        in.readFully(totalFileLengthByteArray, START_IDX, BYTES_FOR_ONE_LONG);
        return SendingFileUtils.convertByteArrayToLong(totalFileLengthByteArray);
    }

    public static String getTimeString() {
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = currentTime.format(formatter);

        return "[" + formattedTime + "] ";
    }
}