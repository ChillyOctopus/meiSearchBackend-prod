package parsers;

import music.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This class parses {@link Chord Chords} when given a mei string. It uses a {@link NoteParser} to do this and returns a {@link Chord} based on the
 * list of {@link Note} objects it found inside the chord string.
 */
public class ChordParser extends Base_Parser {
    /**
     * The note parser we use to gather our Notes
     */
    private final NoteParser noteParser;

    /**
     * Parses a {@link Chord} object from the given MEI-formatted chord string.
     *
     * <p>This method extracts the duration specified by the "dur" attribute, if present,
     * and iteratively parses all contained notes within {@code <note ... />} tags using
     * {@link NoteParser}
     *
     * @param meiChordChunk the MEI chord string chunk to parse
     * @return a new {@link Chord} instance composed of parsed notes and optional duration
     */
    public Chord getChordFromMei(String meiChordChunk) {
        List<Note> chordNotes = new ArrayList<>();
        int searchIndex = 0;

        ParsedData durationData = getQuotedInTags("dur", meiChordChunk);
        Float duration = durationData.isFound() ? Float.parseFloat(durationData.getData()) : null;

        while (true) {
            ParsedData noteData = getDataBetweenTags("<note", "/>", searchIndex, meiChordChunk);
            if (!noteData.isFound()) {
                break;
            }
            chordNotes.add(noteParser.getNoteFromMei(noteData.getData()));
            searchIndex = noteData.getEndIndex();
        }

        return new Chord(chordNotes, duration);
    }


    public ChordParser(){
        this.noteParser = new NoteParser();
    }

}
