package chatapp_combined.messagesCommand;

// Class representing a command to receive a message
public class ReceiveMessage implements Command {

    // Field to hold the message instance
    private final Message message;

    // Constructor
    /**
     * Constructs a ReceiveMessage command with the specified Message.
     *
     * @param message The message instance to be received.
     */
    public ReceiveMessage(Message message) {
        this.message = message;
    }

    /**
     * Executes the command to receive the message.
     */
    @Override
    public void execute() {
        message.receive();
    }

}
