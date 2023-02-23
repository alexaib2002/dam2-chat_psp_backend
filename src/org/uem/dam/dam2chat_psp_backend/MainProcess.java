package org.uem.dam.dam2chat_psp_backend;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MainProcess {
    public static final int port = 2453;
    private final static StringBuffer chatHistoryBuffer = new StringBuffer();
    private static ServerSocket serverSocket = null;

    public static void main(String[] args) {
        try {
            startServer();
            while (true) listenClient();
        } finally {
            closeServer();
        }
    }

    public static synchronized void appendHistory(String msg) {
        System.out.println(String.format("[Buffer] %s", msg));
        chatHistoryBuffer.append(msg + "\n");
    }

    public static synchronized String retrieveHistory() {
        return chatHistoryBuffer.toString();
    }

    private static void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server up and running");
        } catch (IOException e) {
            System.err.println("Couldn't create socket on port 2453");
        }
    }

    private static void closeServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed())
                serverSocket.close();
        } catch (IOException e) {
            System.err.println("I/O error while closing the server!");
        }
    }

    private static void listenClient() {
        try {
            System.out.println("Yielding for connection");
            Socket client = serverSocket.accept();
            System.out.println("Connection accepted, releasing a new thread for this connection");
            new ClientThreadConnection(client).start();
        } catch (IOException e) {
            System.err.println("Client got disconnected");
            e.printStackTrace();
        }
    }
}