package chatapp_combined.messagesCommand;

import chatapp_combined.APIs.Command;

public class SendMessage implements Command {
    private final Message message;

    public SendMessage(Message message) {
        this.message = message;
    }

    @Override
    public void execute() {
        message.send();
    }
}
