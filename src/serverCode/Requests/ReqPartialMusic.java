package serverCode.Requests;

import workers.Record;

import java.util.List;
import java.util.Map;

/**
 * This class is the request object for the /partialSheetMusic endpoint
 */
public class ReqPartialMusic {
    Record source;
    Map<String, List<String>> highlight;

    public ReqPartialMusic(Record source, Map<String, List<String>> highlight) {
        this.source = source;
        this.highlight = highlight;
    }

    public Record getSource() {
        return source;
    }

    public void setSource(Record source) {
        this.source = source;
    }

    public Map<String, List<String>> getHighlight() {
        return highlight;
    }

    public void setHighlight(Map<String, List<String>> highlight) {
        this.highlight = highlight;
    }

    @Override
    public String toString() {
        return "ReqPartialMusic{" +
                "source=" + source +
                ", highlight=" + highlight +
                '}';
    }
}
