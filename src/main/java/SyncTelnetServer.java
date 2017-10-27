import static java.lang.System.lineSeparator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SyncTelnetServer {
    public static final Logger L = Logger.getLogger(SyncTelnetServer.class.getName());
    public static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(12667);
        L.info("Server running and listening on port: " + (12667));

        while (true) {
            Socket newClient = server.accept();
            executor.execute(() -> {
                try (Socket cli = newClient;
                        BufferedReader in = new BufferedReader(new InputStreamReader(cli.getInputStream()));
                        PrintWriter out = new PrintWriter(cli.getOutputStream(), true)
                ) {
                    out.println("Connected! (status, ping, exit)" + lineSeparator());
                    while (true) {
                        String command = in.readLine();
                        if (command == null) continue;
                        if (command.equalsIgnoreCase("exit")) {
                            out.println("Goodbye…"); break;
                        }

                        //handle the command
                        Supplier<String> handler;
                        if (command.equalsIgnoreCase("status")) handler = () -> "Server running";
                        else if (command.equalsIgnoreCase("ping")) handler = () -> "pong";
                        else handler = () -> "unknown command: " + command;
                        out.println(handler.get());
                    }
                } catch (IOException e) {
                    L.log(Level.SEVERE, "client error: " + e, e);
                }
            });
        }
    }
}
