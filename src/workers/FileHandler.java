package workers;

import music.Document;
import parsers.DocumentParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Handles file and directory management related to the indexing of MEI files.
 * This includes directory initialization, backups, file parsing, and writing processed output.
 * Usually used in dev for testing.
 */
public class FileHandler {

    private final String pathToDirectory = System.getenv("FILE_BASE_PATH") != null
            ? System.getenv("FILE_BASE_PATH")
            : "/app/files";

    private final String newInputDirSuffix = "/new_input_mei_files";
    private final String oldInputDirSuffix = "/old_input_mei_files";
    private final String backupInputDirSuffix = "/backup_of_new_mei_input_files";
    private final String intervalDirSuffix = "/interval_files";

    private final File mainDirectory = new File(pathToDirectory);
    private final File newInputDirectory = new File(mainDirectory.getAbsolutePath() + newInputDirSuffix);
    private final File oldInputDirectory = new File(mainDirectory.getAbsolutePath() + oldInputDirSuffix);
    private final File backupInputDirectory = new File(mainDirectory.getAbsolutePath() + backupInputDirSuffix);
    private final File intervalDirectory = new File(mainDirectory.getAbsolutePath() + intervalDirSuffix);

    private final DocumentParser documentParser = new DocumentParser();

    /**
     * A basic write string method
     *
     * @param str the string we are writing
     * @param os  the {@link OutputStream} we are writing to
     * @throws IOException when we encounter an io problem
     */
    public static void writeString(String str, OutputStream os) throws IOException {
        OutputStreamWriter sw = new OutputStreamWriter(os);
        sw.write(str);
        sw.flush();
    }

    /**
     * Processes and indexes MEI input files. Ensures required directories exist, makes backups,
     * parses documents, indexes them into Elasticsearch, and writes interval files.
     *
     * @throws IOException if file I/O operations fail
     */
    public void makeIndexDocuments() throws IOException {
        validateDirectories();

        ElasticProcessor elasticProcessor = new ElasticProcessor();
        elasticProcessor.createMusicIndex();

        File[] inputFiles = newInputDirectory.listFiles();
        if (inputFiles == null) throw new IOException("Failed to list files in " + newInputDirectory.getAbsolutePath());

        for (final File file : inputFiles) {
            Path backupPath = Paths.get(backupInputDirectory.getAbsolutePath(), file.getName());
            Files.copy(file.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);

            documentParser.setInFile(file);
            Document parsedDoc = documentParser.getDocumentFromFile();

            Record record = new Record(
                    file.getName(),
                    parsedDoc.getIntervalRep(),
                    parsedDoc.getMeasureMap(),
                    null,
                    parsedDoc.getMetadata()
            );

            elasticProcessor.indexRecord(record);
            writeRecordToFile(record);

            // Uncomment for production:
            // Files.move(file.toPath(), Paths.get(oldInputDirectory.getAbsolutePath(), file.getName()));
        }

        // Uncomment for production:
        // cleanDirectory(newInputDirectory);
    }

    /**
     * Writes the interval representation of a Record to a text file.
     *
     * @param record the record whose interval data should be written
     * @throws IOException if writing to file fails
     */
    void writeRecordToFile(Record record) throws IOException {
        File outputFile = new File(intervalDirectory, record.getName() + "_intervals.txt");
        if (outputFile.exists()) outputFile.delete();
        outputFile.createNewFile();

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            writeString(record.getIntervals_text(), fos);
        }
    }

    /**
     * Checks if required directories are present, creates the directories we can if they are not found.
     *
     * @throws IOException If we fail to either create a directory or find a required one we can't create.
     */
    private void validateDirectories() throws IOException {
        if (!newInputDirectory.exists()) {
            throw new IOException("Required directory not found: " + newInputDirSuffix + " inside " + mainDirectory.getAbsolutePath());
        }

        if (oldInputDirectory.exists()) {
            cleanDirectory(oldInputDirectory);
        } else if (!oldInputDirectory.mkdir()) {
            throw new IOException("Failed to create directory: " + oldInputDirSuffix + " inside " + mainDirectory.getAbsolutePath());
        }

        if (!backupInputDirectory.exists() && !backupInputDirectory.mkdir()) {
            throw new IOException("Failed to create directory: " + backupInputDirSuffix + " inside " + mainDirectory.getAbsolutePath());
        }

        if (!intervalDirectory.exists() && !intervalDirectory.mkdir()) {
            throw new IOException("Failed to create directory: " + intervalDirSuffix + " inside " + mainDirectory.getAbsolutePath());
        }
    }

    /**
     * A simple function to delete a directories files
     *
     * @param directory the {@link File} object we are clearing out
     * @throws IOException when we have an issue with the files.
     */
    private void cleanDirectory(File directory) throws IOException {
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (!file.delete()) throw new IOException();
        }
    }
}
