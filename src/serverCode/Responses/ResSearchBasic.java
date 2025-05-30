package serverCode.Responses;

import java.util.List;

/**
 * This class used to be the response object for the /searchMusic endpoint.
 *
 * @deprecated
 */
public class ResSearchBasic extends BASE_RESPONSE {
    List<String> names;

    public ResSearchBasic(String message, boolean success, List<String> names) {
        super(message, success);
        this.names = names;
    }

    public ResSearchBasic(List<String> names) {
        this(null, true, names);
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }
}
