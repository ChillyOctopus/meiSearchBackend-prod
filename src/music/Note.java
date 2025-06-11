package music;

import java.util.Objects;

/**
 * This class represents our basic Note object. It has a pitch, an accidental, a duration, an octave, an intVal, and a pitch pos.
 * When given another note, it gives the half step distance between itself and the other note starting from itself
 */
public class Note {

    /**
     * The basic pitch of this note, before any accidentals are added
     */
    private final String pitch;

    /**
     * The accidental of this note as found in the mei string. Is null if nothing is found.
     */
    private final String accidental;

    /**
     * The octave that the note is in, typically from 0 (the lowest A to the next B) to 8 (the last C)
     */
    private final int octave;

    /**
     * The duration of this note, or how many of this note in particular would fit in a measure. Dots are accounted for in this number
     */
    private final Float duration;

    /**
     * 'i' means initial, 'm' means intermediate, and 't' means terminal. TODO: currently unused.
     */
    private final String tieState = null;

    /**
     * The position of the note in any given octave, from 0 (C) to 11 (B)
     */
    private final int pitchPos;

    /**
     * The position of the note on the 88-key keyboard, typically from 0 (the lowest A) to 87 (the highest C)
     */
    private final int intVal;

    /**
     * The constructor
     *
     * @param pitch      the pitch is the char that represents the base pitch without accidentals
     * @param accidental the accidental, could be null.
     * @param octave     the octave the note is in. This is ALWAYS correct due to exporting reliability, even in the case of B sharps (increasing an octave) or C flats (decreasing an octave)
     * @param duration   the duration of the note, occasionally this is found inside the chord or this note may be a grace note, in which case this is null.
     */
    public Note(char pitch, String accidental, int octave, Float duration) {
        this.accidental = accidental;
        if (accidental != null) {
            this.pitchPos = makePitchPos(String.valueOf(pitch).toUpperCase() + this.accidental);
        } else {
            this.pitchPos = makePitchPos(String.valueOf(pitch).toUpperCase());
        }
        this.pitch = String.valueOf(pitch).toUpperCase();
        this.octave = octave;
        this.intVal = octave * 12 + this.pitchPos;
        this.duration = duration;
    }

    /**
     * The constructor
     *
     * @param pitch      the pitch is the char that represents the base pitch without accidentals
     * @param accidental the accidental, could be null.
     * @param octave     the octave the note is in. This is always correct due to exporting reliability, even in the case of B sharps (increasing an octave) or C flats (decreasing an octave)
     */
    public Note(char pitch, String accidental, int octave) {
        this(pitch, accidental, octave, null);
    }

    /**
     * When given a note, returns the half steps from itself to the given note.
     *
     * @param n The note we are given
     * @return the distance to the note in half steps
     */
    public int getHalfStepDistance(Note n) {
        return (n.getIntVal() - this.getIntVal());
    }

    /**
     * When given a note, returns the ratio of its own duration to the given notes. TODO currently unused.
     *
     * @param n The note we are given
     * @return the ration of duration to the note
     */
    public Float getDurationRatio(Note n) {
        return (n.getDuration() / this.getDuration());
    }

    /**
     * This is a function to return the int note values in an octave given the pitch and accidental
     *
     * @param s the string we are inputting, a combination of 'pitch'+'accidental'.
     * @return an integer representing its place in the octave
     */
    private int makePitchPos(String s) {
        return switch (s) {
            case "A", "An", "Gx", "Gss", "Bff" -> 9;
            case "As", "Bf", "Cff" -> 10;
            case "B", "Bn", "Ax", "Ass", "Cf" -> 11;
            case "C", "Cn", "Bs", "Dff" -> 0;
            case "Cs", "Bx", "Bss", "Df" -> 1;
            case "D", "Dn", "Cx", "Css", "Eff" -> 2;
            case "Ds", "Ef", "Fff" -> 3;
            case "E", "En", "Dx", "Dss", "Ff" -> 4;
            case "F", "Fn", "Es", "Gff" -> 5;
            case "Fs", "Gf", "Ex", "Ess" -> 6;
            case "G", "Gn", "Fx", "Fss", "Aff" -> 7;
            case "Gs", "Af" -> 8;
            default -> throw new RuntimeException("Unknown note value: " + s);
        };
    }

    /**
     * This function "applies" a new accidental to a Note, ensuring we get the right one back
     *
     * @param accidental the accidental in question we are applying
     * @return a new Note with the accidental configured
     */
    public Note applyAccidental(String accidental) {
        Note newNote = new Note(this.pitch.charAt(0), accidental, this.octave, this.duration);

        int difference = newNote.getPitchPos() - this.pitchPos;
        // If the following is true, then the new note jumped an octave up, i.e. we made C into Cb
        if (difference >= 6) return new Note(this.pitch.charAt(0), accidental, this.octave - 1, this.duration);
        // If the following is true, then the new note dropped an octave down, i.e. we made B into Bs
        if (difference <= -6) return new Note(this.pitch.charAt(0), accidental, this.octave + 1, this.duration);
        // Otherwise it was done correctly
        return newNote;
    }

    public String getPitch() {
        return pitch;
    }

    public String getAccidental() {
        return accidental;
    }

    public int getOctave() {
        return octave;
    }

    public Float getDuration() {
        return duration;
    }

    public int getIntVal() {
        return intVal;
    }

    public int getPitchPos() {
        return pitchPos;
    }

    @Override
    public String toString() {
        return "Note{" +
                "pitch='" + pitch + '\'' +
                ", accidental='" + accidental + '\'' +
                ", octave=" + octave +
                ", duration='" + duration + '\'' +
                ", intVal=" + intVal +
                ", pitchPos=" + pitchPos +
                '}';
    }

    /**
     * Equals function checks the class, pitchPos, and duration
     *
     * @param obj the object we cast to Note and check against
     * @return true if equal, false if not
     */
    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) return false;
        Note compMe = (Note) obj;
        return (this.getPitchPos() == compMe.getPitchPos() &&
                Objects.equals(this.getDuration(), compMe.getDuration()));

    }
}
