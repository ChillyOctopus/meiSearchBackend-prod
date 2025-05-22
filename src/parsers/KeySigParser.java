package parsers;

import music.KeySig;

/**
 * This class soley exists to parse {@link KeySig KeySigs}, which the KeySig class handles most of the logic for.
 */
public class KeySigParser extends Base_Parser{

    /**
     * Takes a key sig string and returns a new KeySig class based on the signature in between the quotations
     * @param keySigMei the string we are making the keySig out of
     * @return a new KeySig class based on the value of the key sig
     */
    public KeySig getKeySig(String keySigMei){
        //To cover 'key.sig', 'keySig sig', and 'keySig', we use 'ig='
        return new KeySig(getQuotedInTags("ig=", keySigMei).getData());
    }
}
