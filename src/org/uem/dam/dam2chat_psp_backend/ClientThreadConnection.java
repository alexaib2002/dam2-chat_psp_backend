package org.uem.dam.dam2chat_psp_backend;

import com.sun.tools.javac.Main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

public class ClientThreadConnection extends Thread {
    public static final String NICK_PREFIX = "[nick]";
    public static final String PRIVATE_PREFIX = "private:";

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
                } else if (msg.contains(PRIVATE_PREFIX)) {
                    try {
                        String decMsg = msg.replace(nick + ": ", "");
                        String to = decMsg.substring(decMsg.indexOf(PRIVATE_PREFIX) + PRIVATE_PREFIX.length(),
                                decMsg.indexOf(" "));
                        System.out.printf("Forwarding private message to: %s\n", to);
                        MainProcess.forwardMsg(to, msg.replace(PRIVATE_PREFIX + nick, ""));
                    } catch (IndexOutOfBoundsException e) {
                        System.err.println("Invalid private message format, is the client updated?");
                    }
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

    public String getNick() {
        return nick;
    }

    public synchronized void writeMsgSocket(String msg) {
        try {
            outputStream.writeUTF(msg);
        } catch (IOException e) {
            System.err.println("I/O error while writing into the output socket");
        }
    }
}
