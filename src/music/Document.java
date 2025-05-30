package music;

import exceptions.Empty;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents a whole mei piece of music. It has a list of {@link Measure Measures} objects, and can output the interval of
 * the notes in the document. Also contains metadata
 */
public class Document {
    private final List<Measure> measures;
    private final HashMap<String, String> metadata;
    private int[] intervalRep;

    public Document(List<Measure> measures, HashMap<String, String> metadataFromInFile) {
        this.measures = measures;
        this.metadata = metadataFromInFile;
        getIntervalsFromMeasures();
    }

    /**
     * Outputs the respective interval for this 'document', or piece of music.
     *
     * @param outfile the file we are outputting the interval to
     * @throws IOException when there is an issue with the files
     */
    public void outputInterval(File outfile) throws IOException {
        try (FileWriter fileWriter = new FileWriter(outfile)) {
            fileWriter.write(Arrays.toString(intervalRep));
        }
    }

    /**
     * Computes and populates the {@code intervalRep} array with interval values derived from adjacent pairs
     * of {@link Measure} objects in the {@code measures} list. Each measure computes its interval
     * relative to the next one in sequence, or {@code null} if it is the last measure.
     *
     * @see Measure#getMeasureInterval(Measure)
     */
    public void getIntervalsFromMeasures() {
        List<Integer> intervalList = new ArrayList<>();

        for (int i = 0; i < measures.size(); i++) {
            try {
                Measure current = measures.get(i);
                Measure next = (i < measures.size() - 1) ? measures.get(i + 1) : null;
                int[] intervals = current.getMeasureInterval(next);

                for (int interval : intervals) {
                    intervalList.add(interval);
                }
            } catch (Empty ex) {
                continue;
            }
        }

        intervalRep = new int[intervalList.size()];
        for (int i = 0; i < intervalList.size(); i++) {
            intervalRep[i] = intervalList.get(i);
        }
    }

    /**
     * Generates a mapping from each musical note or chord to the index of its containing measure.
     * Only objects of type {@link Note} or {@link Chord} are considered.
     *
     * @return an array of measure indices corresponding to the order of {@link Note} and {@link Chord} instances in {@code measures}
     * @see Note
     * @see Chord
     */
    public int[] getMeasureMap() {
        List<Integer> measureIndices = new ArrayList<>();

        for (int i = 0; i < measures.size(); i++) {
            List<Object> noteObjects = measures.get(i).getNoteRep();
            for (Object obj : noteObjects) {
                if (obj.getClass() == Note.class || obj.getClass() == Chord.class) {
                    measureIndices.add(i);
                }
            }
        }

        int[] map = new int[measureIndices.size()];
        for (int i = 0; i < measureIndices.size(); i++) {
            map[i] = measureIndices.get(i);
        }

        return map;
    }

    public int[] getIntervalRep() {
        return intervalRep;
    }

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public List<Measure> getMeasures() {
        return measures;
    }

    public int totalMeasures() {
        return measures.size();
    }

    public Measure getMeasureAtPos(int pos) {
        return measures.get(pos);
    }

    @Override
    public String toString() {
        return "Document{" +
                "measures=" + measures +
                ", intervalRep=" + Arrays.toString(intervalRep) +
                ", metadata=" + metadata +
                '}';
    }
}
