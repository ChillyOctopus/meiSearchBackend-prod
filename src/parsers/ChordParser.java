package parsers;

import music.Chord;
import music.Note;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * This class parses {@link Chord Chords} when given a mei string. It uses a {@link NoteParser} to do this and returns a {@link Chord} based on the
 * list of {@link Note} objects it found inside the chord string.
 */
public class ChordParser {
    /**
     * The note parser we use to gather our Notes
     */
    private final NoteParser noteParser;

    public ChordParser() {
        this.noteParser = new NoteParser();
    }

    /**
     * Parses a {@link Chord} object from the given MEI-formatted element
     *
     * <p>This method extracts the duration specified by the "dur" attribute, if present,
     * and iteratively parses all contained notes within {@code <note ... />} tags using
     * {@link NoteParser}
     *
     * @param chordElement the MEI chord element
     * @return a new {@link Chord} instance composed of parsed notes and optional duration
     */
    public Chord getChordFromElement(Element chordElement) {
//        System.out.println("(getChordFromEl): "+DocumentParser.elementToString(chordElement));
        List<Note> chordNotes = new ArrayList<>();
        Float duration = null;
        if (chordElement.hasAttribute("dur")) {
            try {
                duration = Float.parseFloat(chordElement.getAttribute("dur"));
            } catch (NumberFormatException ignored) {
            }
        }

        NodeList children = chordElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("note")) {
                Element noteElement = (Element) child;

                // Set duration from the first note that contains it
                if (duration == null && noteElement.hasAttribute("dur")) {
                    try {
                        duration = Float.parseFloat(noteElement.getAttribute("dur"));
                    } catch (NumberFormatException ignored) {
                    }
                }

                Note note = noteParser.getNoteFromElement(noteElement);
                chordNotes.add(note);
            }
        }

        return new Chord(chordNotes, duration);
    }

}
