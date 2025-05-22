package parsers;

import music.*;
import music.Chord;

import java.util.*;

/**
 * This class parses out our measures when given measure strings. It uses a {@link ChordParser} and a {@link NoteParser} to do this. It returns
 * a measure based on its internal {@link KeySig} class, and keeps track of the accidentals for the measure as it goes along in the
 * {@link AccidentalTracker} class. It makes a new {@link Measure} object with the list of {@link Chord} and {@link Note} it found in the measure string.
 */
public class MeasureParser extends Base_Parser{
    /**
     * The key sig a measure this parses is to be under influence from
     */
    private KeySig keySig;

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

    public MeasureParser(KeySig keySig) {
        this.keySig = keySig;
        noteParser = new NoteParser();
        chordParser = new ChordParser();
        accidentalTracker = new AccidentalTracker();
    }

    public MeasureParser(){
        this(new KeySig("0"));
    }

    /**
     * A wrapper function for getting a measure
     * @param meiMeasureChunk the string we are parsing the measure out of
     * @return a new measure with the key sig this parser is currently set to.
     */
    public Measure getMeasureFromMei(String meiMeasureChunk){
        return new Measure(keySig, getNoteRepFromMei(meiMeasureChunk), getMeasureNumFromMei(meiMeasureChunk));
    }

    /**
     * This function finds the measure number of our current measure
     * @param meiMeasureChunk the mei chunk we are using to find the num
     * @return the measure number we find
     */
    private int getMeasureNumFromMei(String meiMeasureChunk){
        return Integer.parseInt(getQuotedInTags(" n=", meiMeasureChunk).getData());
    }

    /**
     * Extracts all {@link Note} and {@link Chord} objects from the provided MEI measure chunk string.
     *
     * <p>Only the first {@code <staff>} and its first {@code <layer>} are considered in parsing.
     * Notes and chords are parsed in the order they appear within this layer.
     * Accidentals are tracked and applied consistently throughout the measure.
     *
     * @param meiMeasureChunk the MEI string chunk representing a measure
     * @return a List of {@link Note} and {@link Chord} objects in the order found within the measure
     */
    private List<Object> getNoteRepFromMei(String meiMeasureChunk) {
        meiMeasureChunk = extractFirstStaffLayer(meiMeasureChunk);

        List<Object> noteAndChordList = new ArrayList<>();
        accidentalTracker.clear();

        int chordSearchIndex = 0;
        int noteSearchIndex = 0;

        while (true) {
            ParsedData nextNoteData = getDataBetweenTags("<note", "/>", noteSearchIndex, meiMeasureChunk);
            if (!nextNoteData.isFound()) break;

            ParsedData nextChordData = new ParsedData("", -2, -2, false);
            if (chordSearchIndex != -1) {
                nextChordData = getDataBetweenTags("<chord", "/chord>", chordSearchIndex, meiMeasureChunk);
                if (!nextChordData.isFound()) chordSearchIndex = -1;
            }

            Object elementToAdd;
            if (chordSearchIndex != -1 && nextChordData.getStartIndex() < nextNoteData.getStartIndex()) {
                elementToAdd = getChordFromMeasureMei(nextChordData.getData());
                chordSearchIndex = nextChordData.getEndIndex();
                noteSearchIndex = nextNoteData.getEndIndex();
            } else {
                Note note = noteParser.getNoteFromMei(nextNoteData.getData());
                elementToAdd = updatedNoteFromAccidentalTracker(note);
                noteSearchIndex = nextNoteData.getEndIndex();
            }
            noteAndChordList.add(elementToAdd);
        }

        return noteAndChordList;
    }

    /**
     * Extracts the first {@code <staff>} element and its first {@code <layer>} element from the given MEI measure chunk.
     * If either is not found, returns the original chunk and logs an error.
     *
     * @param measureChunk the MEI measure chunk string
     * @return a substring representing the first layer inside the first staff, or the original chunk if extraction fails
     */
    private String extractFirstStaffLayer(String measureChunk) {
        ParsedData staffChunk = getDataBetweenTags("<staff", "/staff>", 0, measureChunk);
        if (!staffChunk.isFound()) {
            System.out.println("Unable to find <staff> inside measure: " + measureChunk);
            return measureChunk;
        }

        ParsedData layerChunk = getDataBetweenTags("<layer", "/layer>", 0, staffChunk.getData());
        if (!layerChunk.isFound()) {
            System.out.println("Unable to find <layer> inside staff: " + staffChunk.getData());
            return staffChunk.getData();
        }

        return layerChunk.getData();
    }


    /**
     * Returns a cord from the mei string, all the notes are checked against and inputted into the accidental tracker
     * @param chordString the string we are pulling the chord from
     * @return a sorted chord
     */
    private Chord getChordFromMeasureMei(String chordString){
        Chord c = chordParser.getChordFromMei(chordString);
        List<Note> newNotes = new ArrayList<>();
        //Add the all notes as they are fixed to a new Notes list
        for(int i = 0; i < c.getNotes().size(); i++){
            Note n = updatedNoteFromAccidentalTracker(c.getNotes().get(i));
            newNotes.add(n);
        }
        //Make a new chord with the new notes just in case they changed positions with the reassignment of accidentals. It sorts them in the constructor.
        return new Chord(newNotes);
    }

    /**
     * This function updates the accidental tracker as well as returns the new note created by the accidentals or lack thereof
     * @param n The note we give it
     * @return the new note that is formed by the accidental tracker.
     */
    private Note updatedNoteFromAccidentalTracker(Note n){
        if(n.getAccidental() != null){
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
     * @param keySig the new key signature given to us
     */
    public void setKeySig(KeySig keySig) {
        this.keySig = keySig;
    }
}
