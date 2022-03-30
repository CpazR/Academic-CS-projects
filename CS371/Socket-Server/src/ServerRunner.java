import java.io.IOException;
import java.net.SocketException;
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
                    try {
                        switch (userOperation) {
                            case UPLOAD:
                                if (inputFromClient.length == 3) {
                                    var fileSizeInBytes = Long.parseLong(inputFromClient[2]);
                                    server.uploadFile(inputFromClient[1], (int) fileSizeInBytes);
                                } else {
                                    throw new IOException("ERROR: UPLOAD operation requires two arguments, {\"fileName\", \"fileSizeInBytes\"}");
                                }
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
                            case HEARTBEAT:
                                // Do not perform any operations, try next iteration for valid input
                                break;
                            default:
                                System.err.println("INVALID OPERATION: " + userOperation);
                                break;
                        }
                    } catch (IOException e) {
                        System.err.println("ERROR: Failed to execute operation " + userOperation);
                        e.printStackTrace();

                        if (e instanceof SocketException) {
                            System.err.println("ERROR: Connection reset, closing socket");
                            server.closeServer();
                        }
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
}

