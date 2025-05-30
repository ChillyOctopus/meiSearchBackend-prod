package serverCode.Handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import serverCode.Responses.BASE_RESPONSE;

import java.io.*;
import java.util.Arrays;

import static java.net.HttpURLConnection.HTTP_BAD_METHOD;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * Abstract base handler for all HTTP request handlers.
 * Provides utility methods for parsing requests, validating HTTP methods and endpoints,
 * sending JSON responses, and authenticating requests.
 */
public abstract class BASE_HANDLER implements HttpHandler {

    protected static final String CHAR_SET = "UTF-8";

    protected String reqBody;
    protected Gson gson = new GsonBuilder().serializeNulls().create();
    protected InputStream is;
    protected OutputStream os;

    /**
     * Writes a string to an output stream.
     *
     * @param str The string to write.
     * @param os  The output stream.
     * @throws IOException if writing fails.
     */
    public static void writeString(String str, OutputStream os) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(os, CHAR_SET);
        writer.write(str);
        writer.flush();
    }

    /**
     * Parses the request body into an object of the given type.
     *
     * @param exchange The HTTP exchange object.
     * @param type     The class type to parse into.
     * @param <T>      The generic type.
     * @return Parsed object of the specified type.
     * @throws IOException if reading from the input stream fails.
     */
    public <T> T getRequest(HttpExchange exchange, Class<T> type) throws IOException {
        is = exchange.getRequestBody();
        reqBody = readString(is);
        return gson.fromJson(reqBody, type);
    }

    /**
     * Sends a JSON response to the client.
     *
     * @param response The response object to send.
     * @param exchange The HTTP exchange object.
     * @param httpCode The HTTP status code.
     * @throws IOException if writing to the output stream fails.
     */
    public void sendResponse(BASE_RESPONSE response, HttpExchange exchange, int httpCode) throws IOException {
        String responseString = gson.toJson(response);
        byte[] encodedResponse = responseString.getBytes(CHAR_SET);
        exchange.sendResponseHeaders(httpCode, encodedResponse.length);
        os = exchange.getResponseBody();
        os.write(encodedResponse);
        os.close();
    }

    /**
     * Verifies the request uses the expected HTTP method.
     *
     * @param expectedMethod The expected method (e.g., "POST").
     * @param exchange       The HTTP exchange object.
     * @return true if the method matches, false otherwise.
     * @throws IOException if sending a response fails.
     */
    public boolean checkMethodIs(String expectedMethod, HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase(expectedMethod)) {
            BASE_RESPONSE response = new BASE_RESPONSE("Incorrect HTTP method.", false);
            sendResponse(response, exchange, HTTP_BAD_METHOD);
            return false;
        }
        return true;
    }

    /**
     * Verifies the URL has the expected number of path components.
     *
     * @param expectedLength The expected number of components.
     * @param exchange       The HTTP exchange object.
     * @return true if the length matches, false otherwise.
     * @throws IOException if sending a response fails.
     */
    public boolean checkUrlLength(int expectedLength, HttpExchange exchange) throws IOException {
        int actualLength = urlParse(exchange).length;
        if (actualLength != expectedLength) {
            String message = String.format(
                    "Wrong endpoint construction, found length of %d in url \"%s\".",
                    actualLength,
                    exchange.getRequestURI().getPath()
            );
            BASE_RESPONSE response = new BASE_RESPONSE(message, false);
            sendResponse(response, exchange, HTTP_BAD_REQUEST);
            return false;
        }
        return true;
    }

    /**
     * Parses the request URI into its path components, excluding the leading slash.
     *
     * @param exchange The HTTP exchange object.
     * @return An array of path components.
     */
    public String[] urlParse(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        return Arrays.copyOfRange(parts, 1, parts.length); // Skip leading empty string
    }

    /**
     * Reads the entire contents of an input stream as a string.
     *
     * @param is The input stream.
     * @return The string content of the stream.
     * @throws IOException if reading fails.
     */
    public String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(is, CHAR_SET);
        char[] buffer = new char[1024];
        int length;
        while ((length = reader.read(buffer)) > 0) {
            sb.append(buffer, 0, length);
        }
        return sb.toString();
    }

    /**
     * Validates the Authorization header and returns the user ID if valid.
     *
     * @param exchange The HTTP exchange object.
     * @return The user ID as an Integer, or null if invalid or missing.
     * @throws IOException if sending a response fails.
     */
    public Integer authChecksOut(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getRequestHeaders();
        if (headers.containsKey("Authorization")) {
            return Integer.parseInt(headers.getFirst("Authorization"));
        } else {
            sendBadAuthtokenResponse(exchange);
            return null;
        }
    }

    /**
     * Sends a standardized response for bad or missing Authorization tokens.
     *
     * @param exchange The HTTP exchange object.
     * @throws IOException if sending a response fails.
     */
    private void sendBadAuthtokenResponse(HttpExchange exchange) throws IOException {
        BASE_RESPONSE response = new BASE_RESPONSE("Bad Authtoken.", false);
        sendResponse(response, exchange, HTTP_BAD_REQUEST);
    }
}
