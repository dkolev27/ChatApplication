package chatapp_combined.streamsCommand;

/**
 * The type Close stream.
 * The class is responsible for closing network streams and sockets using the Stream class
 *
 * @author Dimitar Kolev
 */
public class CloseStream {

    private final Stream stream;

    /**
     * Instantiates a new Close stream.
     *
     * @param stream the stream
     */
    public CloseStream(Stream stream) {
        this.stream = stream;
    }

    /**
     * Method to execute the closing of the stream
     */
    public void execute() {
        stream.closeEverything();
    }

}
