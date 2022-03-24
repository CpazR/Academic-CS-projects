import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;

public class Server {
    private final int serverPort = 4444;
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private DataInputStream inputStream = null;
    private DataOutputStream outputStream = null;
    private boolean isConnected = false;

    public final static int MAX_BUFFER_SIZE = Integer.MAX_VALUE;

    private final File directory = new File("./sharedFiles/");

    Server() {
        initializeServer();
    }

    private void initializeServer() {
        try {
            // Establish server on port {#serverPort}
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Server started");

            // Wait for client to connect
            System.out.println("Waiting for client...");
            socket = serverSocket.accept();

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
            clientInput = inputStream.readUTF();
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
            Files.copy(inputStream, fileToUpload.toPath());
            System.out.println("INFO: File received from client");
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
