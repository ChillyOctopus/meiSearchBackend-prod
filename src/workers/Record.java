package workers;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Represents a musical record to be indexed. Fields are automatically mapped
 * and flattened for Elasticsearch. Includes both textual and array representations
 * of interval and measure mappings.
 * <p>
 * The names of these fields correspond DIRECTLY
 * to method processes of {@link ElasticProcessor}.
 * </p>
 */
public class Record {

    private String name;
    /**
     * Contains one less than the number of notes, as this is the half-step distance between notes e.g. if
     * the first four notes are C, C, D, C#, this string starts with the three intervals "0 2 -1 ..."
     */
    private String intervals_text;
    /**
     * It is a one-to-one mapping of note number -> measure number it belongs to e.g.
     * if there are exactly four notes in the first measure, this string begins with "0 0 0 0 1 ..."
     */
    private String measure_map;
    /**
     * Contains one less than the number of notes, as this is the half-step distance between notes e.g. if
     * the first four notes are C, C, D, C#, this array starts with the three intervals [0, 2, -1, ...]
     */
    private int[] intervals_as_array;
    /**
     * It is a one-to-one mapping of note number -> measure number it belongs to e.g.
     * if there are exactly four notes in the first measure, this array begins with [0, 0, 0, 0, 1, ...]
     */
    private int[] measure_map_as_array;
    private String file_id;
    private HashMap<String, String> mei_metadata;

    /**
     * Constructs a Record with both array and textual interval and measure mappings.
     *
     * @param name                 The name of the file.
     * @param intervals_as_array   The intervals as an array of integers.
     * @param measure_map_as_array The measure map as an array of integers.
     * @param file_id              The file's unique identifier.
     * @param mei_metadata         A map of MEI metadata key-value pairs.
     */
    public Record(String name, int[] intervals_as_array, int[] measure_map_as_array, String file_id, HashMap<String, String> mei_metadata) {
        this.name = name;
        this.intervals_as_array = intervals_as_array;
        this.measure_map_as_array = measure_map_as_array;
        this.intervals_text = convertIntArrayToString(intervals_as_array);
        this.measure_map = convertIntArrayToString(measure_map_as_array);
        this.file_id = file_id;
        this.mei_metadata = mei_metadata;
    }

    /**
     * Default constructor.
     */
    public Record() {
    }

    /**
     * Converts an array of integers into a space-separated string with no trailing space.
     *
     * @param array The array to convert.
     * @return A space-separated string of integers.
     */
    public static String convertIntArrayToString(int[] array) {
        if (array == null || array.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int value : array) {
            sb.append(value).append(" ");
        }
        sb.setLength(sb.length() - 1); // Remove trailing space
        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntervals_text() {
        return intervals_text;
    }

    public void setIntervals_text(String intervals_text) {
        this.intervals_text = intervals_text;
    }

    public String getMeasure_map() {
        return measure_map;
    }

    public void setMeasure_map(String measure_map) {
        this.measure_map = measure_map;
    }

    public int[] getIntervals_as_array() {
        return intervals_as_array;
    }

    public void setIntervals_as_array(int[] intervals_as_array) {
        this.intervals_as_array = intervals_as_array;
    }

    public int[] getMeasure_map_as_array() {
        return measure_map_as_array;
    }

    public void setMeasure_map_as_array(int[] measure_map_as_array) {
        this.measure_map_as_array = measure_map_as_array;
    }

    public String getFile_id() {
        return file_id;
    }

    public void setFile_id(String file_id) {
        this.file_id = file_id;
    }

    public HashMap<String, String> getMei_metadata() {
        return mei_metadata;
    }

    public void setMei_metadata(HashMap<String, String> mei_metadata) {
        this.mei_metadata = mei_metadata;
    }

    @Override
    public String toString() {
        return "Record{" +
                "name='" + name + '\'' +
                ", intervals_text='" + intervals_text + '\'' +
                ", measure_map='" + measure_map + '\'' +
                ", intervals_as_array=" + Arrays.toString(intervals_as_array) +
                ", measure_map_as_array=" + Arrays.toString(measure_map_as_array) +
                ", file_id='" + file_id + '\'' +
                ", mei_metadata=" + mei_metadata +
                '}';
    }
}
