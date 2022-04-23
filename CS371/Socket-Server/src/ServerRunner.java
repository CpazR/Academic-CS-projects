import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerRunner {
    private static final BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
    private static final AtomicBoolean isCurrentlyBusy = new AtomicBoolean(false);
    private static final AtomicBoolean serverActive = new AtomicBoolean(true);
    private static Server newServer = null;

    private final static int SERVER_PORT = 4444;

    public static void main(String[] args) throws IOException {

        // Establish server on port {#serverPort}
        /**
         * Persistent server socket that each connection is based on
         */
        var inetAddr = InetAddress.getByName("0.0.0.0");
        var serverSocket = new ServerSocket(SERVER_PORT, 50, inetAddr);
        System.out.println("Server started");
        var serverActive = new AtomicBoolean(true);
        var serverHostList = new ArrayList<Server>();
        // Determine is whole server is busy with an operation
        var maintenanceInputThread = new Thread(() -> {
            while (serverActive.get()) {
                try {
                    var userInput = inputReader.readLine();
                    if (userInput.equals("quit") || userInput.equals("close") || userInput.equals("stop")) {
                        // Close active server instances
                        serverHostList.forEach(Server::closeServer);
                        // Close main server instance
                        serverSocket.close();
                        serverActive.set(false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        var serverLifetimeThread = new Thread(() -> {
            while (serverActive.get()) {
                serverHostList.forEach(server -> {
                    if (!server.isConnected()) {
                        serverHostList.remove(server);
                    }
                });
            }
        });
        maintenanceInputThread.start();
        serverLifetimeThread.start();

        while (serverActive.get()) {
            // Block main thread curing connection. Next iteration create a new server for another client to attempt to connect.
            newServer = new Server(serverSocket);
            newServer.init();
            if (newServer.isConnected()) {
                serverHostList.add(newServer);
            }
        }
    }

    public static void setIsCurrentlyBusy(boolean isCurrentlyBusy) {
        ServerRunner.isCurrentlyBusy.set(isCurrentlyBusy);
    }

    public static boolean isBusy() {
        return ServerRunner.isCurrentlyBusy.get();
    }

    public static void forceCloseServer() {
        serverActive.set(false);
    }
}