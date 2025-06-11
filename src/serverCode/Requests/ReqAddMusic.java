package serverCode.Requests;

/**
 * This class is the request object for the /addMusic endpoint
 */
public class ReqAddMusic {

    String fileName;
    String fileContents;
    String file_id;

    public ReqAddMusic(String fileName, String fileContents, String file_id) {
        this.fileName = fileName;
        this.fileContents = fileContents;
        this.file_id = file_id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileContents() {
        return fileContents;
    }

    public void setFileContents(String fileContents) {
        this.fileContents = fileContents;
    }

    public String getFile_id() {
        return file_id;
    }

    public void setFile_id(String file_id) {
        this.file_id = file_id;
    }
}
