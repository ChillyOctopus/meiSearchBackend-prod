package parsers;

import music.Document;
import music.Measure;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class parses files using a {@link MeasureParser}, {@link KeySigParser}, and {@link MetaDataParser}. When it finds a new key signature, it parses it and gives
 * it to the MeasureParser to work with. Otherwise, it parses out measures and gives the resulting strings to the MeasureParser
 * for parsing into measures. It takes these measures and creates a new {@link Document} object with them.
 */
public class DocumentParser extends Base_Parser{
    /**
     * The measure parser this document parser uses work with measures
     */
    private final MeasureParser measureParser;

    /**
     * The keysig parser this document parser uses to work with keysigs
     */
    private final KeySigParser keySigParser;

    /**
     * The metadata parser this document parser uses to work with metadata
     */
    private final MetaDataParser metaDataParser;

    /**
     * The File that this parser is going to use to parse out into a document
     */
    private File inFile;

    public Document getDocumentFromFile() throws FileNotFoundException {
        return new Document(getListOfMeasuresFromInFile(), getMetadataFromInFile());
    }

    /**
     * Parses and returns a list of {@link Measure} objects from the input file.
     *
     * <p>Scans the file line-by-line, updating the {@code measureParser}'s key signature
     * when a key signature line is detected, and collecting measure data to parse into {@code Measure} instances.
     *
     * @return a list of parsed {@link Measure} objects representing the document's measures
     * @throws FileNotFoundException if the input file cannot be found
     */
    private List<Measure> getListOfMeasuresFromInFile() throws FileNotFoundException {
        List<Measure> measures = new ArrayList<>();
        try (Scanner scanner = new Scanner(inFile)) {
            while (scanner.hasNextLine()) {
                if (scanner.hasNext(".*keySig.*")) {
                    String keyLine = scanner.nextLine();
                    measureParser.setKeySig(keySigParser.getKeySig(keyLine));
                } else if (scanner.hasNext(".*measure.*")) {
                    measures.add(parseMeasureFromScanner(scanner));
                } else {
                    scanner.nextLine();
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in getListOfMeasuresFromInFile: " + e);
        }
        return measures;
    }

    /**
     * Parses a single {@link Measure} from the current position of the scanner.
     *
     * <p>Reads lines until the closing {@code </measure>} tag is found, accumulating
     * the measure's content for parsing.
     *
     * @param scanner the scanner reading from the input source
     * @return a parsed {@link Measure} object representing the scanned measure chunk
     */
    private Measure parseMeasureFromScanner(Scanner scanner) {
        StringBuilder measureContent = new StringBuilder();
        while (!scanner.hasNext(".*</measure>.*")) {
            measureContent.append(scanner.nextLine());
        }
        measureContent.append(scanner.nextLine());  // Append closing tag line
        return measureParser.getMeasureFromMei(measureContent.toString());
    }

    /**
     * Extracts metadata from the input file's MEI header section.
     *
     * <p>Reads lines until the closing {@code </meiHead>} tag is reached, then
     * passes the accumulated metadata string to the {@code metaDataParser}.
     *
     * @return a HashMap of metadata key-value pairs parsed from the file
     * @throws FileNotFoundException if the input file cannot be found
     */
    private HashMap<String, String> getMetadataFromInFile() throws FileNotFoundException {
        StringBuilder metadataContent = new StringBuilder();
        try (Scanner scanner = new Scanner(inFile)) {
            if (!scanner.hasNextLine()) {
                return metaDataParser.getData(metadataContent.toString());
            }
            while (!scanner.hasNext(".*</meiHead>.*")) {
                metadataContent.append(scanner.nextLine());
            }
            if (!metadataContent.isEmpty()) {
                metadataContent.append(scanner.nextLine()); // Append closing tag line
            }
        } catch (Exception e) {
            System.out.println("Exception in getMetadataFromInFile: " + e);
        }
        return metaDataParser.getData(metadataContent.toString());
    }


    public DocumentParser() {
        this.measureParser = new MeasureParser();
        this.keySigParser = new KeySigParser();
        this.metaDataParser = new MetaDataParser();
    }

    /**
     * Setting the inFile to a file to read from.
     * @param inFile The file we are setting this classes internal file to
     */
    public void setInFile(File inFile) {
        this.inFile = inFile;
    }
}
