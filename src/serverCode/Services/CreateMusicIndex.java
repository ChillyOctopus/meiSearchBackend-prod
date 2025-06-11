package serverCode.Services;

import serverCode.Responses.ResCreateMusicIndex;
import workers.ElasticProcessor;

/**
 * This class is essentially a wrapper around the method call 'createMusicIndex' in {@link ElasticProcessor}
 */
public class CreateMusicIndex extends BASE_SERVICE {
    public ResCreateMusicIndex create() {
        try {
            ElasticProcessor elasticProcessor = new ElasticProcessor();
            elasticProcessor.createMusicIndex();

            return new ResCreateMusicIndex(null, true);
        } catch (Exception e) {
            return new ResCreateMusicIndex(e.toString(), false);
        }
    }
}
