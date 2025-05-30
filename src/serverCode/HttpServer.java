package serverCode;

import serverCode.Handlers.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import static java.lang.Integer.parseInt;

/**
 * This class is the server for all the backend. See 'run' and other methods for functionality and endpoints.
 */
public class HttpServer {

    private final static int MAX_CONNECTIONS = 12;
    private com.sun.net.httpserver.HttpServer Server;

    /**
     * Main method and entry point for the server. Expects a port number as the first argument.
     * If no argument is provided, prints usage instructions and exits.
     *
     * @param args command-line arguments, where {@code args[0]} is expected to be the port number
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java HttpServer <port number>");
            System.exit(1);
        }

        String portNumber = args[0];
        try {
            new HttpServer().run(portNumber);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            System.out.println("Couldn't start server.");
        }
    }

    /**
     * Starts and configures the HTTP server on the given port. Initializes request contexts
     * for all endpoints including {@code /searchMusic}, {@code /addMusic}, {@code /removeMusic},
     * {@code /createMusicIndex}, and {@code /partialSheetMusic}, as well as a {@code /ping} route
     * for basic health checks.
     *
     * @param portNumber the port number on which to start the server
     * @throws IOException if an I/O error occurs while starting the server
     * @see HanSearchMusic
     * @see HanAddMusic
     * @see HanRemoveMusic
     * @see HanCreateMusicIndex
     * @see HanPartialSheetMusic
     */
    private void run(String portNumber) throws IOException {
        System.out.println("******************************************************");
        System.out.println("Creating server on port " + portNumber + "...");

        try {
            Server = com.sun.net.httpserver.HttpServer.create(
                    new InetSocketAddress("0.0.0.0", parseInt(portNumber)), MAX_CONNECTIONS);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Server.setExecutor(null);

        System.out.println("Server created. Initializing contexts...");

        initializeContexts();

        Server.start();

        System.out.println("Success. Server started and waiting for connection.");
        System.out.println("******************************************************");
    }

    /**
     * Initializes all server request contexts and handlers.
     * This includes endpoints for music search, index creation, and partial sheet retrieval.
     */
    private void initializeContexts() {
        Server.createContext("/searchMusic", new HanSearchMusic());
        Server.createContext("/addMusic", new HanAddMusic());
        Server.createContext("/removeMusic", new HanRemoveMusic());
        Server.createContext("/createMusicIndex", new HanCreateMusicIndex());
        Server.createContext("/partialSheetMusic", new HanPartialSheetMusic());

        Server.createContext("/ping", exchange -> {
            String response = "pong\n";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
    }
}
