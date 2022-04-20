import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerRunner {
    private static final BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
    private static final AtomicBoolean isCurrentlyBusy = new AtomicBoolean(false);
    private final static int SERVER_PORT = 4444;

    public static void main(String[] args) throws IOException {
        // Establish server on port {#serverPort}
        /**
         * Persistent server socket that each connection is based on
         */
        var serverSocket = new ServerSocket(SERVER_PORT);
        System.out.println("Server started");
        var serverActive = new AtomicBoolean(true);
        var serverHostList = new ArrayList<Server>();
        // Determine is whole server is busy with an operation
        var maintenanceInputThread = new Thread(() -> {
            while (serverActive.get()) {
                try {
                    var userInput = inputReader.readLine();
                    if (userInput.equals("quit") || userInput.equals("close") || userInput.equals("stop")) {
                        serverHostList.forEach(Server::closeServer);
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
//                if (serverHostList.isEmpty()) {
//                    serverActive.set(false);
//                    try {
//                        inputReader.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        });
        maintenanceInputThread.start();
        serverLifetimeThread.start();

        while (serverActive.get()) {
            // Block main thread curing connection. Next iteration create a new server for another client to attempt to connect.
            var newServer = new Server(serverSocket);
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
}