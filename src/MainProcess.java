import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainProcess {
    public static final int port = 2453;
    private static ServerSocket serverSocket = null;

    public static void main(String[] args) {
        try {
            startServer();
            while (true) {
                listenClient();
            }
        } finally {
            closeServer();
        }
    }

    private static void startServer() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Couldn't create socket on port 2453");
        }
    }

    private static void closeServer() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("I/O error while closing the server!");
        }
    }

    private static void listenClient() {
        Socket client = null;
        DataInputStream clientStream = null;
        try {
            System.out.println("Yielding until connection made");
            client = serverSocket.accept();
            clientStream = new DataInputStream(client.getInputStream());
            System.out.println("Reached response!");
            System.out.println(clientStream.readUTF());
        } catch (IOException e) {
            System.err.println("IO error while listening to client!");
            e.printStackTrace();
        }
    }
}