package com.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Socket mainSocket = null;
        DataInputStream inputStream = null;
        DataOutputStream outputStream = null;
        int clientPort = 4444;

        try {
            mainSocket = new Socket("127.0.0.1", clientPort);
            System.out.println("Client connected");

            inputStream = new DataInputStream(System.in);
            outputStream = new DataOutputStream(mainSocket.getOutputStream());
            Scanner inputScanner = new Scanner(System.in);
            inputScanner.useDelimiter("\\n");
            String localUserInput = inputScanner.nextLine();

            while (!localUserInput.equals("close")) {
                try {
                    outputStream.writeUTF(localUserInput);
                    System.out.println("Sent: " + localUserInput + " to server");
                } catch (UnknownHostException u) {
                    u.printStackTrace();
                }

                // Get new input
                localUserInput = inputScanner.nextLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mainSocket.close();
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
