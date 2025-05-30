package serverCode.Responses;

/**
 * This class is the response object for the /removeMusic endpoint
 */
public class ResRemoveMusic extends BASE_RESPONSE {
    String elasticResponse;

    public ResRemoveMusic(String message, boolean success) {
        super(message, success);
    }

    public ResRemoveMusic(String message, boolean success, String elasticResponse) {
        super(message, success);
        this.elasticResponse = elasticResponse;
    }

    public String getElasticResponse() {
        return elasticResponse;
    }

    public void setElasticResponse(String elasticResponse) {
        this.elasticResponse = elasticResponse;
    }
}
