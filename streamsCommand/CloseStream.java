package chatapp_combined.streamsCommand;

public class CloseStream {

    // Field
    private final Stream stream;


    // Constructor
    public CloseStream(Stream stream) {
        this.stream = stream;
    }


    // Close stream method
    public void execute() {
        stream.closeEverything();
    }

}
