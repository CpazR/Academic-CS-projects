package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {

    /* Socket operations have a timeout of 10 seconds */
    private static final int SOCKET_TIMEOUT = 10000;
    // Byte buffer size for chunks in one KB
    public static final int MAX_BUFFER_SIZE = 10000;
    private static final boolean TIMEOUT_ENABLED = false;
    /* Total number of times an operation can be attempted before decided a failure */
    private static final int OPERATION_RETRY_COUNT = 3;

    private Socket mainSocket = null;

    // Receive acknowledgements from server
    private DataInputStream inputStream = null;
    // Send data to server
    private DataOutputStream outputStream = null;
    private boolean isConnected = false;

    private final File directory = new File("./sharedFiles/");

    Client() {

    }

    Client(String address, int port) {
        initializeClient(address, port);
    }

    /**
     * Performs validation on request to ensure it's valid before attempting to sent via IO stream
     * If valid, performs initial request
     */
    private void performInitialRequest(ServerOperations operation, String... arguments) throws IOException {
        // Make sure file name is provided for operations that require them
        if (!isConnected) {
            throw new SocketException("Connection not established");
        }

        if (operation.equals(ServerOperations.UPLOAD) || operation.equals(ServerOperations.DOWNLOAD) || operation.equals(ServerOperations.DELETE)) {
            if (arguments.length == 0) {
                throw new IOException("No file name given for operation that requires a file name.");
            } else {
                arguments[0] = "\"" + arguments[0] + "\"";
            }
        }

        outputStream.writeUTF(buildCommandRequest(operation.getInputValue(), arguments));
    }

    private ServerOperations waitForResponse() throws IOException {
        ServerOperations responseOperation;
        var responseCode = inputStream.readUTF();
        if (!Objects.equals(ServerOperations.getOperation(responseCode), ServerOperations.HEARTBEAT) && !Objects.equals(ServerOperations.getOperation(responseCode), ServerOperations.ACKNOWLEDGE)) {
            var errorMessage = "Unknown response operation received: " + responseCode;
            throw new IOException(errorMessage);
        }
        responseOperation = ServerOperations.getOperation(responseCode);
        Objects.requireNonNull(responseOperation);
        return responseOperation;
    }

    private String buildCommandRequest(String operationName, String... arguments) {
        StringBuilder completeRequest = new StringBuilder(operationName);

        if (arguments.length > 0) {
            for (String argument : arguments) {
                completeRequest.append(" ").append(argument);
            }
        }

        return completeRequest.toString();
    }

    public boolean initializeClient(String address, int port) {
        var successfulConnect = false;
        try {
            mainSocket = new Socket();
            mainSocket.connect(new InetSocketAddress(address, port), SOCKET_TIMEOUT);
            if (TIMEOUT_ENABLED) {
                mainSocket.setSoTimeout(SOCKET_TIMEOUT);
            }
            System.out.println("Client connected");

            inputStream = new DataInputStream(mainSocket.getInputStream());
            outputStream = new DataOutputStream(mainSocket.getOutputStream());
            isConnected = true;

            if (!directory.exists()) {
                directory.mkdir();
                System.out.println("Initialized shared directory.");
            }
            successfulConnect = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return successfulConnect;
    }

    public void closeClient() {
        try {
            outputStream.writeUTF("CLOSE");
            outputStream.flush();

            mainSocket.close();
            inputStream.close();
            outputStream.close();
            isConnected = false;
            System.out.println("Client disconnected successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void uploadFile(String fileName) throws IOException {
        // Check if file exists
        var fileToUpload = new File(directory.getAbsolutePath() + "\\" + fileName);
        if (fileToUpload.exists()) {
            // Send initial upload request with file size in bytes to server. Signals it to wait for file.
            performInitialRequest(ServerOperations.UPLOAD, fileName, String.valueOf(Files.size(fileToUpload.toPath())));
            var fileBuffer = Files.readAllBytes(fileToUpload.toPath());
            AtomicInteger chunkCount = new AtomicInteger();
            AtomicInteger chunkedByteCount = new AtomicInteger();

            ServerOperations response = null;
            var retryCount = 0;
            while (retryCount < OPERATION_RETRY_COUNT) {
                try {
                    var uploadThread = new Thread(() -> {
                        try {
                            for (byte b : fileBuffer) {
                                outputStream.write(b);

                                if (chunkedByteCount.incrementAndGet() == MAX_BUFFER_SIZE) {
                                    chunkCount.incrementAndGet();
                                    chunkedByteCount.set(0);
                                }
                            }
                            outputStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    uploadThread.start();

                    var startTime = System.currentTimeMillis();
                    while (uploadThread.isAlive()) {
                        if (System.currentTimeMillis() - startTime >= 1000) {
                            System.out.println("Uploaded " + chunkedByteCount.get() + " bytes");
                            startTime = System.currentTimeMillis();
                        }
                    }
                    System.out.println("Uploaded " + chunkedByteCount.get() * chunkCount.get() + " kb/S");
                    System.out.println("INFO: Sent file to server");

                    response = waitForResponse();
                    retryCount = OPERATION_RETRY_COUNT;

                } catch (SocketTimeoutException e) {
                    retryCount++;
                    if (retryCount < OPERATION_RETRY_COUNT) {
                        System.err.println("ERROR: Request timed out, retrying");
                    } else {
                        System.err.println("ERROR: Too many attempts, request failed");
                    }
                }
            }

            Objects.requireNonNull(response);
            if (response.equals(ServerOperations.ACKNOWLEDGE)) {
                System.out.println("INFO: File uploaded successfully");
            } else {
                System.err.println("ERROR: Failure when uploading file - " + response);
            }
        } else {
            System.err.println("ERROR: Specified file does not exist");
        }
    }

    public void downloadFile(String fileName) throws IOException {
        System.out.println("INFO: Attempting to receive file from client \"" + fileName + "\"");
        performInitialRequest(ServerOperations.DOWNLOAD, fileName);

        var uploadedFile = new File(directory.getAbsolutePath() + "\\" + fileName);
        // TODO: track statistics
        if (uploadedFile.createNewFile()) {
            System.out.println("INFO: File created");
        }
        var fileByteSize = inputStream.readLong();

        var uploadedFileByteData = inputStream.readNBytes((int) fileByteSize);
        Files.write(uploadedFile.toPath(), uploadedFileByteData);
        System.out.println("INFO: File downloaded from client");
        // NOTE: Ack isn't required here. If error occurs, client will automatically trigger a re-request
    }

    public String[] showRemoteFolderContents() throws IOException {
        performInitialRequest(ServerOperations.DIR);
        var directoryListString = inputStream.readUTF();
        System.out.println("Remote files:\n------------");
        System.out.println(directoryListString);
        System.out.println("------------");
        return directoryListString.split("\n");
    }

    public String[] showLocalFolderContents() throws IOException {
        var fileNames = Arrays.stream(Objects.requireNonNull(directory.listFiles())).map(File::getName).toArray();
        var convertedFileNames = Arrays.copyOf(fileNames, fileNames.length, String[].class);
        System.out.println("Local files:\n------------");
        for (String convertedFileName : convertedFileNames) {
            System.out.println(convertedFileName);
        }
        System.out.println("------------");
        return convertedFileNames;
    }

    public void deleteFile(String fileName) throws IOException {
        performInitialRequest(ServerOperations.DELETE, fileName);

        var response = waitForResponse();
        if (response.equals(ServerOperations.ACKNOWLEDGE)) {
            System.out.println("INFO: File deleted successfully");
        } else {
            System.err.println("ERROR: Failure when requesting deletion of file - " + response);
        }

    }
}
