package com.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        Socket mainSocket = null;
        DataInputStream inputStream = null;
        DataOutputStream outputStream = null;

        try {
            mainSocket = new Socket("127.0.0.1", 80);
            System.out.println("Client connected");

            inputStream = new DataInputStream(System.in);
            outputStream = new DataOutputStream(mainSocket.getOutputStream());
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
