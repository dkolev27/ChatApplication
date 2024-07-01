package chatapp_combined.messagesCommand;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

import static chatapp_combined.Utility.CommonUtils.*;
import static chatapp_combined.Utility.ReceivingFileUtils.receiveFile;
import static chatapp_combined.Utility.ReceivingMessageUtils.getCommand;
import static chatapp_combined.Utility.ReceivingMessageUtils.receiveMessage;
import static chatapp_combined.Utility.SendingFileUtils.sendFileBytes;
import static chatapp_combined.Utility.SendingMessageUtils.sendMessageBytes;

/**
 * Represents a message exchanged between users in the chat application.
 */
public class Message implements Serializable {

    // to be sure that this current version of the class can be serialized/deserialized
    // if we update the class, we must generate a new value
    @Serial
    private static final long serialVersionUID = -2417267367232152732L;


    // Constants
    private final String MESSAGE_COMMAND = "-m";
    private final String FILE_COMMAND = "-f";
    private final int START_IDX = 0;


    // Fields
    private final DataInputStream in;        // MAX capacity = 8192 bytes (8 kilobytes)
    private final DataOutputStream out;      // MAX capacity = 8192 bytes (8 kilobytes)
    private final String senderName;
    private final Scanner scanner = new Scanner(System.in);


    // Constructor
    public Message(DataInputStream in, DataOutputStream out,
                   String senderName) {
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

            try {
                while (true) {
                    sendToOtherUser();
                }
            } catch (IOException e) {
                throw new RuntimeException();
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
            } catch (IOException e) {
                System.out.printf(ANSI_PURPLE + getTimeString() + "%s logged out!" + System.lineSeparator() + ANSI_RESET, senderName);
            }
        }).start();
    }

    /**
     * Sends a message or a file to the other user.
     */
    public void sendToOtherUser() throws IOException {
        String msg = scanner.nextLine();
        String messageToSend = msg;
        String[] msgArr = msg.split(" ");

        if (msgArr[START_IDX].equals(MESSAGE_COMMAND) || msgArr[START_IDX].equals(FILE_COMMAND)) {
            String command = msgArr[START_IDX];
            msgArr = Arrays.copyOfRange(msgArr, 1, msgArr.length);
            msg = String.join(" ", msgArr);

            if (command.equals(MESSAGE_COMMAND)) {
                System.out.println(ANSI_BLUE + getTimeString() + "Me: " + msg + ANSI_RESET);
            }

            runSendingCommand(command, messageToSend);

        } else {
            System.out.println(ANSI_RED + "Invalid command! Try again." + ANSI_RESET);
        }
    }

    /**
     * Receives a message from the other user.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void receiveFromOtherUser() throws IOException {
        // Read the length of the command (4 bytes)
        int commandLength = getLength(in);

        // Read the command bytes and construct a String representing the command
        String command = getCommand(commandLength, in);

        // Determine the action to take based on the received command
        runReceivingCommand(command);
    }

    /**
     * Executes the appropriate action based on the received command.
     *
     * @param command The command represents if you send a message or a file.
     * @param messageToSend The message as String to be sent to other user.
     * @throws IOException If an I/O error occurs.
     */
    private void runSendingCommand(String command, String messageToSend) throws IOException {
        switch (command) {
            case MESSAGE_COMMAND -> sendMessageBytes(messageToSend, out);
            case FILE_COMMAND -> sendFileBytes(messageToSend, out);
        }
    }

    /**
     * Executes the appropriate action based on the received command.
     *
     * @param command The command received from the other user.
     * @throws IOException If an I/O error occurs.
     */
    private void runReceivingCommand(String command) throws IOException {
        // Execute different actions based on the command received
        switch (command) {
            case MESSAGE_COMMAND -> receiveMessage(senderName, in);
            case FILE_COMMAND -> receiveFile(senderName, in);
        }
    }

}
