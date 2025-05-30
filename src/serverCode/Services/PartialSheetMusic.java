package serverCode.Services;

import serverCode.Requests.ReqPartialMusic;
import serverCode.Responses.ResPartialSheetMusic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The class responsible for finding the appropriate partial sheet music chunks in highlighted search results and sending them back
 */
public class PartialSheetMusic {

    private static final String JDBC_URL = "jdbc:postgresql://melodysearchmeidatabase.ct8ig60g4q3f.us-east-2.rds.amazonaws.com:5432/meiDatabase";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "ScouredElmContempt8";
    private static final String POSTGRE_SQL_JDBC_DRIVER_NOT_FOUND = "PostgreSQL JDBC Driver not found.";

    /**
     * Populates the {@code start} and {@code end} lists with measure ranges that overlap
     * with the highlighted intervals in the music record contained within the request.
     * This method extracts and parses necessary interval and measure information from the request,
     * identifies matched ranges based on highlight data, and appends the result to the output lists.
     *
     * @param request The request object containing source music and highlight data.
     * @param start   A list to populate with the start measures of overlapping ranges.
     * @param end     A list to populate with the end measures of overlapping ranges.
     */
    public static void getHighlightMeasures(ReqPartialMusic request, List<Integer> start, List<Integer> end) {
        List<Byte> intervalSequence = parseByteList(request.getSource().getIntervals_text());
        List<Integer> measureMap = parseIntegerList(request.getSource().getMeasure_map());
        List<List<Byte>> highlightPatterns = getBytesFromHighlight(String.valueOf(request.getHighlight()));

        List<List<Integer>> beginEndMeasures = getMeasuresOfAllPatterns(highlightPatterns, intervalSequence, measureMap);

        for (List<Integer> measureRange : beginEndMeasures) {
            start.add(measureRange.get(0));
            end.add(measureRange.get(1));
        }

        start.sort(Comparator.naturalOrder());
        end.sort(Comparator.naturalOrder());
    }

    /**
     * Parses a space-separated string of bytes into a list of Byte objects.
     *
     * @param byteString A string containing space-separated byte values.
     * @return A list of bytes parsed from the input string.
     */
    public static List<Byte> parseByteList(String byteString) {
        String[] tokens = byteString.trim().split(" ");
        List<Byte> byteList = new ArrayList<>(tokens.length);
        for (String token : tokens) {
            byteList.add(Byte.parseByte(token));
        }
        return byteList;
    }

    /**
     * Converts a space-delimited string of numbers into a list of integers.
     *
     * @param str The input string containing space-separated integer values.
     * @return A list of Integer values.
     */
    public static List<Integer> parseIntegerList(String str) {
        String[] tokens = str.trim().split(" ");
        List<Integer> integers = new ArrayList<>(tokens.length);
        for (String token : tokens) {
            integers.add(Integer.parseInt(token));
        }
        return integers;
    }

    /**
     * Extracts a list of byte-encoded intervals from a highlight string containing
     * space-separated byte values enclosed within <em> tags.
     * For example, the string "<em>1 2 3</em><em>4 5</em>" would be parsed into:
     * [[1, 2, 3], [4, 5]]
     *
     * @param clumpedHighlight The raw highlight string containing <em> tags.
     * @return A list of byte lists, each representing a group of parsed interval values.
     */
    public static List<List<Byte>> getBytesFromHighlight(String clumpedHighlight) {
        List<String> highlightedSegments = extractEmphasizedSegments(clumpedHighlight);

        List<List<Byte>> parsedIntervals = new ArrayList<>();
        for (String segment : highlightedSegments) {
            parsedIntervals.add(parseByteList(segment));
        }

        return parsedIntervals;
    }

    /**
     * Extracts the string contents found between <em> and </em> tags.
     *
     * @param text The string containing HTML-emphasized segments.
     * @return A list of strings, each being the content inside a matched <em>...</em> block.
     */
    public static List<String> extractEmphasizedSegments(String text) {
        final String startTag = "<em>";
        final String endTag = "</em>";
        Set<String> result = new HashSet<>();

        int start = -1;
        int end = -1;

        while (true) {
            start = text.indexOf(startTag, start + 1);
            end = text.indexOf(endTag, end + 1);
            if (start == -1 || end == -1) break;

            String segment = text.substring(start + startTag.length(), end);
            result.add(segment);
        }

        return new ArrayList<>(result);
    }

    /**
     * Identifies ranges of measure numbers that correspond to matched highlight sequences.
     * Each matched sequence from {@code highlightPatterns} is located within the {@code allIntervals},
     * and its corresponding measure number range is extracted using {@code measureNumbers}.
     * Adjacent or overlapping measure ranges are merged into unified intervals.
     *
     * @param highlightPatterns A list of byte-sequence patterns to search for.
     * @param allIntervals      The complete list of byte intervals in order.
     * @param measureNumbers    A list of measure numbers mapped one-to-one with {@code allIntervals} + 1 element.
     * @return A list of merged measure number intervals represented as two-element lists [start, end].
     */
    public static List<List<Integer>> getMeasuresOfAllPatterns(
            List<List<Byte>> highlightPatterns,
            List<Byte> allIntervals,
            List<Integer> measureNumbers) {

        List<List<Integer>> matchedMeasureRanges = new ArrayList<>();

        for (List<Byte> pattern : highlightPatterns) {
            // Go through every highlighted chunk
            List<Integer> positionsFound = findSubsequencePositions(allIntervals, pattern);
            for (int i : positionsFound) {
                matchedMeasureRanges.add(List.of(measureNumbers.get(i), measureNumbers.get(i + pattern.size())));
            }
        }

        return mergeIntervals(matchedMeasureRanges);
    }

    /**
     * Finds all starting positions in the {@code main} list where the {@code pattern} list appears
     * as a contiguous subsequence. The search is performed by converting both lists to space-separated
     * string representations and then locating all occurrences of the {@code pattern} within the {@code main} string
     * as Java's string search is highly optimized.
     *
     * <p>The returned positions correspond to the starting indices in the {@code main} list where
     * each match begins.</p>
     *
     * @param main    the primary list of bytes to search within
     * @param pattern the byte pattern to search for as a subsequence
     * @return a list of starting indices where {@code pattern} occurs in {@code main}
     * @see #convertStringIndexToArrayIndex(String, int)
     */
    public static List<Integer> findSubsequencePositions(List<Byte> main, List<Byte> pattern) {
        String mainStr = listToDelimitedString(main);
        String patternStr = listToDelimitedString(pattern);

        List<Integer> positions = new ArrayList<>();
        int index = mainStr.indexOf(patternStr);

        while (index != -1) {
            int arrayIndex = convertStringIndexToArrayIndex(mainStr, index);
            positions.add(arrayIndex);
            index = mainStr.indexOf(patternStr, index + 1);
        }

        return positions;
    }

    /**
     * Converts the list of bytes to a space-delimited string with no brackets or commas.
     *
     * @param list the byte list to convert
     * @return a space-delimited string representation of the list
     */
    private static String listToDelimitedString(List<Byte> list) {
        return list.stream()
                .map(Object::toString)
                .collect(Collectors.joining(" "));
    }

    /**
     * Converts a string index from the space-delimited array string into a list index.
     * The index corresponds to the number of spaces (i.e., list elements) before the given character position.
     *
     * @param arrayStr the string representation of the list (e.g., {@code "12 14 1562 0 2 5 3"})
     * @param index    the character index within the string
     * @return the corresponding zero-based index in the original list
     */
    public static int convertStringIndexToArrayIndex(String arrayStr, int index) {
        String prefix = arrayStr.substring(0, index);
        return prefix.isEmpty() ? 0 : prefix.split(" ").length;
    }

    /**
     * Merges overlapping or adjacent intervals into consolidated ranges.
     * Each interval is a two-element list representing [start, end].
     *
     * @param intervals The list of intervals to merge.
     * @return A new list of merged intervals.
     */
    public static List<List<Integer>> mergeIntervals(List<List<Integer>> intervals) {
        if (intervals == null || intervals.isEmpty()) {
            return new ArrayList<>();
        }

        intervals.sort(Comparator.comparingInt(interval -> interval.get(0)));
        List<List<Integer>> merged = new ArrayList<>();
        List<Integer> current = new ArrayList<>(intervals.get(0));

        for (int i = 1; i < intervals.size(); i++) {
            List<Integer> next = intervals.get(i);

            if (current.get(1) >= next.get(0) - 1) {
                current.set(1, Math.max(current.get(1), next.get(1)));
            } else {
                merged.add(new ArrayList<>(current));
                current = new ArrayList<>(next);
            }
        }

        merged.add(current);
        return merged;
    }

    /**
     * Extracts specific segments of MEI sheet music from a file stored in a PostgreSQL database.
     * The segments correspond to the overlapping highlighted measures defined in the request.
     *
     * @param request The {@link ReqPartialMusic} containing highlight information and file reference.
     * @return A {@link ResPartialSheetMusic} object containing either the matched MEI segments or an error message.
     */
    public ResPartialSheetMusic getPartial(ReqPartialMusic request) {
        String fileContent = fetchFileFromDatabase(request.getSource().getName());
        if (fileContent == null || fileContent.equals(POSTGRE_SQL_JDBC_DRIVER_NOT_FOUND)) {
            return new ResPartialSheetMusic("File not found in database", false);
        }

        StringBuilder currentSegment = new StringBuilder();
        List<String> extractedSegments = new ArrayList<>();

        List<Integer> startMeasures = new ArrayList<>();
        List<Integer> endMeasures = new ArrayList<>();
        getHighlightMeasures(request, startMeasures, endMeasures);

        try (Scanner scanner = new Scanner(fileContent)) {
            boolean insideMeasure = false;
            int measureIndex = -1;
            int segmentIndex = 0;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line.matches(".*<measure.*")) {
                    measureIndex++;
                    insideMeasure = true;

                    if (measureIndex > endMeasures.get(segmentIndex) && segmentIndex + 1 < endMeasures.size()) {
                        segmentIndex++; // If the current measure is past the last segment, go to the next segment if there is one
                    }

                    if (insideRightMeasure(startMeasures, endMeasures, measureIndex, segmentIndex)) {
                        currentSegment.append(line).append("\n"); // Then check the measure
                    }

                } else if (line.matches(".*</measure>.*")) {
                    insideMeasure = false;

                    if (insideRightMeasure(startMeasures, endMeasures, measureIndex, segmentIndex)) {
                        currentSegment.append(line).append("\n"); // Add the closing measure tag

                        if (measureIndex == endMeasures.get(segmentIndex)) {
                            // If final measure in segment, toString the builder and reset it
                            extractedSegments.add(currentSegment.toString());
                            currentSegment = new StringBuilder();
                        }
                    }

                } else if (insideRightMeasure(startMeasures, endMeasures, measureIndex, segmentIndex) || !insideMeasure) {
                    // We aren't at a closing or ending tag, and we are in a correct measure
                    currentSegment.append(line).append("\n");

                }
            }
        }

        extractedSegments.add(currentSegment.toString()); // This is to add the end data of the mei file
        return new ResPartialSheetMusic(null, true, extractedSegments);
    }

    /**
     * Retrieves the MEI file content associated with the specified file name
     * from the PostgreSQL database table {@code public.meiFiles}.
     *
     * @param fileName The name of the file to fetch.
     * @return The content of the file if found; otherwise, {@code null} or a constant error message
     * such as {@link #POSTGRE_SQL_JDBC_DRIVER_NOT_FOUND} if the JDBC driver is missing.
     */
    public String fetchFileFromDatabase(String fileName) {
        String fileContent = null;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println(POSTGRE_SQL_JDBC_DRIVER_NOT_FOUND);
            return POSTGRE_SQL_JDBC_DRIVER_NOT_FOUND;
        }

        String query = "SELECT file_content FROM public.\"meiFiles\" WHERE file_name = ?";

        try (
                Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, fileName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                fileContent = rs.getString("file_content");
            }
        } catch (Exception e) {
            System.out.println("FetchFileFromDatabase: " + e.getMessage());
            e.printStackTrace();
        }

        return fileContent;
    }

    /**
     * Determines if the given measure number falls within the range defined by the
     * {@code start} and {@code end} lists at the specified index.
     *
     * @param start   A list of starting measure numbers.
     * @param end     A list of ending measure numbers.
     * @param checkMe The measure number to test.
     * @param pos     The index into the {@code start} and {@code end} lists to compare against.
     * @return {@code true} if {@code checkMe} lies between {@code start.get(pos)} and {@code end.get(pos)} (inclusive),
     * {@code false} otherwise or if {@code pos} is out of bounds.
     */
    private boolean insideRightMeasure(List<Integer> start, List<Integer> end, int checkMe, int pos) {
        if (pos >= start.size()) return false;
        return checkMe >= start.get(pos) && checkMe <= end.get(pos);
    }
}
