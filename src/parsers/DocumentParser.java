package parsers;

import music.Document;
import music.KeySig;
import music.Measure;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * This class parses files using a {@link MeasureParser}, {@link KeySigParser}, and {@link MetaDataParser}.
 * It maintains sequential processing to ensure key signatures affect the correct measures in document order.
 */
public class DocumentParser {
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
     * Cached DOM document to avoid re-parsing
     */
    public org.w3c.dom.Document domDocument;
    /**
     * The File that this parser is going to use to parse out into a document
     */
    private File inFile;

    public DocumentParser() {
        this.measureParser = new MeasureParser();
        this.keySigParser = new KeySigParser();
        this.metaDataParser = new MetaDataParser();
    }

    /**
     * Turns a w3 element into a String properly, usually for debugging
     *
     * @param element the Element we are transforming
     * @return a String that represents the XML of the element
     */
    public static String elementToString(Element element) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(element), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            System.out.println("Exception in element to String. Returning null.");
            return null;
        }
    }

    public Document getDocumentFromFile() throws FileNotFoundException {
        return new Document(getListOfMeasuresFromInFile(), getMetadataFromInFile());
    }

    /**
     * Parses the MEI file into a DOM Document for efficient processing.
     * Caches the result to avoid reparsing for multiple operations.
     *
     * @return the parsed DOM Document
     * @throws FileNotFoundException if the input file cannot be found
     */
    private org.w3c.dom.Document getDOMDocument() throws FileNotFoundException {
        if (domDocument == null) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true); // MEI uses namespaces
                DocumentBuilder builder = factory.newDocumentBuilder();
                domDocument = builder.parse(inFile);
            } catch (Exception e) {
                throw new FileNotFoundException("Could not parse MEI file: " + e.getMessage());
            }
        }
        return domDocument;
    }

    /**
     * Parses and returns a list of {@link Measure} objects from the input file.
     *
     * <p>Processes elements in document order, updating the {@code measureParser}'s key signature
     * when a key signature element is encountered, ensuring proper sequential state management.
     *
     * @return a list of parsed {@link Measure} objects representing the document's measures
     * @throws FileNotFoundException if the input file cannot be found
     */
    private List<Measure> getListOfMeasuresFromInFile() throws FileNotFoundException {
        List<Measure> measures = new ArrayList<>();

        try {
            org.w3c.dom.Document doc = getDOMDocument();

            // Get all keySig and measure elements in document order
            List<Element> orderedElements = getKeySigAndMeasureElementsInOrder(doc);

            // Process elements sequentially to maintain state
            for (Element element : orderedElements) {
                if (isKeySigElement(element)) {
//                    System.out.println("Parsing keysig");
                    KeySig newKeySig = keySigParser.getKeySigFromElement(element);
                    measureParser.setKeySig(newKeySig);
                } else if (isMeasureElement(element)) {
//                    System.out.println("Parsing measure");
                    measures.add(measureParser.getMeasureFromElement(element));
                }
            }

        } catch (Exception e) {
            System.out.println("Exception in getListOfMeasuresFromInFile: " + e);
        }

        return measures;
    }

    /**
     * Gets all keySig and measure elements in document order.
     * This preserves the sequential processing behavior of the original scanner approach.
     *
     * @param doc the DOM document to search
     * @return a list of elements in document order
     */
    private List<Element> getKeySigAndMeasureElementsInOrder(org.w3c.dom.Document doc) {
        List<Element> orderedElements = new ArrayList<>();

        // Traverse the entire document tree in order
        traverseForKeySigAndMeasure(doc.getDocumentElement(), orderedElements);

        return orderedElements;
    }

    /**
     * Recursively traverses the DOM tree to find keySig and measure elements in document order.
     *
     * @param node            the current node being traversed
     * @param orderedElements the list to accumulate elements in order
     */
    private void traverseForKeySigAndMeasure(Node node, List<Element> orderedElements) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            if (isKeySigElement(element) || isMeasureElement(element)) {
                orderedElements.add(element);
            }
        }

        // Traverse children in order
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            traverseForKeySigAndMeasure(children.item(i), orderedElements);
        }
    }

    /**
     * Determines if an element is a key signature element.
     * Handles various MEI key signature representations.
     *
     * @param element the element to check
     * @return true if this is a key signature element
     */
    private boolean isKeySigElement(Element element) {
        String tagName = element.getTagName().toLowerCase();
        return tagName.contains("key") && tagName.contains("sig");
    }

    /**
     * Determines if an element is a measure element.
     *
     * @param element the element to check
     * @return true if this is a measure element
     */
    private boolean isMeasureElement(Element element) {
        return element.getTagName().equalsIgnoreCase("measure");
    }

    /**
     * Extracts metadata from the input file's MEI header section.
     *
     * <p>Finds the meiHead element and passes it to the metadata parser.
     *
     * @return a HashMap of metadata key-value pairs parsed from the file
     * @throws FileNotFoundException if the input file cannot be found
     */
    private HashMap<String, String> getMetadataFromInFile() throws FileNotFoundException {
//        System.out.println("Parsing metadata");
        try {
            org.w3c.dom.Document doc = getDOMDocument();

            // Find the meiHead element
            NodeList meiHeadList = doc.getElementsByTagName("meiHead");
            if (meiHeadList.getLength() > 0) {
                Element meiHead = (Element) meiHeadList.item(0);
                return metaDataParser.getDataFromElement(meiHead);
            } else {
                // No meiHead found, return empty metadata
                System.out.println("No meiHead tags found in: " + doc);
                return new HashMap<>();
            }

        } catch (Exception e) {
            System.out.println("Exception in getMetadataFromInFile: " + e);
            return new HashMap<>();
        }
    }

    /**
     * Setting the inFile to a file to read from.
     * Clears the cached DOM document when file changes.
     *
     * @param inFile The file we are setting this classes internal file to
     */
    public void setInFile(File inFile) {
        if (!Objects.equals(this.inFile, inFile)) {
            this.inFile = inFile;
            this.domDocument = null; // Clear cache when file changes
        }
    }
}