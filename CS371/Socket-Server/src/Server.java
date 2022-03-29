import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class Server {
    /**
     * Socket operations have a timeout of 10 seconds
     */
    private static final int SOCKET_TIMEOUT = 10000;
    public static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE;
    private final static int SERVER_PORT = 4444;
    private static final boolean TIMEOUT_ENABLED = false;

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
            if (TIMEOUT_ENABLED) {
                socket.setSoTimeout(SOCKET_TIMEOUT);
            }
            // Client connected, establish data input/output
            System.out.println("Client accepted: " + socket.getInetAddress());
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
            // TODO: Edge case where server instance is in limbo when client errors out
            if (!isConnected()) {
                throw new SocketException("ERROR: Connection closed");
            }

            // Only check for input if there is data in input stream
            var bytesAvailable = inputStream.available();
            if (bytesAvailable > 0) {
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
    public void uploadFile(String fileName, int fileByteSize) throws IOException {
        System.out.println("INFO: Attempting to receive file from client \"" + fileName + "\"");
        var uploadedFile = new File(directory.getAbsolutePath() + "\\" + fileName);

        // TODO: track statistics
        if (uploadedFile.createNewFile()) {
            System.out.println("INFO: File created");
        }
        // Wait for file stream
        // TODO: need to do chunked downloads for large files
        var uploadedFileByteData = inputStream.readNBytes(fileByteSize);
        Files.write(uploadedFile.toPath(), uploadedFileByteData);
        System.out.println("INFO: File received from client");

        // Send ACK back to client
        outputStream.writeUTF(ServerOperations.ACKNOWLEDGE.getInputValue());
    }

    /**
     * CLIENT is requesting to download a file
     */
    public void downloadFile(String fileName) throws IOException {

        // Check if file exists
        var fileToUpload = new File(directory.getAbsolutePath() + "\\" + fileName);
        if (fileToUpload.exists()) {
            // Send signal back to client with size of requested file.
            outputStream.writeLong(Files.size(fileToUpload.toPath()));
            System.out.println("INFO: Attempting to send file to client \"" + fileName + "\"");
            var fileBuffer = Files.readAllBytes(fileToUpload.toPath());

            outputStream.write(fileBuffer);
            outputStream.flush();
            System.out.println("INFO: Sent file to client");
        } else {
            System.err.println("ERROR: Specified file does not exist");
        }
    }

    public void showFolderContents() throws IOException {
        System.out.println("INFO: Sending file list to client");
        // Build directory content message
        var directoryListString = "Files in shared directory:\n-------\n" +
                Arrays.stream(Objects.requireNonNull(directory.listFiles()))
                        .map(File::getName).collect(Collectors.joining("\n")) +
                "\n-------\n";


        outputStream.writeUTF(directoryListString);

    }

    public void deleteFile(String fileName) throws IOException {

        var fileToDelete = new File(directory.getAbsolutePath() + "\\" + fileName);
        Files.deleteIfExists(fileToDelete.toPath());

        // Send ack back to client
        outputStream.writeUTF(ServerOperations.ACKNOWLEDGE.getInputValue());

    }

    public void closeServer() {
        try {
            System.out.println("Closing...");

            socket.close();
            inputStream.close();
            serverSocket.close();
            isConnected = false;
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
