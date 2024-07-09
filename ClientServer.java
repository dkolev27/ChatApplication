package chatapp_combined;

import java.io.IOException;

/**
 * The type Client server.
 */
public final class ClientServer {

    private ClientServer() {

    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        Manager.manage();

    }

}
