package com.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        final int serverPort = 4444;
        Socket socket = null;
        ServerSocket serverSocket = null;
        DataInputStream inputStream = null;
        DataOutputStream outputStream = null;

        boolean lookForClients = true;

        while (lookForClients) {
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
                        System.out.println("Waiting for client input...");
                        inputFromClient = inputStream.readUTF();
                        performAction(inputFromClient);
                        System.out.println(inputFromClient);
                    } catch (Exception i) {
                        i.printStackTrace();
                    }
                }
                System.out.println("Closing...");

                // Close server
                socket.close();
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void performAction(String inputFromClient) {
        var directory = "./files/";
        switch (inputFromClient) {
            case "CREATE":
                // Create a path local to the server
                var file = new File(directory + "somefile.txt");
                if (file.isDirectory()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (file.mkdir()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }
}
