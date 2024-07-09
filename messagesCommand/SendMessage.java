package chatapp_combined.messagesCommand;

/**
 * The type Send message.
 *
 * @author Dimitar Kolev
 */
// Class representing a command to send a message
public class SendMessage implements Command {

    private final Message message;


    /**
     * Constructs a SendMessage command with the specified Message.
     *
     * @param message The message instance to be sent.
     */
    public SendMessage(final Message message) {
        this.message = message;
    }

    /**
     * Executes the command to send the message.
     */
    @Override
    public void execute() {
        message.send();
    }

}
