package org.uem.dam.dam2chat_psp_backend;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientThreadConnection extends Thread {

    private Socket socket;
    private DataInputStream dataStream;

    public ClientThreadConnection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            System.out.println("Socket: Grabbed connection, starting stream read!");
            dataStream = new DataInputStream(socket.getInputStream());
            System.out.println(dataStream.readUTF());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
