package parsers;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is mostly a wrapper around a map, to keep track of our accidentals in a measure. The {@link MeasureParser MeasureParser} object
 * uses this one to parse out the notes.
 */
public class AccidentalTracker {
    /**
     * This map keeps track of the pitches and their respective accidentals. If they do not have an accidental, then there is no entry for that note in the map.
     */
    Map<String, String> accidentalMap;

    public AccidentalTracker() {
        this.accidentalMap = new HashMap<>();
    }

    public String getCurrentAccidentalOfPitch(String pitch) {
        return accidentalMap.get(pitch);
    }

    public void modifyAccidentalMap(String pitch, String accidental) {
        accidentalMap.put(pitch, accidental);
    }

    public void clear() {
        accidentalMap.clear();
    }
}
