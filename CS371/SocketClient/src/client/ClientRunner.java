package client;

import GUI.ApplicationGUI.ApplicationContext;

/**
 * Parse input and pass commands to client
 */
public class ClientRunner {

    static ApplicationContext context;

    public static void main(String[] args) {

        var client = new Client();

        context = new ApplicationContext("Shared Folder Client", client);
    }
}
