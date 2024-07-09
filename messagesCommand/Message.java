package chatapp_combined.messagesCommand;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;

import static chatapp_combined.Utility.CommonUtils.*;
import static chatapp_combined.Utility.ReceivingFileUtils.receiveFile;
import static chatapp_combined.Utility.ReceivingMessageUtils.getCommand;
import static chatapp_combined.Utility.ReceivingMessageUtils.receiveMessage;
import static chatapp_combined.Utility.SendingFileUtils.sendFileBytes;
import static chatapp_combined.Utility.SendingMessageUtils.sendMessageBytes;

/**
 * Represents a message exchanged between users in the chat application.
 *
 * @author Dimitar Kolev
 */
public class Message implements Serializable {

    /**
     * To be sure that this current version of the class can be serialized/deserialized.
     * If we update the class, we must generate a new value
     */
    @Serial
    private static final long serialVersionUID = -2417267367232152732L;


    private static final String MESSAGE_COMMAND = "-m";
    private static final String FILE_COMMAND = "-f";
    private static final int START_IDX = 0;
    private static final Set<String> COMMAND_STRINGS = Set.of(MESSAGE_COMMAND, FILE_COMMAND);


    private final DataInputStream inputStream;        // MAX capacity = 8192 bytes (8 kilobytes)
    private final DataOutputStream outputStream;      // MAX capacity = 8192 bytes (8 kilobytes)
    private final String senderName;
    private final Scanner scanner = new Scanner(System.in);


    /**
     * Instantiates a new Message.
     * Constructor
     *
     * @param inputStream  the in
     * @param outputStream the out
     * @param senderName   the sender name
     */
    public Message(final DataInputStream inputStream, final DataOutputStream outputStream, final String senderName) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.senderName = senderName;
    }


    /**
     * Initiates the sending of messages.
     */
    public void send() {
        synchronized (this) {
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
    }

    /**
     * Initiates the receiving of messages.
     */
    public void receive() {
        synchronized (this) {
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
    }

    /**
     * Sends a message or a file to the other user.
     *
     * @throws IOException the io exception
     */
    public void sendToOtherUser() throws IOException {
        String msg = scanner.nextLine();
        final String messageToSend = msg;
        String[] msgArr = msg.split(" ");

        if (COMMAND_STRINGS.contains(msgArr[START_IDX])) {
            final String command = msgArr[START_IDX];
            msgArr = Arrays.copyOfRange(msgArr, 1, msgArr.length);
            msg = String.join(" ", msgArr);

            if (MESSAGE_COMMAND.equals(command)) {
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
        final int commandLength = getLength(inputStream);

        // Read the command bytes and construct a String representing the command
        final String command = getCommand(commandLength, inputStream);

        // Determine the action to take based on the received command
        runReceivingCommand(command);
    }

    /**
     * Executes the appropriate action based on the received command.
     *
     * @param command       The command represents if you send a message or a file.
     * @param messageToSend The message as String to be sent to other user.
     * @throws IOException If an I/O error occurs.
     */
    private void runSendingCommand(final String command, final String messageToSend) throws IOException {
        switch (command) {
            case MESSAGE_COMMAND -> sendMessageBytes(messageToSend, outputStream);
            case FILE_COMMAND -> sendFileBytes(messageToSend, outputStream);
            default -> throw new IllegalStateException("Unexpected value: " + command);
        }
    }

    /**
     * Executes the appropriate action based on the received command.
     *
     * @param command The command received from the other user.
     * @throws IOException If an I/O error occurs.
     */
    private void runReceivingCommand(final String command) throws IOException {
        switch (command) {
            case MESSAGE_COMMAND -> receiveMessage(senderName, inputStream);
            case FILE_COMMAND -> receiveFile(senderName, inputStream);
            default -> throw new IllegalStateException("Unexpected value: " + command);
        }
    }

}
