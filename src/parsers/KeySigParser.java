package parsers;

import music.KeySig;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

/**
 * This class soley exists to parse {@link KeySig KeySigs}, which the KeySig class handles most of the logic for.
 */
public class KeySigParser {

    /**
     * Takes a key sig element and returns a new KeySig class based on the signature in between the quotations
     *
     * @param keySigElement the element we are making the keySig out of
     * @return a new KeySig class based on the value of the key sig
     */
    public KeySig getKeySigFromElement(Element keySigElement) {
        if (keySigElement == null || !"keySig".equals(keySigElement.getTagName())) {
            throw new IllegalArgumentException("Provided element is not a keySig: " + keySigElement);
        }

        String sigValue = keySigElement.getAttribute("sig");

        if (!sigValue.isEmpty()) {
            return new KeySig(sigValue);
        }

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
}
