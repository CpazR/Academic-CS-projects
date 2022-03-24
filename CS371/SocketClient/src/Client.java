import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;

public class Client {

    private Socket mainSocket = null;
    private DataInputStream inputStream = null;
    private DataOutputStream outputStream = null;
    private boolean isConnected = false;

    public final static int MAX_BUFFER_SIZE = Integer.MAX_VALUE;

    private final File directory = new File("./sharedFiles/");

    Client() {

    }

    Client(String address, int port) {
        initializeClient(address, port);
    }

    private void sendRequest(ServerOperations operation, String... arguments) {
        // Build bitfield(?) for operations and append arguments
        try {
            outputStream.writeUTF(buildCommandRequest(operation.getInputValue(), arguments));

            if (operation.equals(ServerOperations.UPLOAD) || operation.equals(ServerOperations.DOWNLOAD) || operation.equals(ServerOperations.DELETE)) {
                if (arguments.length == 0) {
                    throw new IOException("No file name given for operation that requires a file name.");
                }
            }

            if (operation.equals(ServerOperations.UPLOAD)) {
                // Check if file exists
                var fileToUpload = new File(directory.getAbsolutePath() +"\\"+ arguments[0]);
                if (fileToUpload.exists()) {
                    var fileBuffer = Files.readAllBytes(fileToUpload.toPath());

                    outputStream.write(fileBuffer);
                    outputStream.flush();
                    System.out.println("INFO: Sent file to server");
                } else {
                    System.err.println("ERROR: Specified file does not exist");
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String buildCommandRequest(String operationName, String[] arguments) {
        StringBuilder completeRequest = new StringBuilder(operationName);

        if (arguments.length > 0) {
            for (String argument : arguments) {
                completeRequest.append(" ").append(argument);
            }
        }

        return completeRequest.toString();
    }

    public void initializeClient(String address, int port) {
        try {
            mainSocket = new Socket(address, port);
            System.out.println("Client connected");

            inputStream = new DataInputStream(mainSocket.getInputStream());
            outputStream = new DataOutputStream(mainSocket.getOutputStream());
            isConnected = true;

            if (!directory.exists()) {
                directory.mkdir();
                System.out.println("Initialized shared directory.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeClient() {
        try {
            outputStream.writeUTF("CLOSE");
            outputStream.flush();

            mainSocket.close();
            inputStream.close();
            outputStream.close();
            isConnected = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void uploadFile(String fileName) {
        sendRequest(ServerOperations.UPLOAD, fileName);
    }

    public void downloadFile(String fileName) {
        sendRequest(ServerOperations.DOWNLOAD, fileName);
    }

    public void showFolderContents() {
        sendRequest(ServerOperations.DIR);
    }

    public void deleteFile(String fileName) {
        sendRequest(ServerOperations.DELETE);
    }
}
