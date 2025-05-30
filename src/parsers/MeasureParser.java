package parsers;

import music.Chord;
import music.KeySig;
import music.Measure;
import music.Note;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * This class parses out our measures when given measure strings. It uses a {@link ChordParser} and a {@link NoteParser} to do this. It returns
 * a measure based on its internal {@link KeySig} class, and keeps track of the accidentals for the measure as it goes along in the
 * {@link AccidentalTracker} class. It makes a new {@link Measure} object with the list of {@link Chord} and {@link Note} it found in the measure string.
 */
public class MeasureParser {
    /**
     * The note parser we are using to parse the notes
     */
    private final NoteParser noteParser;
    /**
     * The chord parser we are using to parse the chords
     */
    private final ChordParser chordParser;
    /**
     * The accidental tracker we are using for every measure
     */
    private final AccidentalTracker accidentalTracker;
    /**
     * The key sig a measure this parses is to be under influence from
     */
    private KeySig keySig;

    public MeasureParser(KeySig keySig) {
        this.keySig = keySig;
        noteParser = new NoteParser();
        chordParser = new ChordParser();
        accidentalTracker = new AccidentalTracker();
    }

    public MeasureParser() {
        this(new KeySig("0"));
    }

    /**
     * A wrapper function for getting a measure
     *
     * @param measureElement the element we are parsing the measure out of
     * @return a new measure with the key sig this parser is currently set to.
     */
    public Measure getMeasureFromElement(Element measureElement) {
//        System.out.println("(getMeasureFromEl): "+DocumentParser.elementToString(measureElement));
        List<Object> noteReps = getNoteRepFromMei(measureElement);
        return new Measure(keySig, noteReps);
    }

    /**
     * Extracts all {@link Note} and {@link Chord} objects from the provided MEI measure element.
     *
     * <p>Only the first {@code <staff>} and its first {@code <layer>} are considered in parsing.
     * Notes and chords are parsed in the order they appear within this layer.
     * Accidentals are tracked and applied consistently throughout the measure.
     *
     * @param measureElement the MEI element representing a measure
     * @return a List of {@link Note} and {@link Chord} objects in the order found within the measure
     */
    private List<Object> getNoteRepFromMei(Element measureElement) {
        List<Object> noteAndChordList = new ArrayList<>();
        accidentalTracker.clear();

        // Get first <staff>
        NodeList staffs = measureElement.getElementsByTagName("staff");
        if (staffs.getLength() == 0) {
            System.out.println("No staff tags found in element: " + DocumentParser.elementToString(measureElement));
            return noteAndChordList;
        }
        Element staff = (Element) staffs.item(0);

        // Get first <layer>
        NodeList layers = staff.getElementsByTagName("layer");
        if (layers.getLength() == 0) {
            System.out.println("No layer tags found in element: " + DocumentParser.elementToString(measureElement));
            return noteAndChordList;
        }
        Element layer = (Element) layers.item(0);

        // Recursively process children of <layer> in document order
        NodeList layerChildren = layer.getChildNodes();
        for (int i = 0; i < layerChildren.getLength(); i++) {
            Node node = layerChildren.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;
            Element element = (Element) node;

            switch (element.getTagName()) {
                case "note" -> {
                    Note parsedNote = noteParser.getNoteFromElement(element);
                    Note updatedNote = updatedNoteFromAccidentalTracker(parsedNote);
                    noteAndChordList.add(updatedNote);
                }
                case "chord" -> {
                    Chord parsedChord = chordParser.getChordFromElement(element);
                    List<Note> updatedNotes = new ArrayList<>();
                    for (Note note : parsedChord.getNotes()) {
                        updatedNotes.add(updatedNoteFromAccidentalTracker(note));
                    }
                    noteAndChordList.add(new Chord(updatedNotes));
                }
                case "beam" -> {
                    NodeList beamNotes = element.getChildNodes();
                    for (int j = 0; j < beamNotes.getLength(); j++) {
                        Node beamChild = beamNotes.item(j);
                        if (beamChild.getNodeType() != Node.ELEMENT_NODE) continue;
                        Element beamElem = (Element) beamChild;
                        if (beamElem.getTagName().equals("note")) {
                            Note parsedNote = noteParser.getNoteFromElement(beamElem);
                            Note updatedNote = updatedNoteFromAccidentalTracker(parsedNote);
                            noteAndChordList.add(updatedNote);
                        } else if (beamElem.getTagName().equals("chord")) {
                            Chord parsedChord = chordParser.getChordFromElement(beamElem);
                            List<Note> updatedNotes = new ArrayList<>();
                            for (Note note : parsedChord.getNotes()) {
                                updatedNotes.add(updatedNoteFromAccidentalTracker(note));
                            }
                            noteAndChordList.add(new Chord(updatedNotes));
                        }
                    }
                }
                // Ignore other tags
            }
        }

        return noteAndChordList;
    }

    /**
     * This function updates the accidental tracker as well as returns the new note created by the accidentals or lack thereof
     *
     * @param n The note we give it
     * @return the new note that is formed by the accidental tracker.
     */
    private Note updatedNoteFromAccidentalTracker(Note n) {
        if (n.getAccidental() != null) {
            accidentalTracker.modifyAccidentalMap(n.getPitch(), n.getAccidental());
        } else {
            String accidental = accidentalTracker.getCurrentAccidentalOfPitch(n.getPitch());
            n = n.applyAccidental(accidental);
        }
        return n;
    }

    public KeySig getKeySig() {
        return keySig;
    }

    /**
     * What we use to update our keysig when we come across a new one
     *
     * @param keySig the new key signature given to us
     */
    public void setKeySig(KeySig keySig) {
        this.keySig = keySig;
    }
}
