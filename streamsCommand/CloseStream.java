package chatapp_combined.streamsCommand;

public class CloseStream {
    private final Stream stream;

    public CloseStream(Stream stream) {
        this.stream = stream;
    }

    public void execute() {
        stream.closeEverything();
    }
}
