package chatapp_combined.messagesCommand;

import chatapp_combined.APIs.Command;

public class ReceiveMessage implements Command {
    private final Message message;

    public ReceiveMessage(Message message) {
        this.message = message;
    }

    @Override
    public void execute() {
        message.receive();
    }
}
