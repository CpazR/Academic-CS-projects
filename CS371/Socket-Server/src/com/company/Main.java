package com.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        final int serverPort = 80;
        Socket socket = null;
        ServerSocket serverSocket = null;
        DataInputStream inputStream = null;
        DataOutputStream outputStream = null;

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

            String inputFromClient = "";

            while (!inputFromClient.equals("close")) {
                try {
                    inputFromClient = inputStream.readUTF();
                    System.out.println(inputFromClient);
                } catch (IOException i) {
                    i.printStackTrace();
                }
            }

            // Close server
            socket.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
