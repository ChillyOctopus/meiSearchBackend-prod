package serverCode.Handlers;

import com.sun.net.httpserver.HttpExchange;
import serverCode.Requests.ReqAddMusic;
import serverCode.Responses.ResAddMusic;
import serverCode.Services.AddMusic;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * This class handles the /addMusic endpoint
 */
public class HanAddMusic extends BASE_HANDLER {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!checkMethodIs("POST", exchange)) return;

        ReqAddMusic request = getRequest(exchange, ReqAddMusic.class);
        AddMusic service = new AddMusic();
        ResAddMusic response = service.add(request);

        if (response.isSuccess()) {
            sendResponse(response, exchange, HTTP_OK);
        } else {
            sendResponse(response, exchange, HTTP_INTERNAL_ERROR);
        }
    }
}
