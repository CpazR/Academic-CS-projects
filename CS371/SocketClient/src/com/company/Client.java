package com.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {

    private Socket mainSocket = null;
    private DataInputStream inputStream = null;
    private DataOutputStream outputStream = null;
    private boolean isConnected = false;

    Client() {

    }

    Client(String address, int port) {
        initializeClient(address, port);
    }

    private void sendRequest() {
        // Build bitfield(?) for operations and append arguments
    }

    public void initializeClient(String address, int port) {
        try {
            mainSocket = new Socket(address, port);
            System.out.println("Client connected");

            inputStream = new DataInputStream(System.in);
            outputStream = new DataOutputStream(mainSocket.getOutputStream());
            isConnected = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeClient() {
        try {
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
    }

    public void downloadFile(String fileName) {
    }

    public void showFolderContents() {
    }

    public void deleteFile(String fileName) {
    }
}
