package com.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final int serverPort = 4444;
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private DataInputStream inputStream = null;
    private DataOutputStream outputStream = null;
    private boolean isConnected = false;

    private String baseDirectory = "./sharedFiles/";

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
            outputStream = new DataOutputStream(System.out);
            isConnected = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeServer() {
        try {
            System.out.println("Closing...");

            socket.close();
            inputStream.close();
            isConnected = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String waitForInput() {
        var clientInput = "";
        try {
            clientInput = inputStream.readUTF();
        } catch (IOException e) {
            System.err.println("ERROR WHILE RECEIVING INPUT: " + e.getMessage());
        }
        return clientInput;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void uploadFile(String fileName) {
    }

    public void downloadFile(String fileName) {
    }

    public void showFolderContents() {
    }

    public void deleteFile(String fileName) {
    }

    public void closeClient() {
    }
}
