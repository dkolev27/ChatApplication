package chatapp_combined.messagesCommand;

import java.io.IOException;

// Interface representing a generic command in the chat application
public interface Command {

    /**
     * Executes the command. This method may throw an IOException,
     * depending on the specific implementation.
     *
     * @throws IOException if an I/O error occurs during command execution
     */
    void execute() throws IOException;

}
