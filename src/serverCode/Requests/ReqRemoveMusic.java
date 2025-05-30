package serverCode.Requests;

/**
 * This class is the request object for the /removeMusic endpoint
 */
public class ReqRemoveMusic {
    String name;

    public ReqRemoveMusic(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
