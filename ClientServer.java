package chatapp_combined;

import java.io.*;

public class ClientServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Manager manager = new Manager();
        manager.manage();
    }
}
