package serverCode.Handlers;

import com.sun.net.httpserver.HttpExchange;
import serverCode.Responses.ResCreateMusicIndex;
import serverCode.Services.CreateMusicIndex;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * This class handles the /createMusicIndex endpoint
 */
public class HanCreateMusicIndex extends BASE_HANDLER {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!checkMethodIs("POST", exchange)) return;

        CreateMusicIndex service = new CreateMusicIndex();
        ResCreateMusicIndex response = service.create();

        if (response.isSuccess()) {
            sendResponse(response, exchange, HTTP_OK);
        } else {
            sendResponse(response, exchange, HTTP_INTERNAL_ERROR);
        }
    }
}
