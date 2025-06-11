package music;

import java.util.Comparator;
import java.util.List;

/**
 * This class represents Chords in the music. The Chord is just a List of {@link Note notes}, sorted in descending order, as a written
 * chord would be. It has a special comparator to do this. It can return its top {@link Note}.
 */
public class Chord {
    /**
     * This list of {@link Note Notes} represents our chord, and the notes are sorted in descending order.
     */
    private final List<Note> notes;

    /**
     * This is the duration of all the notes in the chord
     */
    private final Float duration;

    /**
     * This constructor is only special because it calls notes.sort with our custom comparator.
     */
    public Chord(List<Note> notes, Float duration) {
        this.notes = notes;
        this.duration = duration;
        sortNotes();
    }

    /**
     * This constructor is only special because it calls notes.sort with our custom comparator.
     */
    public Chord(List<Note> notes) {
        this(notes, null);
    }

    /**
     * Gets the first note in the list of notes that make up our chord. If our sorting function works, it should be the highest one.
     *
     * @return A {@link Note}, the first and thus highest in our "chord".
     */
    public Note getTopNote() {
        return notes.get(0);
    }

    /**
     * Sorts the notes using the comparator. See the notePosComparator for the logic
     */
    public void sortNotes() {
        notes.sort(new notePosComparator());
    }

    public List<Note> getNotes() {
        return notes;
    }

    public Float getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "Chord{" +
                "notes=" + notes +
                '}';
    }

    /**
     * Our custom comparator. Notes higher on the keyboard should be sorted above those that are not.
     */
    public static class notePosComparator implements Comparator<Note> {
        @Override
        public int compare(Note o1, Note o2) {
            return Integer.compare(o2.getIntVal(), o1.getIntVal());
        }
    }
}
