package client;

import GUI.ApplicationGUI.ApplicationContext;

import java.io.IOException;
import java.net.SocketException;
import java.util.Objects;
import java.util.Scanner;

/**
 * Parse input and pass commands to client
 */
public class ClientRunner {

    static Scanner inputScanner = new Scanner(System.in);

    public static void main(String[] args) {

        boolean clientRunning = true;
        boolean clientConnecting = false;
        var client = new Client();

        new ApplicationContext("Shared Folder Client", client);
    }

    private static String[] getLocalInput() {
        inputScanner.useDelimiter("\\n");
        return inputScanner.nextLine().split(" ");
    }

    public static void mainLoop(Client client) {
        while (true) {

            var localUserInput = getLocalInput();
            var userOperation = ServerOperations.getOperation(localUserInput[0].toUpperCase());
            if (Objects.nonNull(userOperation)) {
                try {
                    switch (userOperation) {
                        case CONNECT:
                            if (localUserInput.length == 3) {
                                var address = localUserInput[1];
                                var port = Integer.parseInt(localUserInput[2]);

                                client.initializeClient(address, port);
                            } else {
                                throw new IOException("ERROR: CONNECT protocol requires two arguments, {\"address\", \"port\"}");
                            }
                            break;
                        case UPLOAD:

                            client.uploadFile(localUserInput[1]);
                            break;
                        case DOWNLOAD:
                            client.downloadFile(localUserInput[1]);
                            break;
                        case DIR:
                            client.showFolderContents();
                            break;
                        case DELETE:
                            client.deleteFile(localUserInput[1]);
                            break;
                        case CLOSE:
                            client.closeClient();
                            break;
                        default:
                            System.err.println("INVALID OPERATION: " + userOperation);
                            break;
                    }
                } catch (IOException e) {
                    System.err.println("ERROR: Failed to execute operation " + userOperation);
                    e.printStackTrace();

                    if (e instanceof SocketException) {
                        System.err.println("ERROR: Connection reset, closing connection");
                        client.closeClient();
                    }
                }
            } else {
                System.err.println("ERROR: Input is null.");
            }
        }
    }
}
