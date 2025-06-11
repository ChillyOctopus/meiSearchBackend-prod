package serverCode.Responses;

/**
 * This class is the parent class for all responses - it has a String message and a bool success.
 */
public class BASE_RESPONSE {
    public String message;
    public boolean success;

    public BASE_RESPONSE(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
