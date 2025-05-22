package parsers;

import java.util.List;
/**
 * This class is what parsers usually return when asked for a list. It contains the data parsed,
 * if any, and some metadata for further usage in parsing.
 */
public class ParsedListData {
    private List<String> data;
    private int startIndex;
    private int endIndex;
    private boolean found;

    public ParsedListData(List<String> data, int startIndex, int endIndex, boolean found) {
        this.data = data;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.found = found;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    @Override
    public String toString() {
        return "ParsedData{" +
                "data='" + data + '\'' +
                ", startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                ", found=" + found +
                '}';
    }

}
