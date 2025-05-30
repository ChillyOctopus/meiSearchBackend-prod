package serverCode.Responses;

import co.elastic.clients.elasticsearch.core.search.Hit;
import workers.Record;


/**
 * This class is the response object for the /searchMusic endpoint
 */
public class ResSearchMusic extends BASE_RESPONSE {
    public Hit<Record>[] hits;

    public ResSearchMusic(Hit<Record>[] hits) {
        super(null, true);
        this.hits = hits;
    }

    public ResSearchMusic(String message, boolean success) {
        super(message, success);
        this.hits = null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ResSearchMusic:");
        sb.append("\nSuccess=").append(success);
        sb.append("\nMessage=").append(message);
        sb.append("\nHits=");
        for (Hit<Record> hit : hits) {
            sb.append("\n\tSource: ").append(hit.source());
            sb.append("\n\tFields: ").append(hit.fields());
            sb.append("\n\tScore: ").append(hit.score());
            sb.append("\n\tHighlight: ").append(hit.highlight());
            sb.append("\n\tIndex: ").append(hit.index());
            sb.append("\n\tID: ").append(hit.id());
            sb.append("\n");
        }
        return sb.toString();
    }
}