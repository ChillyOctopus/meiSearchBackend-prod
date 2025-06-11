package parsers;

import music.KeySig;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

/**
 * This class only exists to parse {@link KeySig KeySigs}, which the KeySig class handles most of the logic for.
 */
public class KeySigParser {

    /**
     * Takes a key sig element and returns a new KeySig class based on the signature in between the quotations
     *
     * @param keySigElement the element we are making the keySig out of
     * @return a new KeySig class based on the value of the key sig
     */
    public KeySig getKeySigFromElement(Element keySigElement) {
        if (keySigElement == null) {
            throw new IllegalArgumentException("Provided element is not a keySig (null)");
        }
        // If it is named key + sig in any way, it's good. If it has the key.sig, keysig, or keyAccid attribute, it's good.
        String tagName = keySigElement.getTagName().toLowerCase();
        if (! (tagName.contains("key") && tagName.contains("sig"))) {
            if (! (keySigElement.hasAttribute("key.sig") || keySigElement.hasAttribute("keysig") || keySigElement.hasAttribute("keyAccid"))) {
                throw new IllegalArgumentException("Provided element is not a keySig: "+DocumentParser.elementToString(keySigElement));
            }
        }

        String sigValue = getNonEmptySigAttribute(keySigElement);

        if (!sigValue.isEmpty()) {
            return new KeySig(sigValue);
        }

        // This handles custom keys that may happen. Non-standard keys have the 'keyAccid' form
        NodeList accidList = keySigElement.getElementsByTagName("keyAccid");
        if (accidList.getLength() == 0) {
            throw new IllegalArgumentException("keySig element malformed: " + DocumentParser.elementToString(keySigElement));
        }

        Map<String, String> customKey = new HashMap<>();
        for (int i = 0; i < accidList.getLength(); i++) {
            Element accidElem = (Element) accidList.item(i);
            String pname = accidElem.getAttribute("pname");
            String accid = accidElem.getAttribute("accid");

            if (pname.isEmpty() || accid.isEmpty()) {
                throw new IllegalArgumentException("keyAccid element malformed: " + accidElem);
            }

            customKey.put(pname, accid);
        }

        return new KeySig(customKey);
    }

    private String getNonEmptySigAttribute(Element keySigElement) {
        String sig = keySigElement.getAttribute("sig");
        if (!sig.isEmpty()) return sig;
        String keysig = keySigElement.getAttribute("keysig");
        if (!keysig.isEmpty()) return keysig;
        String keyDotSig = keySigElement.getAttribute("key.sig");
        if (!keyDotSig.isEmpty()) return keyDotSig;
        return null;
    }
}
