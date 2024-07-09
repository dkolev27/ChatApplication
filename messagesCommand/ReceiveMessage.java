package chatapp_combined.messagesCommand;

/**
 * The type Receive message.
 * Class representing a command to receive a message.
 *
 * @author Dimitar Kolev
 */
public class ReceiveMessage implements Command {

    private final Message message;


    /**
     * Constructs a ReceiveMessage command with the specified Message.
     *
     * @param message The message instance to be received.
     */
    public ReceiveMessage(final Message message) {
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
