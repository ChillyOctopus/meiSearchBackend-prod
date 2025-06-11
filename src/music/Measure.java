package music;

import exceptions.Empty;

import java.util.List;

/**
 * This class represents our measures. Each measure has a note representation as a list of Objects, and a {@link KeySig} class the
 * measure is under influence from. Each measure can give its first and last {@link Note}, as well as its interval representation.
 * If another measure is provided in a parameter, it can chain the interval representations together.
 */
public class Measure {
    /**
     * The key signature the measure is under influence from
     */
    private KeySig keySig;
    /**
     * This represents our notes. It holds {@link Note} and {@link Chord} objects in the order they were found in the measures.
     */
    private List<Object> noteRep;

    public Measure(KeySig keySig, List<Object> noteRep) {
        this.keySig = keySig;
        this.noteRep = noteRep;
    }

    /**
     * Initializes the intervals array length based on the presence and validity of a {@code nextMeasure}.
     * If {@code nextMeasure} is {@code null} or empty (throws {@link Empty}), the array length is {@code size - 1};
     * otherwise, it is {@code size}.
     *
     * @param nextMeasure the next measure to check for linking intervals; may be {@code null}
     * @param size        the number of notes in the current measure
     * @return an {@code int[]} sized appropriately for the interval representation between notes and optionally linked measure
     */
    private static int[] initializeIntervalArray(Measure nextMeasure, int size) {
        if (nextMeasure == null) {
            return new int[size - 1];
        }

        try {
            nextMeasure.getFirstNoteOfMeasure();
            return new int[size];
        } catch (Empty ex) {
            return new int[size - 1];
        }
    }

    /**
     * @return the first {@link Note note} of the measure, either a note or the first note of a {@link Chord chord}
     */
    public Note getFirstNoteOfMeasure() throws Empty {
        if (noteRep == null || noteRep.isEmpty() || noteRep.get(0) == null) {
            throw new Empty();
        }
        if (noteRep.get(0).getClass() == Note.class) return (Note) noteRep.get(0);
        if (noteRep.get(0).getClass() == Chord.class) return ((Chord) noteRep.get(0)).getTopNote();
        throw new RuntimeException("First note of this measure has unknown class: " + noteRep.get(0).getClass().getName());
    }

    /**
     * @return the last {@link Note note} of the measure, either a note or the first note of a {@link Chord chord}
     */
    public Note getLastNoteOfMeasure() throws Empty {
        if (noteRep == null || noteRep.isEmpty() || noteRep.get(noteRep.size() - 1) == null) throw new Empty();
        int lastPos = noteRep.size() - 1;
        if (noteRep.get(lastPos).getClass() == Note.class) return (Note) noteRep.get(lastPos);
        if (noteRep.get(lastPos).getClass() == Chord.class) return ((Chord) noteRep.get(lastPos)).getTopNote();
        throw new RuntimeException("Last note of this measure has unknown class: " + noteRep.get(lastPos).getClass().getName());
    }

    /**
     * Computes the half-step intervals between consecutive notes within this measure,
     * optionally including the interval between this measure's last note and the first note
     * of the provided {@code nextMeasure}.
     *
     * <p>The intervals are calculated as distances in half-steps between {@link Note notes} or {@link Chord chords}.
     * For chords, only the top note is considered. Notes are adjusted according to the
     * {@link KeySig key signature} if they lack accidentals.
     *
     * @param nextMeasure the next {@link Measure} to link intervals with; may be {@code null} if no linking is desired
     * @return an array of half-step distances representing intervals between notes in this measure,
     * including the interval to {@code nextMeasure}'s first note if provided and valid
     * @throws Empty if this measure contains no notes or its first note is {@code null}
     */
    public int[] getMeasureInterval(Measure nextMeasure) throws Empty {
        if (noteRep == null || noteRep.isEmpty() || noteRep.get(0) == null) {
            throw new Empty();
        }

        int size = noteRep.size();
        int[] intervals = initializeIntervalArray(nextMeasure, size);

        Note previousNote = null;
        for (int i = 0; i < size; i++) {
            Object obj = noteRep.get(i);
            Note currentNote;

            if (obj.getClass() == Note.class) {
                currentNote = (Note) obj;
            } else if (obj.getClass() == Chord.class) {
                currentNote = ((Chord) obj).getTopNote();
            } else {
                throw new RuntimeException("Unexpected object type at position " + i + ": " + obj.getClass().getName());
            }

            if (currentNote.getAccidental() == null) {
                String keySiggedPitch = keySig.getKeySiggedPitch(currentNote.getPitch());
                if (keySiggedPitch.length() > 1) {
                    String accidental = String.valueOf(keySiggedPitch.charAt(1));
                    currentNote = currentNote.applyAccidental(accidental);
                }
            }

            if (i != 0) {
                intervals[i - 1] = previousNote.getHalfStepDistance(currentNote);
            }
            previousNote = currentNote;
        }

        if (nextMeasure != null) {
            Note nextFirstNote = nextMeasure.getFirstNoteOfMeasure();
            intervals[intervals.length - 1] = this.getLastNoteOfMeasure().getHalfStepDistance(nextFirstNote);
        }

        return intervals;
    }

    public KeySig getKeySig() {
        return keySig;
    }

    public void setKeySig(KeySig keySig) {
        this.keySig = keySig;
    }

    public List<Object> getNoteRep() {
        return noteRep;
    }

    public void setNoteRep(List<Object> noteRep) {
        this.noteRep = noteRep;
    }

    @Override
    public String toString() {
        return "Measure{" +
                ", keySig=" + keySig +
                ", noteRep=" + noteRep +
                '}';
    }
}
