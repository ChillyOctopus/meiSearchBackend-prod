package serverCode.Responses;

import java.util.List;

/**
 * This class is the response object for the /partialSheetMusic endpoint
 */
public class ResPartialSheetMusic extends BASE_RESPONSE {

    List<String> partialMusic;
    String meiSkeleton;

    public ResPartialSheetMusic(String message, boolean success) {
        super(message, success);
    }

    public ResPartialSheetMusic(String message, boolean success, List<String> partialMusic, String meiSkeleton) {
        super(message, success);
        this.partialMusic = partialMusic;
        this.meiSkeleton = meiSkeleton;
    }

    public List<String> getPartialMusic() {
        return partialMusic;
    }

    public void setPartialMusic(List<String> partialMusic) {
        this.partialMusic = partialMusic;
    }

    public String getMeiSkeleton() {
        return meiSkeleton;
    }

    public void setMeiSkeleton(String meiSkeleton) {
        this.meiSkeleton = meiSkeleton;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MeiSkeleton:");
        sb.append(getMeiSkeleton());
        for (String s : partialMusic){
            sb.append("<!__________________________________________________________________________________________________>\n");
            sb.append(s);
        }
        sb.append("<!__________________________________________________________________________________________________>\n");
        return "ResPartialSheetMusic{" +
                "\nmessage='" + message + '\'' +
                "\nsuccess=" + success +
                "\npartialMusic(" + partialMusic.size() + " sequence(s) found)=\n" + sb +
                '}';
    }
}
