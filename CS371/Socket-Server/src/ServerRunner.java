import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ServerRunner {

    public static void main(String[] args) {
        boolean serverActive = true;

        var server = new Server();

        while (serverActive) {

            if (server.isConnected()) {
                var inputFromClient = server.waitForInput().split(" ");
                var userOperation = ServerOperations.getOperation(inputFromClient[0]);

                if (Objects.nonNull(userOperation)) {
                    switch (userOperation) {
                        case UPLOAD:
                            server.uploadFile(inputFromClient[1]);
                            break;
                        case DOWNLOAD:
                            server.downloadFile(inputFromClient[1]);
                            break;
                        case DIR:
                            server.showFolderContents();
                            break;
                        case DELETE:
                            server.deleteFile(inputFromClient[1]);
                            break;
                        case CLOSE:
                            server.closeServer();
                            break;
                        default:
                            System.err.println("INVALID OPERATION: " + userOperation);
                            break;
                    }
                } else {
                    System.err.println("ERROR: Failed to receive operation from client.");
                }
            } else {
                // Reset server, wait for new client
                server = new Server();
            }
        }
    }

    @Deprecated
    private static void performAction(String inputFromClient) {
        var directory = "./files/";
        switch (inputFromClient) {
            case "CREATE":
                // Create a path local to the server
                var file = new File(directory + "somefile.txt");
                if (file.isDirectory()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (file.mkdir()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }
}

