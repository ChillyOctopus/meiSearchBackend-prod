package serverCode.Responses;

/**
 * This class is the response object for the /addMusic endpoint
 */
public class ResAddMusic extends BASE_RESPONSE {
    String elasticResponse;

    public ResAddMusic(String message, boolean success, String elasticResponse) {
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
