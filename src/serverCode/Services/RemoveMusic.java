package serverCode.Services;

import serverCode.Requests.ReqRemoveMusic;
import serverCode.Responses.ResRemoveMusic;
import java.io.IOException;
import workers.*;

/**
 * This class is essentially a wrapper around the method call 'removeRecord' in {@link ElasticProcessor}
 */
public class RemoveMusic extends BASE_SERVICE{
    public ResRemoveMusic remove(ReqRemoveMusic request) throws IOException {
        ElasticProcessor elasticProcessor = new ElasticProcessor();

        return new ResRemoveMusic(null, true, elasticProcessor.removeRecord(request.getName()).toString());
    }
}
