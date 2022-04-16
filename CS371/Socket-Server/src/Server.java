import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
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

    private final File directory = new File("sharedFiles/");

    Server() {
        initializeServer();
        listener();
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
            inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            outputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            isConnected = true;

            if (!directory.exists()) {
                directory.mkdir();
                System.out.println("Initialized shared directory.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listener() {
        var listener = new Thread(() -> {

            while (!socket.isClosed()) {

                if (isConnected()) {
                    var unParameterizedInput = waitForInput();
                    var inputTokenList = new ArrayList<String>();
                    var matcher = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(unParameterizedInput);
                    while (matcher.find())
                        inputTokenList.add(matcher.group(1));
                    var inputFromClient = Arrays.copyOf(inputTokenList.toArray(), inputTokenList.size(),
                            String[].class);
                    var userOperation = ServerOperations.getOperation(inputFromClient[0]);

                    if (Objects.nonNull(userOperation)) {
                        try {
                            switch (userOperation) {
                                case UPLOAD:
                                    if (inputFromClient.length == 3) {
                                        var fileSizeInBytes = Long.parseLong(inputFromClient[2]);
                                        uploadFile(inputFromClient[1].replaceAll("\"", ""), (int) fileSizeInBytes);
                                    } else {
                                        throw new IOException(
                                                "ERROR: UPLOAD operation requires two arguments, {\"fileName\", \"fileSizeInBytes\"}");
                                    }
                                    break;
                                case DOWNLOAD:
                                    downloadFile(inputFromClient[1].replaceAll("\"", ""));
                                    break;
                                case DIR:
                                    showFolderContents();
                                    break;
                                case DELETE:
                                    deleteFile(inputFromClient[1].replaceAll("\"", ""));
                                    break;
                                case CLOSE:
                                    closeServer();
                                    break;
                                case HEARTBEAT:
                                    // Do not perform any operations, try next iteration for valid input
                                    System.out.println("INFO: Heartbeat at " + LocalDateTime.now());
                                    break;
                                default:
                                    System.err.println("INVALID OPERATION: " + userOperation);
                                    break;
                            }
                        } catch (IOException e) {
                            sendNack();
                            System.err.println("ERROR: Failed to execute operation " + userOperation);
                            e.printStackTrace();

                            if (e instanceof SocketException) {
                                System.err.println("ERROR: Connection reset, closing socket");
                                closeServer();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.err.println("ERROR: Failed to receive operation from client.");
                    }
                }
            }
        });
        listener.start();
    }

    private void sendNack() {
        try {
            outputStream.writeUTF(ServerOperations.NEG_ACKNOWLEDGE.getInputValue());
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendAck() {
        try {
            outputStream.writeUTF(ServerOperations.ACKNOWLEDGE.getInputValue());
            outputStream.flush();
        } catch (IOException e) {
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

            // Wait for input from client. Can be a simple heartbeat to prevent server timeout
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
    public void uploadFile(String fileName, int fileByteSize) throws IOException, InterruptedException {
        System.out.println("INFO: Attempting to receive file from client \"" + fileName + "\"");
        var uploadedFile = new File(directory.getAbsolutePath() + "/" + fileName);

        if (uploadedFile.createNewFile()) {
            System.out.println("INFO: File created");
        }
        // Wait for file stream
        var receivedBytes = 0;
        var bytesPerSecond = 0;
        var uploadedFileByteData = new byte[fileByteSize];
        // So long as bytes are available in stream, collect data until all bytes in file are collected
        var startTime = System.currentTimeMillis();
        while (receivedBytes < fileByteSize && receivedBytes != -1) {
            try {
                uploadedFileByteData[receivedBytes] = inputStream.readByte();
                receivedBytes++;
                bytesPerSecond++;

                // Send number of bytes processed per second back to client
                if (System.currentTimeMillis() - startTime >= 1000) {
                    startTime = System.currentTimeMillis();
                    outputStream.writeInt(bytesPerSecond);
                    outputStream.flush();
                    bytesPerSecond = 0;
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Set received bytes to -1, indicating an error
                receivedBytes = -1;
            }
        }

        if (receivedBytes != -1) {
            // Flush any possible remaining output from stream
            outputStream.flush();
            Files.write(uploadedFile.toPath(), uploadedFileByteData);
            System.out.println("INFO: File received from client");

            System.out.println("INFO: Waiting for new command.");
        } else {
            System.err.println("ERROR: An error occurred when receiving uploaded file");
            sendNack();
        }
    }

    /**
     * CLIENT is requesting to download a file
     */
    public void downloadFile(String fileName) throws IOException {

        // Check if file exists
        var fileToUpload = new File(directory.getAbsolutePath() + "/" + fileName);
        if (fileToUpload.exists()) {
            // Use an ACK / NACK to indicate to client that file exists
            sendAck();
            // Send signal back to client with size of requested file.
            outputStream.writeLong(Files.size(fileToUpload.toPath()));
            outputStream.flush();
            System.out.println("INFO: Attempting to send file to client \"" + fileName + "\"");
            var fileBuffer = Files.readAllBytes(fileToUpload.toPath());
            var startTime = System.currentTimeMillis();
            for (byte b : fileBuffer) {
                outputStream.write(b);
                if (System.currentTimeMillis() - startTime >= 1000) {
                    outputStream.flush();
                    startTime = System.currentTimeMillis();
                }
            }
            System.out.println("INFO: Sent file to client");
        } else {
            System.err.println("ERROR: Specified file does not exist");
            sendNack();
        }
    }

    public void showFolderContents() throws IOException {
        System.out.println("INFO: Sending file list to client");
        // Build directory content message
        var directoryListString = Arrays.stream(Objects.requireNonNull(directory.listFiles())).map(
                File::getName).collect(Collectors.joining("\n"));

        outputStream.writeUTF(directoryListString);
        outputStream.flush();
    }

    public void deleteFile(String fileName) throws IOException {

        var fileToDelete = new File(directory.getAbsolutePath() + "/" + fileName);
        Files.deleteIfExists(fileToDelete.toPath());

        // Send ack back to client
        sendAck();
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
