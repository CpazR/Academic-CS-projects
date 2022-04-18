import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerRunner {
    private static BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
    private static boolean isBusy;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        AtomicBoolean serverActive = new AtomicBoolean(true);
        var serverHostList = new ArrayList<Server>();
        // Determine is whole server is busy with an operation
        AtomicBoolean isBusy = new AtomicBoolean(false);
        var maintenanceInputThread = new Thread(() -> {
            while (serverActive.get()) {
                try {
                    var userInput = inputReader.readLine();
                    if (userInput.equals("quit") || userInput.equals("close") || userInput.equals("stop")) {
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
                if (serverHostList.isEmpty()) {
                    serverActive.set(false);
                    try {
                        inputReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        maintenanceInputThread.start();
        serverLifetimeThread.start();

        //        while (serverActive.get()) {
        // Block main thread curing connection. Next iteration create a new server for another client to attempt to connect.
        var newServer = new Server();

        //            if (newServer.isConnected()) {
        //                serverHostList.add(newServer);
        //            }
        //        }

    }

    public static void setIsBusy(boolean isBusy) {
        ServerRunner.isBusy = isBusy;
    }

    public static boolean isBusy() {
        return ServerRunner.isBusy;
    }
}