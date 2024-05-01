package chatapp_combined.messagesCommand;

import chatapp_combined.APIs.Command;

public class SendMessage implements Command {

    // Field
    private final Message message;


    // Constructor
    public SendMessage(Message message) {
        this.message = message;
    }


    // Overridden method
    @Override
    public void execute() {
        message.send();
    }

}
