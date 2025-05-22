package parsers;

import music.Note;

/**
 * This class takes in a simple note string of mei format and parses it into our {@link Note} objects.
 */
public class NoteParser extends Base_Parser {

    /**
     * Uses previous functions to pull values from the mei and make a note
     * @param meiNoteChunk the string we are pulling pitch, accidental, and octave from
     * @return A {@link Note} that represents the mei string.
     */
    public Note getNoteFromMei(String meiNoteChunk){
        return new Note(getPitchFromMei(meiNoteChunk), getAccidentalsFromMei(meiNoteChunk), getOctaveFromMei(meiNoteChunk), getDurationFromMei(meiNoteChunk));
    }

    /**
     * This function takes in a mei note string and returns a pitch name
     * @param meiNoteChunk the string in mei format: "<note> ... </note>"
     * @return the char of the pitch, 'a' through 'g'.
     */
    public char getPitchFromMei(String meiNoteChunk){
        return getQuotedInTags(" pname=", meiNoteChunk).getData().charAt(0);
    }

    /**
     * This function takes in a mei note string and returns its accidental
     * @param meiNoteChunk the string in mei format: "<note> ... </note>"
     * @return the string of the accidental found, or null for nothing.
     */
    public String getAccidentalsFromMei(String meiNoteChunk) {
        ParsedData parData = getQuotedInTags(" accid", meiNoteChunk);
        //Sometimes we won't have accidentals in which case just return null
        return (parData.isFound()) ? parData.getData() : null;
    }

    /**
     * This function returns the octave integer of the note
     * @param meiNoteChunk the string in mei format: "<note> ... </note>"
     * @return the int of the octave that we found based on piano pitch, 0, 1, 2, ect.
     */
    public int getOctaveFromMei(String meiNoteChunk){
        return Integer.parseInt(getQuotedInTags("oct=", meiNoteChunk).getData());
    }

    /**
     * This function gets the total duration of a note, factoring in the dots
     * @param meiNoteChunk the note chunk we are extracting the information from
     * @return the total duration
     */
    public Float getDurationFromMei(String meiNoteChunk){
        Integer dur = getRawDurationFromMei(meiNoteChunk);
        if(dur == null) return null;
        int dots = getDotsFromMei(meiNoteChunk);
        return (dots == 0) ? dur : dur * (1+getDotValueAdded(dots));
    }

    /**
     * This function takes in a mei note string and returns the raw duration, which is how many fit in a measure
     * @param meiNoteChunk the string in mei format: "<note> ... </note>"
     * @return the duration encoded
     */
    private Integer getRawDurationFromMei(String meiNoteChunk){
        ParsedData pd = getQuotedInTags(" dur=", meiNoteChunk);
        Integer integer = null;
        try{integer = (pd.isFound()) ? Integer.parseInt(pd.getData()) : null;}catch (NumberFormatException ex){System.out.println(ex.getMessage()+" on "+pd);}
        return integer;
    }

    /**
     * This function takes in a mei note string and returns the number of dots, if any
     * @param meiNoteChunk the string in mei format: "<note> ... </note>"
     * @return the number of dots
     */
    private int getDotsFromMei(String meiNoteChunk){
        ParsedData parData = getQuotedInTags(" dots=", meiNoteChunk);
        //Sometimes we won't have dots in which case just return 0
        return (parData.isFound()) ? Integer.parseInt(parData.getData()) : 0;
    }

    /**
     * This returns the value added by subsequent dots. Dots = 2^1 - 1 / 2^1, i.e. 1 dot = 1/2, 2 dots = 3/4, ect, 3 dots = 7/8, ect
     * @param dots the number of dots the note has
     * @return the float to add the duration of the note
     */
    private float getDotValueAdded(int dots){
        return ((float) ((2 ^ dots) - 1) / (2^dots));
    }
}
