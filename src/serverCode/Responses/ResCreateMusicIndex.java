package serverCode.Responses;

/**
 * This class is the response object for the /createMusicIndex endpoint
 */
public class ResCreateMusicIndex extends BASE_RESPONSE {

    public ResCreateMusicIndex(String message, boolean success) {
        super(message, success);
    }
}
