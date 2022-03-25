import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Server {
    /**
     * Socket operations have a timeout of 10 seconds
     */
    private static final int SOCKET_TIMEOUT = 10000;
    public static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE;
    private final static int SERVER_PORT = 4444;

    // Socket to manage client connection
    private Socket socket = null;
    // Socket to manage server state
    private ServerSocket serverSocket = null;
    // Receive data from client
    private DataInputStream inputStream = null;
    // Send data to client
    private DataOutputStream outputStream = null;
    private boolean isConnected = false;


    private final File directory = new File("./sharedFiles/");

    Server() {
        initializeServer();
    }

    private void initializeServer() {
        try {
            // Establish server on port {#serverPort}
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Server started");

            // Wait for client to connect
            System.out.println("Waiting for client...");
            socket = serverSocket.accept();
            socket.setSoTimeout(SOCKET_TIMEOUT);

            // Client connected, establish data input/output
            System.out.println("Client accepted");
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            isConnected = true;

            if (!directory.exists()) {
                directory.mkdir();
                System.out.println("Initialized shared directory.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String waitForInput() {
        var clientInput = "";
        try {
            // Only check for input if there is data in input stream
            if (inputStream.available() > 0) {
                clientInput = inputStream.readUTF();
            } else {
                clientInput = "HB";
            }
        } catch (IOException e) {
            System.err.println("ERROR WHILE RECEIVING INPUT: ");
            e.printStackTrace();
            clientInput = "";

            // Some sort of error occurred with socket, close connection
            if (e instanceof SocketException) {
                closeServer();
            }
        }
        return clientInput;
    }

    public boolean isConnected() {
        return isConnected;
    }

    /**
     * CLIENT is uploading a file
     */
    public void uploadFile(String fileName) {
        System.out.println("INFO: Attempting to receive file from client - " + fileName);
        var fileToUpload = new File(directory.getAbsolutePath() + "\\" + fileName);

        // TODO: track statistics
        try {
            // Copy byte data to destination, replacing or creating file if needed
            Files.copy(inputStream, fileToUpload.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("INFO: File received from client");

            outputStream.writeUTF(ServerOperations.ACKNOWLEDGE.getInputValue());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * CLIENT is requesting to download a file
     */
    public void downloadFile(String fileName) {
        System.out.println("INFO: Attempting to receive file from client - " + fileName);
    }

    public void showFolderContents() {

    }

    public void deleteFile(String fileName) {
    }

    public void closeServer() {
        try {
            System.out.println("Closing...");

            socket.close();
            inputStream.close();
            isConnected = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
