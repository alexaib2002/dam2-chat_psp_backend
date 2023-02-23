package org.uem.dam.dam2chat_psp_backend;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientThreadConnection extends Thread {
    public static final String NICK_PREFIX = "[nick]";

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private String nick;


    public ClientThreadConnection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            synchronized (MainProcess.class) {
                System.out.println("Socket: Grabbed connection, starting stream read!");
                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());
                outputStream.writeUTF(MainProcess.retrieveHistory());
            }
        } catch (IOException e) {
            System.err.println("Socket's data stream couldn't be acquired");
            throw new RuntimeException(e);
        }
        try {
            while (true) {
                // TODO Redirect flow into every client chat history
                String msg = inputStream.readUTF();
                if (msg.startsWith(NICK_PREFIX)) {
                    nick = msg.replace(NICK_PREFIX, "");
                    System.out.println(String.format("Received client header nick %s", nick));
                    MainProcess.appendHistory(String.format("%s enters the chat. Welcome!", nick));
                    continue;
                }
                MainProcess.appendHistory(msg);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected. Goodbye!");
            MainProcess.appendHistory(String.format("%s leaves the chat. Bye!", nick));
        }
    }
}
