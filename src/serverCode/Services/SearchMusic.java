package serverCode.Services;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import serverCode.Requests.ReqSearchMusic;
import serverCode.Responses.ResSearchMusic;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import music.Document;
import parsers.DocumentParser;
import workers.*;
import workers.Record;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * This class is responsible for all searching done on the {@link ElasticProcessor} backend.
 */
public class SearchMusic extends BASE_SERVICE{
    /**
     * Executes a music search against {@link ElasticProcessor} using interval representation
     * extracted from MEI data using {@link DocumentParser}. The MEI chunk is written to a temporary file, parsed,
     * and then searched using logical operators and a phrase query.
     *
     * @param request the {@link ReqSearchMusic} object containing MEI content and logical filters
     * @return a {@link ResSearchMusic} object containing the search results or an error
     *
     * @throws IOException if an error occurs writing the MEI chunk to a temporary file
     */
    public ResSearchMusic search(ReqSearchMusic request) throws IOException {
        File tempFile = new File("/tmp/temp.txt");
        if (tempFile.exists()) {
            tempFile.delete();
        }

        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            writeString(request.getMeiChunk(), outputStream);
        }

        DocumentParser documentParser = new DocumentParser();
        documentParser.setInFile(tempFile);
        Document parsedDocument = documentParser.getDocumentFromFile();

        String intervalString = convertIntervalsToString(parsedDocument.getIntervalRep());

        ElasticProcessor elasticProcessor = new ElasticProcessor();
        SearchResponse<Record> searchResponse;

        try {
            searchResponse = elasticProcessor.advancedPhraseQuery(
                    intervalString,
                    request.getAndMap(),
                    request.getOrMap(),
                    request.getNotMap(),
                    0,
                    "fvh",
                    0.0,
                    10_000
            );
        } catch (ElasticsearchException ex) {
            return new ResSearchMusic(ex.getMessage(), false);
        }

        return new ResSearchMusic(convertHitListToArray(searchResponse.hits().hits()));
    }

    /**
     * Converts an integer array representing musical intervals into a space-separated {@link String}.
     *
     * @param intervals the array of intervals
     * @return a formatted {@link String} of interval values
     */
    private String convertIntervalsToString(int[] intervals) {
        if (intervals.length == 0) return "";
        String intervalStr = Arrays.toString(intervals);
        intervalStr = intervalStr.replace("[", "")
                .replace("]", "")
                .replace(",", "");
        return intervalStr;
    }

    /**
     * Converts a {@link List} of {@link Hit} objects to a raw array.
     *
     * @param hitList the list of hits
     * @return an array of {@link Hit} objects
     */
    private Hit<Record>[] convertHitListToArray(List<Hit<Record>> hitList) {
        @SuppressWarnings("unchecked")
        Hit<Record>[] hitArray = new Hit[hitList.size()];
        for (int i = 0; i < hitList.size(); i++) {
            hitArray[i] = hitList.get(i);
        }
        return hitArray;
    }
}
