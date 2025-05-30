package music;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the KeySignatures in music. It has a majSig enum and a minSig enum, representing both Major and
 * Minor keys. It has a map of 'keyed pitches' that translate the C major pitches to the pitches of the proper key. Its
 * internal keySig is a major, but it can return the relative minor if requested.
 */
public class KeySig {

    /**
     * Has every pitch, and its corresponding 'keyed pitch', i.e. the key of C maj will translate this map to <pitch, pitch> for every pitch.
     * However, the key of F maj will be the same except for the pitch B, i.e. <B, Bf>. TODO perhaps make this so it is just the accidental instead of the note, i.e. 'null' or 's' or 'x' corresponding to each pitch.
     */
    public Map<String, String> keyedPitches;
    /**
     * Our major key signature for this particular instance of the key
     */
    private majSig sig = null;

    public KeySig(String signatureString) {
        this.sig = translateString(signatureString);
        fillPitchMap();
    }

    /**
     * Occasionally composers will want a custom key, like a single E# or C#
     *
     * @param customKey the map of pitches to accidentals as they appear in the mei element
     *                  pname=e accid=s -> <"e", "s">
     */
    public KeySig(Map<String, String> customKey) {
        addCNotes(); // Initialize the map with the key of C
        for (Map.Entry<String, String> entry : customKey.entrySet()) {
            String pitch = entry.getKey().toUpperCase();
            String accid = entry.getValue().toLowerCase();
            if (accid.equals("n")) continue;
            keyedPitches.replace(pitch, pitch + accid);
        }
    }

    public String getKeySiggedPitch(String basePitch) {
        return keyedPitches.get(basePitch);
    }

    public majSig getSig() {
        return sig;
    }

    /**
     * This takes our new key signature and fills the keyedPitch map appropriately.
     */
    private void fillPitchMap() {
        switch (sig) {
            case C -> addCNotes();
            case G -> addGNote();
            case D -> addDNote();
            case A -> addANote();
            case E -> addENote();
            case B -> addBNote();
            case Fs -> addFsNote();
            case Cs -> addCsNote();
            case F -> addFNote();
            case Bf -> addBfNote();
            case Ef -> addEfNote();
            case Af -> addAfNote();
            case Df -> addDfNote();
            case Gf -> addGfNote();
            case Cf -> addCfNote();
        }
    }

    /**
     * Takes the mei string rep of the key Sig and turns it into our enum
     *
     * @param s the string, formatted as xs or xf where x is the number of flats or sharps
     * @return the majSig representation of the amount of sharps or flats
     */
    private majSig translateString(String s) {
        return switch (s) {
            case "0" -> majSig.C;
            case "1s" -> majSig.G;
            case "2s" -> majSig.D;
            case "3s" -> majSig.A;
            case "4s" -> majSig.E;
            case "5s" -> majSig.B;
            case "6s" -> majSig.Fs;
            case "7s" -> majSig.Cs;
            case "1f" -> majSig.F;
            case "2f" -> majSig.Bf;
            case "3f" -> majSig.Ef;
            case "4f" -> majSig.Af;
            case "5f" -> majSig.Df;
            case "6f" -> majSig.Gf;
            case "7f" -> majSig.Cf;
            default -> throw new IllegalStateException("Unexpected value: " + s);
        };
    }

    /**
     * This function takes in  a major keySig and returns its corresponding natural minor key.
     *
     * @param sig the major keySig
     * @return a minor keySig
     */
    public minSig majToMinSig(majSig sig) {
        return switch (sig) {
            case C -> minSig.A;
            case G -> minSig.E;
            case D -> minSig.B;
            case A -> minSig.Fs;
            case E -> minSig.Cs;
            case B -> minSig.Gs;
            case Fs -> minSig.Ds;
            case Cs -> minSig.As;
            case F -> minSig.D;
            case Bf -> minSig.G;
            case Ef -> minSig.C;
            case Af -> minSig.F;
            case Df -> minSig.Bf;
            case Gf -> minSig.Ef;
            case Cf -> minSig.Af;
        };
    }

    /**
     * This is setting the map to the key of C
     */
    private void addCNotes() {
        keyedPitches = new HashMap<>(Map.of("A", "A", "B", "B", "C", "C", "D", "D", "E", "E", "F", "F", "G", "G"));
    }

    /**
     * For the G major key, the F is sharped, and we initialize the map
     */
    private void addGNote() {
        addCNotes();
        keyedPitches.replace("F", "Fs");
    }

    /**
     * For the D major key, the C is sharped, and we add the previous sharps
     */
    private void addDNote() {
        addGNote();
        keyedPitches.replace("C", "Cs");
    }

    /**
     * For the A major key, the G is sharped, and we add the previous sharps
     */
    private void addANote() {
        addDNote();
        keyedPitches.replace("G", "Gs");
    }

    /**
     * For the E major key, the D is sharped, and we add the previous sharps
     */
    private void addENote() {
        addANote();
        keyedPitches.replace("D", "Ds");
    }

    /**
     * For the B major key, the A is sharped, and we add the previous sharps
     */
    private void addBNote() {
        addENote();
        keyedPitches.replace("A", "As");
    }

    /**
     * For the Fs major key, the E is sharped, and we add the previous sharps
     */
    private void addFsNote() {
        addBNote();
        keyedPitches.replace("E", "Es");
    }

    /**
     * For the Cs major key, the B is sharped, and we add the previous sharps
     */
    private void addCsNote() {
        addFsNote();
        keyedPitches.replace("B", "Bs");
    }

    /**
     * For the F major key, the B is flatted, and we initialize the map
     */
    private void addFNote() {
        addCNotes();
        keyedPitches.replace("B", "Bf");
    }

    /**
     * For the Bf major key, the E is flatted, and we add the previous flats
     */
    private void addBfNote() {
        addFNote();
        keyedPitches.replace("E", "Ef");
    }

    /**
     * For the Ef major key, the A is flatted, and we add the previous flats
     */
    private void addEfNote() {
        addBfNote();
        keyedPitches.replace("A", "Af");
    }

    /**
     * For the Af major key, the D is flatted, and we add the previous flats
     */
    private void addAfNote() {
        addEfNote();
        keyedPitches.replace("D", "Df");
    }

    /**
     * For the Df major key, the G is flatted, and we add the previous flats
     */
    private void addDfNote() {
        addAfNote();
        keyedPitches.replace("G", "Gf");
    }

    /**
     * For the Gf major key, the C is flatted, and we add the previous flats
     */
    private void addGfNote() {
        addDfNote();
        keyedPitches.replace("C", "Cf");
    }

    /**
     * For the Cf major key, the F is flatted, and we add the previous flats
     */
    private void addCfNote() {
        addGfNote();
        keyedPitches.replace("F", "Ff");
    }

    @Override
    public String toString() {
        return "KeySig{" +
                "sig=" + sig +
                '}';
    }

    /**
     * This enum covers all major keys.
     */
    public enum majSig {
        C,
        G,
        D,
        A,
        E,
        B,
        Fs,
        Cs,
        F,
        Bf,
        Ef,
        Af,
        Df,
        Gf,
        Cf
    }

    /**
     * This enum covers all minor keys.
     */
    public enum minSig {
        A,
        E,
        B,
        Fs,
        Cs,
        Gs,
        Ds,
        As,
        D,
        G,
        C,
        F,
        Bf,
        Ef,
        Af
    }
}

