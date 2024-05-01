package chatapp_combined.messagesCommand;

import chatapp_combined.APIs.Command;

public class ReceiveMessage implements Command {

    // Field
    private final Message message;


    // Constructor
    public ReceiveMessage(Message message) {
        this.message = message;
    }


    // Method to receive message
    @Override
    public void execute() {
        message.receive();
    }
}
