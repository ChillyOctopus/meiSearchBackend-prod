package serverCode.Services;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import parsers.DocumentParser;
import serverCode.Requests.ReqPartialMusic;
import serverCode.Responses.ResPartialSheetMusic;
import workers.Indexer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static parsers.DocumentParser.elementToStringWithoutXmlDeclaration;

/**
 * The class responsible for finding the appropriate partial sheet music chunks in highlighted search results and sending them back
 */
public class PartialSheetMusic {
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
            // Check to make sure we didn't match '3 4 5' to '-3 4 5' or '113 4 5'
            // If it isn't the beginning of the string, there should be a space right before the match
            if (index != 0 && mainStr.charAt(index-1) != ' '){
                // Otherwise we have hit a false match
                index = mainStr.indexOf(patternStr, index + 1);
                continue;
            }
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
        String fileContent = Indexer.getFileByName(request.getSource().getName());
        if (fileContent == null) return new ResPartialSheetMusic("File not found in database", false);
        System.out.println("File content in getPartial: "+fileContent);

        List<Integer> startMeasures = new ArrayList<>();
        List<Integer> endMeasures = new ArrayList<>();
        getHighlightMeasures(request, startMeasures, endMeasures);

        try {
            DocumentParser documentParser = new DocumentParser();
            // Create temporary file from database content for parsers use only
            File tempFile = createTempFileFromContent(fileContent);
            // Set the file
            documentParser.setInFile(tempFile);
            // Parse the file
            org.w3c.dom.Document domDocument = documentParser.getDOMDocument();
            // Delete the file
            tempFile.delete();

            System.out.println("DOM doc toString: "+DocumentParser.documentToString(domDocument));

            // Remove all 'section' elements, except the first, in a clone
            NodeList sectionElements = domDocument.getElementsByTagName("section");
            if (sectionElements.getLength() == 0) {
                throw new Exception("No 'section' elements found in getPartial");
            }

            if (sectionElements.getLength() > 1) {
                System.out.println("Multiple 'section' elements found in getPartial. Removing all but one");
                // Collect nodes to remove (can't remove while iterating due to live NodeList)
                List<Node> sectionsToRemove = new ArrayList<>();
                for (int i = 1; i < sectionElements.getLength(); i++){
                    sectionsToRemove.add(sectionElements.item(i));
                }
                // Remove extra sections
                for (Node section : sectionsToRemove) {
                    section.getParentNode().removeChild(section);
                }
            }

            // We need to extract the segments before we extract the skeleton, as extracting the skeleton
            // deletes the object data inside the 'section' tag (which holds the measures)
            List<String> extractedSegments = extractMeasureSegments(domDocument, startMeasures, endMeasures);
            String meiSkeleton = extractMeiSkeleton(domDocument);

            return new ResPartialSheetMusic(null, true, extractedSegments, meiSkeleton);

        } catch (Exception e) {
            System.err.println("Error parsing MEI document: " + e.getMessage());
            return new ResPartialSheetMusic("Error parsing document", false);
        }
    }

    /**
     * Creates a temporary file from the database content string.
     */
    private File createTempFileFromContent(String content) throws IOException {
        File tempFile = File.createTempFile("temp_mei", ".xml");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
        return tempFile;
    }

    /**
     * Extracts the specified measure segments, including any key signature changes
     * that occur within each segment's range.
     */
    private List<String> extractMeasureSegments(org.w3c.dom.Document doc,
                                                List<Integer> startMeasures,
                                                List<Integer> endMeasures) {
        List<String> segments = new ArrayList<>();

        try {
            // Get all keySig and measure elements in document order from the 'section' tag
            NodeList nodeList = doc.getElementsByTagName("section");
            if (nodeList.getLength() == 0) {
                throw new Exception("No 'section' elements found in extractMeasureSegments");
            } else if (nodeList.getLength() > 1) {
                throw new Exception("Multiple 'section' elements found in extractMeasureSegments");
            }

            //Get all the keysig and measure elements in document order starting from the 'section' node
            List<Element> orderedElements = DocumentParser.getKeySigAndMeasureElementsInOrder(doc, nodeList.item(0));

            for (int segmentIndex = 0; segmentIndex < startMeasures.size(); segmentIndex++) {
                int startMeasure = startMeasures.get(segmentIndex);
                int endMeasure = endMeasures.get(segmentIndex);

                StringBuilder segmentBuilder = new StringBuilder();

                // Find key signature that should be included before this segment
                Element relevantKeySig = findRelevantKeySigForSegment(orderedElements, startMeasure);
                if (relevantKeySig != null)
                    segmentBuilder.append(elementToStringWithoutXmlDeclaration(relevantKeySig)).append("\n");

                // Extract measures in the range
                int currentMeasureIndex = 0;
                for (Element element : orderedElements) {
                    if (DocumentParser.isMeasureElement(element)) {
                        if (currentMeasureIndex >= startMeasure && currentMeasureIndex <= endMeasure) {
                            segmentBuilder.append(elementToStringWithoutXmlDeclaration(element)).append("\n");
                        }
                        currentMeasureIndex++;

                        // Break early if we've passed the end measure
                        if (currentMeasureIndex > endMeasure) {
                            break;
                        }
                    }
                }

                segments.add(segmentBuilder.toString());
            }

        } catch (Exception e) {
            System.err.println("Error extracting measure segments: " + e.getMessage());
        }

        return segments;
    }

    /**
     * Removes everything inside the <section> tag, to be populated with measures in the frontend
     */
    private String extractMeiSkeleton(org.w3c.dom.Document doc) {
        try {
            // Get the first only section element
            Element firstSection = (Element) doc.getElementsByTagName("section").item(0);

            // Remove all child elements from the section
            NodeList sectionChildren = firstSection.getChildNodes();
            List<Node> toRemove = new ArrayList<>();

            // Collect all child nodes to remove
            for (int i = 0; i < sectionChildren.getLength(); i++){
                toRemove.add(sectionChildren.item(i));
            }

            // Remove all child nodes from the section
            for (Node child : toRemove) {
                firstSection.removeChild(child);
            }

            return elementToStringWithoutXmlDeclaration(doc.getDocumentElement());

        } catch (Exception e) {
            System.err.println("Error extracting MEI skeleton: " + e.getMessage());
            return "";
        }
    }

    /**
     * Finds key signatures that are relevant for a given measure segment.
     * Includes the most recent key signature before the segment starts.
     */
    private Element findRelevantKeySigForSegment(List<Element> orderedElements, int startMeasure) {
        Element lastKeySigBeforeSegment = null;

        int measureIndex = 0;
        for (Element element : orderedElements) {
            if (measureIndex >= startMeasure) break;
            if (DocumentParser.hasOrIsKeySigElement(element)) {
                lastKeySigBeforeSegment = element;
            } else if (DocumentParser.isMeasureElement(element)) {
                measureIndex++;
            }
        }
        return lastKeySigBeforeSegment;
    }
}