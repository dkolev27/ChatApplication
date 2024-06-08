package chatapp_combined.streamsCommand;

// The CloseStream class is responsible for closing network streams and sockets using the Stream class
public class CloseStream {

    // Field
    private final Stream stream;

    // Constructor
    public CloseStream(Stream stream) {
        this.stream = stream;
    }

    // Method to execute the closing of the stream
    public void execute() {
        stream.closeEverything();
    }

}
