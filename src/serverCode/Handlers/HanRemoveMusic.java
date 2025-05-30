package serverCode.Handlers;

import com.sun.net.httpserver.HttpExchange;
import serverCode.Requests.ReqRemoveMusic;
import serverCode.Responses.ResRemoveMusic;
import serverCode.Services.RemoveMusic;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * This class handles the /removeMusic endpoint
 */
public class HanRemoveMusic extends BASE_HANDLER {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!checkMethodIs("POST", exchange)) return;

        ReqRemoveMusic request = getRequest(exchange, ReqRemoveMusic.class);
        RemoveMusic service = new RemoveMusic();
        ResRemoveMusic response = service.remove(request);

        if (response.isSuccess()) {
            sendResponse(response, exchange, HTTP_OK);
        } else {
            sendResponse(response, exchange, HTTP_INTERNAL_ERROR);
        }
    }
}
