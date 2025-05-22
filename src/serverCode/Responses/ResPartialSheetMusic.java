package serverCode.Responses;

import java.util.List;

/**
 * This class is the response object for the /partialSheetMusic endpoint
 */
public class ResPartialSheetMusic extends BASE_RESPONSE{

    List<String> partialMusic;

    public ResPartialSheetMusic(String message, boolean success) {
        super(message, success);
    }

    public ResPartialSheetMusic(String message, boolean success, List<String> partialMusic) {
        super(message, success);
        this.partialMusic = partialMusic;
    }

    public List<String> getPartialMusic() {
        return partialMusic;
    }

    public void setPartialMusic(List<String> partialMusic) {
        this.partialMusic = partialMusic;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(String s : partialMusic) sb.append("<!__________________________________________________________________________________________________>\n").append(s).append("<!__________________________________________________________________________________________________>\n");
        return "ResPartialSheetMusic{" +
                "\nmessage='" + message + '\'' +
                "\nsuccess=" + success +
                "\npartialMusic("+partialMusic.size()+" sequence(s) found)=\n" + sb +
                '}';
    }
}
