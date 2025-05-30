package serverCode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import serverCode.Requests.ReqAddMusic;
import serverCode.Requests.ReqRemoveMusic;
import serverCode.Requests.ReqSearchMusic;
import serverCode.Responses.ResAddMusic;
import serverCode.Responses.ResRemoveMusic;
import serverCode.Responses.ResSearchMusic;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Acts as a client-side proxy to communicate with an external HTTP server using RESTful endpoints.
 * Supports operations for searching, adding, and removing music records.
 */
public class HttpServerProxy {

    private final String serverHost;
    private final String serverPort;
    private final String charset;
    private final Gson gson;

    /**
     * Constructs a proxy with specified server host, port, and charset.
     *
     * @param serverHost host of the target server
     * @param serverPort port of the target server
     * @param charset    character encoding (e.g., {@code "UTF-8"})
     */
    public HttpServerProxy(String serverHost, String serverPort, String charset) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.charset = charset;
        this.gson = new GsonBuilder().serializeNulls().create();
    }

    /**
     * Constructs a proxy with the given server host and port using UTF-8 as the default charset.
     *
     * @param serverHost host of the target server
     * @param serverPort port of the target server
     */
    public HttpServerProxy(String serverHost, String serverPort) {
        this(serverHost, serverPort, "UTF-8");
    }

    /**
     * Constructs a proxy with default host {@code "app"} and port {@code "5000"}.
     */
    public HttpServerProxy() {
        this("app", "5000");
    }

    /**
     * Writes a string to the provided {@link OutputStream} using {@link OutputStreamWriter}.
     *
     * @param str the string to write
     * @param os  the output stream
     * @throws IOException if an I/O error occurs
     */
    public static void writeString(String str, OutputStream os) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(os);
        writer.write(str);
        writer.flush();
    }

    /**
     * Sends an HTTP request to the server with the specified service configuration and returns the response body.
     *
     * @param service the {@link GenericService} configuration containing the request metadata
     * @return the response body as a String
     * @throws IOException if an I/O error occurs
     */
    private String connectToServer(GenericService service) throws IOException {
        InputStream is = null;
        URL url = new URL("http://" + serverHost + ":" + serverPort + service.path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod(service.method);
            connection.addRequestProperty("Accept", "application/json");
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("Cache-Control", "no-cache");
            connection.addRequestProperty("Connection", "close");

            if (service.authenticator != null) {
                connection.addRequestProperty("Authorization", service.authenticator.toString());
            }

            if (service.reqData != null) {
                connection.setDoOutput(true);
                try (OutputStream reqBody = connection.getOutputStream()) {
                    writeString(service.reqData, reqBody);
                }
            } else {
                connection.setDoOutput(false);
            }

            connection.connect();
            int responseCode = connection.getResponseCode();
            is = (responseCode >= 200 && responseCode < 400)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            return readString(is);

        } finally {
            if (is != null) is.close();
            connection.disconnect();
        }
    }

    /**
     * Sends a search request to the music search endpoint.
     * Calls connectToServer(GenericService)
     *
     * @param request the {@link ReqSearchMusic} object containing the query
     * @return the {@link ResSearchMusic} response, or {@code null} on failure
     */
    public ResSearchMusic searchMusic(ReqSearchMusic request) {
        try {
            String requestBody = gson.toJson(request);
            GenericService service = new GenericService("/searchMusic", "POST", requestBody, null);
            return gson.fromJson(connectToServer(service), ResSearchMusic.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sends a request to add a new music record to the server.
     * Calls connectToServer(GenericService)
     *
     * @param request the {@link ReqAddMusic} object containing the record to add
     * @return the {@link ResAddMusic} response, or {@code null} on failure
     */
    public ResAddMusic addMusic(ReqAddMusic request) {
        try {
            String requestBody = gson.toJson(request);
            GenericService service = new GenericService("/addMusic", "POST", requestBody, null);
            return gson.fromJson(connectToServer(service), ResAddMusic.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sends a request to remove a music record from the server.
     * Calls connectToServer(GenericService)
     *
     * @param request the {@link ReqRemoveMusic} object specifying what to remove
     * @return the {@link ResRemoveMusic} response, or {@code null} on failure
     */
    public ResRemoveMusic removeMusic(ReqRemoveMusic request) {
        try {
            String requestBody = gson.toJson(request);
            GenericService service = new GenericService("/removeMusic", "POST", requestBody, null);
            return gson.fromJson(connectToServer(service), ResRemoveMusic.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Reads the contents of the given {@link InputStream} as a {@link String} using the configured charset.
     *
     * @param is the input stream to read
     * @return the resulting string
     * @throws IOException if an I/O error occurs
     */
    public String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(is, charset);
        char[] buffer = new char[1024];
        int length;

        while ((length = reader.read(buffer)) > 0) {
            sb.append(buffer, 0, length);
        }

        return sb.toString();
    }

    /**
     * Represents a generic HTTP service request used by {@link HttpServerProxy}.
     * Holds endpoint path, HTTP method, request body, and optional authenticator.
     */
    private static class GenericService {
        public final String path;
        public final String method;
        public final String reqData;
        public final Integer authenticator;

        public GenericService(String path, String method, String reqData, Integer authenticator) {
            this.path = path;
            this.method = method;
            this.reqData = reqData;
            this.authenticator = authenticator;
        }
    }
}