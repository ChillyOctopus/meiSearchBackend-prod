package parsers;

import music.Note;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * This class takes in a simple note element and parses it into our {@link Note} objects.
 */
public class NoteParser {

    /**
     * Uses previous functions to pull values from the mei and make a note
     *
     * @param noteElement the element we are pulling pitch, accidental, and octave from
     * @return A {@link Note} that represents the mei element.
     */
    public Note getNoteFromElement(Element noteElement) {
//        System.out.println("(getNoteFromEl): "+DocumentParser.elementToString(noteElement));
        return new Note(
                getPitchFromElement(noteElement),
                getAccidentalsFromElement(noteElement),
                getOctaveFromElement(noteElement),
                getDurationFromElement(noteElement)
        );
    }

    /**
     * This function takes in a mei note element and returns a pitch name
     *
     * @param noteElement the element in mei format: "<note> ... </note>"
     * @return the char of the pitch, 'a' through 'g'.
     */
    public char getPitchFromElement(Element noteElement) {
        return noteElement.getAttribute("pname").charAt(0);
    }

    /**
     * This function takes in a mei note element and returns its accidental
     *
     * @param noteElement the element in mei format: "<note> ... </note>"
     * @return the string of the accidental found, or null for nothing.
     */
    public String getAccidentalsFromElement(Element noteElement) {
        // Try attribute-based accidental first
        if (noteElement.hasAttribute("accid")) {
            return noteElement.getAttribute("accid");
        }

        // If not found, check for <accid> child element
        NodeList accidNodes = noteElement.getElementsByTagName("accid");
        if (accidNodes.getLength() > 0) {
            Element accidElement = (Element) accidNodes.item(0);
            if (accidElement.hasAttribute("accid")) {
                return accidElement.getAttribute("accid");
            }
        }

        return null;
    }

    /**
     * This function returns the octave integer of the note
     *
     * @param noteElement the element in mei format: "<note> ... </note>"
     * @return the int of the octave that we found based on piano pitch, 0, 1, 2, ect.
     */
    public int getOctaveFromElement(Element noteElement) {
        return Integer.parseInt(noteElement.getAttribute("oct"));
    }

    /**
     * This function gets the total duration of a note, factoring in the dots
     *
     * @param noteElement the note element we are extracting the information from
     * @return the total duration
     */
    public Float getDurationFromElement(Element noteElement) {
        Integer dur = getRawDurationFromElement(noteElement);
        if (dur == null) return null;
        int dots = getDotsFromElement(noteElement);
        return (dots == 0) ? dur : dur * (1 + getDotValueAdded(dots));
    }

    /**
     * This function takes in a mei note element and returns the raw duration, which is how many fit in a measure
     *
     * @param noteElement the element in mei format: "<note> ... </note>"
     * @return the duration encoded
     */
    private Integer getRawDurationFromElement(Element noteElement) {
        if (!noteElement.hasAttribute("dur")) return null;
        try {
            return Integer.parseInt(noteElement.getAttribute("dur"));
        } catch (NumberFormatException ex) {
            System.out.println("Invalid dur attribute: " + noteElement.getAttribute("dur"));
            return null;
        }
    }

    /**
     * This function takes in a mei note noteElement and returns the number of dots, if any
     *
     * @param noteElement the element in mei format: "<note> ... </note>"
     * @return the number of dots
     */
    private int getDotsFromElement(Element noteElement) {
        if (!noteElement.hasAttribute("dots")) return 0;
        try {
            return Integer.parseInt(noteElement.getAttribute("dots"));
        } catch (NumberFormatException ex) {
            System.out.println("Invalid dots attribute: " + noteElement.getAttribute("dots"));
            return 0;
        }
    }

    /**
     * This returns the value added by subsequent dots. Dots = 2^1 - 1 / 2^1, i.e. 1 dot = 1/2, 2 dots = 3/4, ect, 3 dots = 7/8, ect
     *
     * @param dots the number of dots the note has
     * @return the float to add the duration of the note
     */
    private float getDotValueAdded(int dots) {
        float nume = (float) (Math.pow(2, dots) - 1);
        float deno = (float) Math.pow(2, dots);
        float val = nume / deno;
        return val;
    }
}
