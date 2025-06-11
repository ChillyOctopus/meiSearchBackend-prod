package serverCode.Services;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * This class is the parent class for all services. Has a 'writeString' method for a String -> OutputStream
 */
public class BASE_SERVICE {
    public static void writeString(String str, OutputStream os) throws IOException {
        OutputStreamWriter sw = new OutputStreamWriter(os);
        sw.write(str);
        sw.flush();
    }
}
