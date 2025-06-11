package serverCode.Requests;

import java.util.List;
import java.util.Map;

/**
 * This class is the request object for the /searchMusic endpoint
 */
public class ReqSearchMusic {
    String meiChunk;
    Map<String, List<String>> andMap;
    Map<String, List<String>> orMap;
    Map<String, List<String>> notMap;

    public ReqSearchMusic() {
    }

    public ReqSearchMusic(String meiChunk, Map<String, List<String>> andMap, Map<String, List<String>> orMap, Map<String, List<String>> notMap) {
        this.meiChunk = meiChunk;
        this.andMap = andMap;
        this.orMap = orMap;
        this.notMap = notMap;
    }

    public String getMeiChunk() {
        return meiChunk;
    }

    public void setMeiChunk(String meiChunk) {
        this.meiChunk = meiChunk;
    }

    public Map<String, List<String>> getAndMap() {
        return andMap;
    }

    public void setAndMap(Map<String, List<String>> andMap) {
        this.andMap = andMap;
    }

    public Map<String, List<String>> getOrMap() {
        return orMap;
    }

    public void setOrMap(Map<String, List<String>> orMap) {
        this.orMap = orMap;
    }

    public Map<String, List<String>> getNotMap() {
        return notMap;
    }

    public void setNotMap(Map<String, List<String>> notMap) {
        this.notMap = notMap;
    }

    @Override
    public String toString() {
        return "ReqSearchMusic{" +
                "meiChunk='" + meiChunk + '\'' +
                ", andMap=" + andMap +
                ", orMap=" + orMap +
                ", notMap=" + notMap +
                '}';
    }
}