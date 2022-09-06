import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Server {
    /**
     * Socket operations have a timeout of 10 seconds
     */
    private static final int SOCKET_TIMEOUT = 10000;
    private static final boolean TIMEOUT_ENABLED = false;

    // Socket to manage client connection
    private Socket socket = null;
    // Socket to manage server state
    private final ServerSocket serverSocket;
    // Receive data from client
    private DataInputStream inputStream = null;
    // Send data to client
    private DataOutputStream outputStream = null;
    private boolean isConnected = false;

    private final File directory = new File("sharedFiles/");

    private final Map<String, Long> fileSize;
    private final Map<String, Integer> downloadCount;
    private final Map<String, String> uploadTime;

    Server(ServerSocket serverSocket, Map<String, Long> fileSize, Map<String, Integer> downloadCount, Map<String, String> uploadTime) {
        this.serverSocket = serverSocket;
        this.fileSize = fileSize;
        this.downloadCount = downloadCount;
        this.uploadTime = uploadTime;
    }

    public void init() {
        initializeServer();
        listener();
    }

    private void initializeServer() {
        try {
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
            if (e instanceof SocketException) {
                var socketException = (SocketException) e;
                var message = socketException.getMessage();
                if (message.equals("Socket closed")) {
                    closeServer();
                }
            }
        }
    }

    private void listener() {
        var listener = new Thread(() -> {

            while (!socket.isClosed()) {

                if (isConnected()) {
                    // Parse input into easily read tokens
                    var unParameterizedInput = waitForInput();
                    var inputTokenList = new ArrayList<String>();
                    var matcher = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(unParameterizedInput);
                    while (matcher.find())
                        inputTokenList.add(matcher.group(1));
                    var inputFromClient = Arrays.copyOf(inputTokenList.toArray(), inputTokenList.size(),
                            String[].class);

                    // Get specific operation from tokens
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
                                case ERROR:
                                    System.err.println(
                                            "ERROR: An error has in processing an operation. Server is likely busy.");
                                    break;
                                case HEARTBEAT:
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
        var clientInput = "ERR";
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
        sendAck();
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

        fileSize.put(fileName, (long) fileByteSize);
        uploadTime.put(fileName, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        downloadCount.put(fileName, 0);


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
            for (byte b : fileBuffer) {
                outputStream.write(b);
            }
            outputStream.flush();
            downloadCount.put(fileName, downloadCount.get(fileName) + 1);
            System.out.println("INFO: Sent file to client");
        } else {
            System.err.println("ERROR: Specified file does not exist");
            sendNack();
        }
    }

    public void showFolderContents() throws IOException {
        System.out.println("INFO: Sending file list to client");
        // Build directory content message
        var directoryListString = Arrays.stream(Objects.requireNonNull(directory.listFiles())).map(file -> {
            var fileName = file.getName();
            // Only show file if it has an upload time. Or, has been completely uploaded.
            return uploadTime.containsKey(fileName) ? fileName + "\t Size: " + fileSize.get(
                    fileName) + "\t Uploaded at: " + uploadTime.get(
                    fileName) + "\t Times Downloaded: " + downloadCount.get(fileName) : "";
        }).collect(Collectors.joining("\n"));
        sendAck();

        outputStream.writeUTF(directoryListString);
        outputStream.flush();
    }

    public void deleteFile(String fileName) throws IOException {
        var fileToDelete = new File(directory.getAbsolutePath() + "/" + fileName);
        Files.deleteIfExists(fileToDelete.toPath());

        fileSize.remove(fileName);
        uploadTime.remove(fileName);
        downloadCount.remove(fileName);

        // Send ack back to client
        sendAck();
    }

    public void closeServer() {
        try {
            System.out.println("Closing...");

            socket.close();
            inputStream.close();
            outputStream.close();
            isConnected = false;
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
