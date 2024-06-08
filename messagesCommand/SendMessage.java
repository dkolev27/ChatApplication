package chatapp_combined.messagesCommand;

// Class representing a command to send a message
public class SendMessage implements Command {

    // Field to hold the message instance
    private final Message message;

    // Constructor
    /**
     * Constructs a SendMessage command with the specified Message.
     *
     * @param message The message instance to be sent.
     */
    public SendMessage(Message message) {
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
