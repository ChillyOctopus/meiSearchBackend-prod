package serverCode.Handlers;

import com.sun.net.httpserver.HttpExchange;
import serverCode.Requests.ReqPartialMusic;
import serverCode.Responses.ResPartialSheetMusic;
import serverCode.Services.PartialSheetMusic;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * This class handles the /partialSheetMusic endpoint
 */
public class HanPartialSheetMusic extends BASE_HANDLER {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!checkMethodIs("POST", exchange)) return;

        ReqPartialMusic request = getRequest(exchange, ReqPartialMusic.class);
        PartialSheetMusic service = new PartialSheetMusic();
        ResPartialSheetMusic response = service.getPartial(request);

        if (response != null && response.isSuccess()) {
            sendResponse(response, exchange, HTTP_OK);
        } else {
            sendResponse(response, exchange, HTTP_INTERNAL_ERROR);
        }
    }
}
