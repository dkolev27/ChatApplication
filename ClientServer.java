package chatapp_combined;

import java.io.*;

public class ClientServer {
    public static void main(String[] args) throws IOException, InterruptedException {

        // The main thread that runs the whole program
        Manager manager = new Manager();
        manager.manage();

    }
}
