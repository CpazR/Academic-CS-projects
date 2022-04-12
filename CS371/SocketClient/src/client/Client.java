package client;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

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
    private boolean isBusy = false;

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
            mainSocket.setTcpNoDelay(true);
            mainSocket.connect(new InetSocketAddress(address, port), SOCKET_TIMEOUT);
            if (TIMEOUT_ENABLED) {
                mainSocket.setSoTimeout(SOCKET_TIMEOUT);
            }
            System.out.println("Client connected");

            inputStream = new DataInputStream(new BufferedInputStream(mainSocket.getInputStream()));
            outputStream = new DataOutputStream(new BufferedOutputStream(mainSocket.getOutputStream()));
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

    public boolean isBusy() {
        return isBusy;
    }

    public void uploadFile(String fileName) throws IOException {
        // Check if file exists
        var fileToUpload = new File(directory.getAbsolutePath() + "\\" + fileName);
        if (fileToUpload.exists()) {
            // Send initial upload request with file size in bytes to server. Signals it to wait for file.
            performInitialRequest(ServerOperations.UPLOAD, fileName, String.valueOf(Files.size(fileToUpload.toPath())));
            var fileBuffer = Files.readAllBytes(fileToUpload.toPath());

            var uploadThread = new Thread(() -> {
                isBusy = true;
                ServerOperations response = null;
                var bytesPerSecond = 0;
                var totalByteCount = 0;
                try {
                    var startTime = System.currentTimeMillis();
                    for (byte b : fileBuffer) {
                        outputStream.write(b);
                        bytesPerSecond++;
                        totalByteCount++;

                        if (System.currentTimeMillis() - startTime >= 1000) {
                            System.out.println("INFO: Uploaded " + (double) bytesPerSecond / Math.pow(10, 6) + " mb/s | " + totalByteCount + " of " + fileBuffer.length);
                            startTime = System.currentTimeMillis();
                            bytesPerSecond = 0;
                        }
                    }
                    outputStream.flush();
                    System.out.println("INFO: File uploaded " + (double) bytesPerSecond / Math.pow(10, 6) + " mb/s | " + totalByteCount + " of " + fileBuffer.length);

                    System.out.println("INFO: Waiting for server ACK");
                    response = waitForResponse();
                } catch (SocketTimeoutException e) {
                    System.err.println("ERROR: Request timed out");
                } catch (IOException e) {
                    e.printStackTrace();

                }

                Objects.requireNonNull(response);
                if (response.equals(ServerOperations.ACKNOWLEDGE)) {
                    System.out.println("INFO: File uploaded successfully");
                } else {
                    System.err.println("ERROR: Failure when uploading file - " + response);
                }
                isBusy = false;
            });
            uploadThread.start();
        } else {
            System.err.println("ERROR: Specified file does not exist");
        }
    }

    public void downloadFile(String fileName) throws IOException {
        System.out.println("INFO: Attempting to receive file from client \"" + fileName + "\"");
        performInitialRequest(ServerOperations.DOWNLOAD, fileName);

        var uploadedFile = new File(directory.getAbsolutePath() + "\\" + fileName);
        if (uploadedFile.createNewFile()) {
            System.out.println("INFO: File created");
        }
        var fileByteSize = inputStream.readLong();

        // Wait for file stream
        var downloadThread = new Thread(() -> {
            var receivedBytes = 0;
            var uploadedFileByteData = new byte[(int) fileByteSize];
            while (receivedBytes < fileByteSize && receivedBytes != -1) {
                try {
                    // So long as bytes are available in stream, collect data until all bytes in file are collected
                    if (inputStream.available() > 0) {
                        uploadedFileByteData[receivedBytes] = inputStream.readByte();
                        receivedBytes++;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    // Set received bytes to -1, indicating an error
                    receivedBytes = -1;
                }
            }
            if (receivedBytes != -1) {
                try {
                    Files.write(uploadedFile.toPath(), uploadedFileByteData);
                } catch (IOException e) {
                    System.err.println("ERROR: An error occurred when writing downloaded file");
                    e.printStackTrace();
                }
                System.out.println("INFO: File downloaded from server");
            } else {
                System.err.println("ERROR: An error occurred when downloading file");
            }
        });
        downloadThread.start();
        // NOTE: Ack isn't required here. If error occurs, client will safely alert user of failure
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
