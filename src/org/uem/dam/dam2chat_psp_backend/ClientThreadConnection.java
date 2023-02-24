package org.uem.dam.dam2chat_psp_backend;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

public class ClientThreadConnection extends Thread {
    public static final String NICK_PREFIX = "[nick]";

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private String nick;
    private Consumer<ClientThreadConnection> connectionDeleter;


    public ClientThreadConnection(Socket socket, Consumer<ClientThreadConnection> connectionDeleter) {
        this.socket = socket;
        this.connectionDeleter = connectionDeleter;
    }

    @Override
    public void run() {
        try {
            synchronized (MainProcess.class) {
                System.out.println("Socket: Grabbed connection, starting stream read!");
                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());
                writeMsgSocket(MainProcess.retrieveHistory());
            }
        } catch (IOException e) {
            System.err.println("Socket's data stream couldn't be acquired");
            throw new RuntimeException(e);
        }
        try {
            while (true) {
                String msg = inputStream.readUTF();
                if (msg.startsWith(NICK_PREFIX)) {
                    nick = msg.replace(NICK_PREFIX, "");
                    System.out.printf("Received client header nick %s%n", nick);
                    MainProcess.appendHistory(String.format("%s enters the chat. Welcome!", nick));
                    continue;
                }
                MainProcess.appendHistory(msg);
            }
        } catch (IOException e) {
            synchronized (MainProcess.class) {
                System.out.println("Removing client from server list");
                connectionDeleter.accept(this);
            }
            System.out.println("Client disconnected. Goodbye!");
            MainProcess.appendHistory(String.format("%s leaves the chat. Bye!", nick));
        }
    }

    public synchronized void writeMsgSocket(String msg) {
        try {
            outputStream.writeUTF(msg);
        } catch (IOException e) {
            System.err.println("I/O error while writing into the output socket");
        }
    }
}
