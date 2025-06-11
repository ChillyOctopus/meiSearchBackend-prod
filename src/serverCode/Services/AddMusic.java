package serverCode.Services;

import music.Document;
import parsers.DocumentParser;
import serverCode.Requests.ReqAddMusic;
import serverCode.Responses.ResAddMusic;
import workers.ElasticProcessor;
import workers.Record;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

/**
 * Service responsible for adding a new music document.
 * Accepts a request with file data, saves it temporarily, parses the document, and indexes it using ElasticSearch.
 */
public class AddMusic extends BASE_SERVICE {

    /**
     * Adds a music document to the system by saving the input file, parsing it into a structured document,
     * and indexing it in {@link ElasticProcessor}.
     *
     * @param request A {@link ReqAddMusic} containing file name, contents, and related metadata.
     * @return A {@link ResAddMusic} indicating success or failure, with an optional message.
     * @throws IOException if there is an issue creating or writing to the temporary file.
     */
    public ResAddMusic add(ReqAddMusic request) throws IOException {
        String normalizedFileName = Normalizer.normalize(request.getFileName(), Normalizer.Form.NFC);
        File tempFile = new File(normalizedFileName);

        if (tempFile.exists()) {
            tempFile.delete(); // Returns false if it DNE, which is the desired behaviour
        }

        boolean created = tempFile.createNewFile();
        if (!created) {
            return new ResAddMusic("Failed to create temporary file.", false, null);
        }

        if (!writeFileContents(tempFile, request.getFileContents())) {
            return new ResAddMusic("Unknown error when trying to write new file.", false, null);
        }

        try {
            System.out.println("Parsing file: " + request.getFileName());
            DocumentParser parser = new DocumentParser();
            parser.setInFile(tempFile);

            Document document = parser.getDocumentFromFile();
            Record record = new Record(
                    request.getFileName(),
                    document.getIntervalRep(),
                    document.getMeasureMap(),
                    request.getFile_id(),
                    document.getMetadata()
            );

            ElasticProcessor processor = new ElasticProcessor();
            String indexResult = processor.indexRecord(record).toString();

            return new ResAddMusic(
                    null,
                    true,
                    "Intervals: {" + record.getIntervals_text() + "} " + indexResult
            );

        } catch (IOException e) {
            return new ResAddMusic("IO Exception: Couldn't index record: " + e, false, e.getMessage());
        } catch (Exception e) {
            return new ResAddMusic("Catch-all Exception: Couldn't index record: " + e, false, e.getMessage());
        } finally {
            tempFile.delete();
        }
    }

    /**
     * Writes string content to the specified file using UTF-8 encoding.
     *
     * @param file     The file to write to.
     * @param contents The string contents to write.
     * @return true if writing was successful, false otherwise.
     */
    private boolean writeFileContents(File file, String contents) {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(contents.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
