package serverCode.Handlers;

import com.sun.net.httpserver.HttpExchange;
import serverCode.Requests.ReqSearchMusic;
import serverCode.Responses.ResSearchMusic;
import serverCode.Services.SearchMusic;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * This class handles the /searchMusic endpoint
 */
public class HanSearchMusic extends BASE_HANDLER {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!checkMethodIs("POST", exchange)) return;

        ReqSearchMusic request = getRequest(exchange, ReqSearchMusic.class);
        SearchMusic service = new SearchMusic();
        ResSearchMusic response = service.search(request);

        if (response.isSuccess()) {
            sendResponse(response, exchange, HTTP_OK);
        } else {
            sendResponse(response, exchange, HTTP_INTERNAL_ERROR);
        }
    }
}
