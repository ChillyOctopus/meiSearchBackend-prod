package workers;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.mapping.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.*;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles all ElasticSearch functionality. It creates and uses a RestClient, pulls creds from the env,
 * and is the 'final destination' of most if not all api calls for searching, indexing, deleting, ect.
 */
public class ElasticProcessor {
    private static final RestClient restClient;
    private static final ElasticsearchTransport transport;
    private static final ElasticsearchClient client;

    private static final String MUSIC_INDEX_NAME = "music";

    private static final Pattern PHRASE_OR_TERM = Pattern.compile("\"([^\"]+)\"|(\\S+)");  // group1=inside quotes, group2=a single token

    static {
        String username = System.getenv("ELASTIC_USER");
        String password = System.getenv("ELASTIC_PASS");
        BasicCredentialsProvider credProv = new BasicCredentialsProvider();
        credProv.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        String host = System.getenv("ELASTIC_HOST");
        int port = Integer.parseInt(System.getenv("ELASTIC_PORT"));
        String scheme = "http";

        restClient = RestClient
                .builder(new HttpHost(host, port, scheme))
                .setHttpClientConfigCallback(hc -> hc
                        .setDefaultCredentialsProvider(credProv)
                        .setDefaultIOReactorConfig(IOReactorConfig.custom()
                                .setIoThreadCount(2)  // Set a smaller thread pool size
                                .build()))
                .build();
        transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        client = new ElasticsearchClient(transport);
    }

    /**
     * Creates a fresh "music" index in Elasticsearch with a predefined mapping for
     * fields such as `name`, `file_id`, `intervals_text`, `measure_map`, and `mei_metadata`.
     * These fields correspond DIRECTLY with the {@link Record} attribute names.
     *
     * <p>This method:
     * <ul>
     *   <li>Deletes the existing "music" index if it exists.</li>
     *   <li>Defines text and keyword properties with appropriate analyzers and term vectors.</li>
     *   <li>Constructs a mapping that supports both static and dynamic fields, particularly
     *       allowing dynamic mapping of metadata fields.</li>
     *   <li>Creates the new index with this mapping.</li>
     * </ul>
     *
     * @throws IOException if there is a failure in communicating with the Elasticsearch cluster.
     */
    public void createMusicIndex() throws IOException {
        deleteExistingIndex(MUSIC_INDEX_NAME);

        // The names of every key in this map need to correspond
        // DIRECTLY with the Record object attribute names
        Property textProperty = createFullTextProperty();
        Map<String, Property> properties = new HashMap<>(Map.of(
                "name", createKeywordProperty(),
                "intervals_text", textProperty,
                "measure_map", textProperty,
                "file_id", createKeywordProperty()
        ));
        // Add dynamic metadata field
        properties.put("mei_metadata", createDynamicObjectProperty());

        // Build mapping
        TypeMapping mapping = new TypeMapping.Builder()
                .dynamic(DynamicMapping.True)
                .properties(properties)
                .build();

        // Create the index
        CreateIndexRequest createRequest = new CreateIndexRequest.Builder()
                .index(MUSIC_INDEX_NAME)
                .mappings(mapping)
                .build();

        client.indices().create(createRequest);

        // Optional: Confirm mapping creation
        // System.out.println("Elastic Mapping: " + client.indices().getMapping().toString());
    }

    /**
     * Deletes the specified index if it exists. Suppresses exception if index is not found.
     */
    private void deleteExistingIndex(String indexName) throws IOException {
        DeleteIndexRequest deleteRequest = new DeleteIndexRequest.Builder()
                .index(indexName)
                .build();
        try {
            client.indices().delete(deleteRequest);
        } catch (ElasticsearchException ex) {
            // Index didn't exist, which is expected
        }
    }

    /**
     * Creates a full-text searchable property with whitespace analyzer and term vectors enabled.
     */
    private Property createFullTextProperty() {
        // https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-analyzers.html
        // https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-tokenizers.html
        // https://www.elastic.co/guide/en/elasticsearch/reference/8.13/term-vector.html

        TextProperty textProperty = new TextProperty.Builder()
                .analyzer("whitespace")
                .termVector(TermVectorOption.WithPositionsOffsets)
                .build();

        return new Property.Builder()
                .text(textProperty)
                .build();
    }

    /**
     * Creates a basic keyword property for exact match fields like name and file_id.
     */
    private Property createKeywordProperty() {
        return new Property.Builder()
                .keyword(new KeywordProperty.Builder().build())
                .build();
    }

    /**
     * Creates a property that allows for dynamic object mapping.
     */
    private Property createDynamicObjectProperty() {
        return new Property.Builder()
                .object(o -> o.dynamic(DynamicMapping.True))
                .build();
    }

    /**
     * Indexes a single {@link Record} into the "music" index in Elasticsearch.
     *
     * <p>The document is indexed using the record's name as the document ID. If the document
     * already exists, it will be updated. If it does not, it will be created.
     *
     * @param record The {@link Record} to index.
     * @return The {@link IndexResponse} from Elasticsearch.
     * @throws IOException If the indexing operation fails due to a network or IO issue.
     */
    public IndexResponse indexRecord(Record record) throws IOException {
        IndexRequest<Record> indexRequest = new IndexRequest.Builder<Record>()
                .index(MUSIC_INDEX_NAME)
                .id(record.getName())
                .document(record)
                .build();

        return client.index(indexRequest);
    }

    /**
     * Removes a document from the "music" index based on its ID.
     *
     * <p>This is typically used to delete a previously indexed {@link Record}
     * using its {@code name} as the identifier.
     *
     * @param name The ID of the document to delete (corresponds to the record's name).
     * @return The {@link DeleteResponse} returned by Elasticsearch.
     * @throws IOException If the delete operation fails due to a network or IO issue.
     */
    public DeleteResponse removeRecord(String name) throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest.Builder()
                .index(MUSIC_INDEX_NAME)
                .id(name)
                .build();

        return client.delete(deleteRequest);
    }

    /**
     * Executes an advanced phrase-based search query against the "music" index.
     *
     * <p>This method constructs a compound query using the provided phrase (on the {@code intervals_text} field)
     * combined with boolean filter conditions derived from the {@code andMap}, {@code orMap}, and {@code notMap},
     * which target subfields within the {@code mei_metadata} object.
     *
     * <p>The search results may optionally include highlighting and are filtered by a minimum score and limited in size.
     *
     * @param phrase       A phrase to match against the {@code intervals_text} field. If null or blank, matches all.
     * @param andMap       A map of metadata fields to terms where all must be matched (AND).
     * @param orMap        A map of metadata fields to terms where any may match (OR).
     * @param notMap       A map of metadata fields to terms that must not match (NOT).
     * @param slop         The slop parameter for the match phrase query.
     * @param highlighter  Optional highlighter type to apply to the {@code intervals_text} field.
     * @param minScore     Minimum score threshold for included results.
     * @param maxResponses Maximum number of results to return. If -1, the server default is used.
     * @return A {@link SearchResponse} containing matching {@link Record} documents.
     * @throws IOException If the search operation fails due to IO or network issues.
     */
    public SearchResponse<Record> advancedPhraseQuery(
            String phrase,
            Map<String, List<String>> andMap,
            Map<String, List<String>> orMap,
            Map<String, List<String>> notMap,
            int slop,
            String highlighter,
            double minScore,
            int maxResponses
    ) throws IOException {
        String phraseField = "intervals_text";
        String metadataField = "mei_metadata";

        Query phraseQuery = (phrase != null && !phrase.isBlank())
                ? new Query.Builder().matchPhrase(new MatchPhraseQuery.Builder()
                .field(phraseField)
                .query(phrase)
                .slop(slop)
                .build()).build()
                : new Query.Builder().matchAll(new MatchAllQuery.Builder().build()).build();

        BoolQuery.Builder filterBuilder = new BoolQuery.Builder();

        if (!andMap.isEmpty()) {
            filterBuilder.filter(buildFilterQuery(metadataField, andMap, BoolClauseType.MUST));
        }

        if (!orMap.isEmpty()) {
            filterBuilder.filter(buildFilterQuery(metadataField, orMap, BoolClauseType.SHOULD));
        }

        if (!notMap.isEmpty()) {
            filterBuilder.filter(buildFilterQuery(metadataField, notMap, BoolClauseType.MUST_NOT));
        }

        Query finalQuery = new Query.Builder().bool(new BoolQuery.Builder()
                .must(phraseQuery)
                .filter(new Query.Builder().bool(filterBuilder.build()).build())
                .build()).build();

        SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
                .index(MUSIC_INDEX_NAME)
                .query(finalQuery)
                .minScore(minScore);

        if (maxResponses != -1 || phrase == null || phrase.isBlank()) {
            requestBuilder.size(maxResponses);
        }

        if (highlighter != null) {
            Highlight highlight = new Highlight.Builder()
                    .fields(phraseField, new HighlightField.Builder().type(highlighter).build())
                    .build();
            requestBuilder.highlight(highlight);
        }

        return client.search(requestBuilder.build(), Record.class);
    }

    /**
     * Builds a nested boolean filter query based on the provided field-value map and clause type.
     *
     * @param metadataField The base metadata object field name (e.g., "mei_metadata").
     * @param fieldMap      A map of subfield names to a list of values.
     * @param clauseType    The type of boolean clause to apply (MUST, SHOULD, MUST_NOT).
     * @return A {@link Query} representing the combined filter logic.
     */
    private Query buildFilterQuery(String metadataField, Map<String, List<String>> fieldMap, BoolClauseType clauseType) {
        BoolQuery.Builder outerBuilder = new BoolQuery.Builder();

        for (Map.Entry<String, List<String>> entry : fieldMap.entrySet()) {
            String key = entry.getKey();
            for (String value : entry.getValue()) {
                if (value == null || value.isBlank()) continue;
                Query innerQuery = new Query.Builder().bool(getAndQueryForValue(metadataField, key, value)).build();
                switch (clauseType) {
                    case MUST -> outerBuilder.must(innerQuery);
                    case SHOULD -> outerBuilder.should(innerQuery);
                    case MUST_NOT -> outerBuilder.mustNot(innerQuery);
                }
            }
        }

        return new Query.Builder().bool(outerBuilder.build()).build();
    }

    /**
     * Constructs a boolean query that requires all terms or phrases in the value string to match.
     *
     * @param metadataField The base metadata field name.
     * @param key           The subfield within the metadata object.
     * @param value         A string containing terms and/or quoted phrases to match.
     * @return A {@link BoolQuery} requiring all parsed components to match.
     */
    public BoolQuery getAndQueryForValue(String metadataField, String key, String value) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        for (String part : splitPhrasesAndTerms(value)) {
            boolQuery.must(buildTermOrPhraseQuery(metadataField, key, part));
        }
        return boolQuery.build();
    }

    /**
     * Splits an input string into individual terms and quoted phrases.
     *
     * @param value The raw input string.
     * @return A list of terms or quoted phrases.
     */
    public List<String> splitPhrasesAndTerms(String value) {
        List<String> parts = new ArrayList<>();
        Matcher matcher = PHRASE_OR_TERM.matcher(value.strip());

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                parts.add(matcher.group(1)); // quoted phrase
            } else {
                parts.add(matcher.group(2)); // single term
            }
        }

        return parts;
    }

    /**
     * Constructs either a match phrase or term query based on the presence of spaces in the value.
     *
     * @param metadataField The base metadata field name.
     * @param key           The subfield to search within the metadata field.
     * @param value         The term or phrase to search for.
     * @return A {@link Query} object suitable for Elasticsearch.
     */
    public Query buildTermOrPhraseQuery(String metadataField, String key, String value) {
        String fieldPath = metadataField + "." + key;
        String lowerValue = value.toLowerCase();

        if (lowerValue.contains(" ")) {
            return new Query.Builder().matchPhrase(new MatchPhraseQuery.Builder()
                    .field(fieldPath)
                    .query(lowerValue)
                    .build()).build();
        } else {
            return new Query.Builder().term(new TermQuery.Builder()
                    .field(fieldPath)
                    .value(lowerValue)
                    .build()).build();
        }
    }

    /**
     * Formats a {@link SearchResponse} containing {@link Record} objects into a readable string summary.
     * <p>
     * This method reports the total number of results and includes information for each hit such as:
     * the source record, score, highlight snippets, index, and document ID.
     * </p>
     *
     * @param response the search response to format
     * @return a formatted string summarizing the search results
     */
    public String getSearchResponse(SearchResponse<Record> response) {
        StringBuilder output = new StringBuilder();
        TotalHits totalHits = response.hits().total();

        if (totalHits == null) {
            return "No total hits information available.";
        }

        boolean isExactMatch = totalHits.relation() == TotalHitsRelation.Eq;

        if (isExactMatch) {
            output.append("There are ").append(totalHits.value()).append(" results");
        } else {
            output.append("There are more than ").append(totalHits.value()).append(" results");
        }

        for (Hit<Record> hit : response.hits().hits()) {
            output.append("\nHIT")
                    .append("\nSource: ").append(hit.source())
                    .append("\nScore: ").append(hit.score())
                    .append("\nHighlight: ").append(hit.highlight())
                    .append("\nIndex: ").append(hit.index())
                    .append("\nID: ").append(hit.id())
                    .append("\n");
        }

        return output.toString();
    }

    /**
     * Enum representing types of boolean clauses in a query.
     */
    private enum BoolClauseType {
        MUST,
        SHOULD,
        MUST_NOT
    }

}

//Elastic search message inside logs to get password, certificate, and other instructions. We set the password as an environment var in the Docker container
/*
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✅ Elasticsearch security features have been automatically configured!
✅ Authentication is enabled and cluster connections are encrypted.

ℹ️  Password for the elastic user (reset with `bin/elasticsearch-reset-password -u elastic`):
  GYw-sPh-mODUElxCyKAS

ℹ️  HTTP CA certificate SHA-256 fingerprint:
  e6098ee7b9aa2078b90e8ce22a8d1f671e9c73085c10015743c53c0861e5083a

ℹ️  Configure Kibana to use this cluster:
• Run Kibana and click the configuration link in the terminal when Kibana starts.
• Copy the following enrollment token and paste it into Kibana in your browser (valid for the next 30 minutes):
  eyJ2ZXIiOiI4LjExLjEiLCJhZHIiOlsiMTcyLjE3LjAuMjo5MjAwIl0sImZnciI6ImU2MDk4ZWU3YjlhYTIwNzhiOTBlOGNlMjJhOGQxZjY3MWU5YzczMDg1YzEwMDE1NzQzYzUzYzA4NjFlNTA4M2EiLCJrZXkiOiJFdlFKSjR3QjAyci1NZWZGN0wwaTp2M2ZmcUtNVlFycWl2RWVDdGNvZ0tBIn0=

ℹ️ Configure other nodes to join this cluster:
• Copy the following enrollment token and start new Elasticsearch nodes with `bin/elasticsearch --enrollment-token <token>` (valid for the next 30 minutes):
  eyJ2ZXIiOiI4LjExLjEiLCJhZHIiOlsiMTcyLjE3LjAuMjo5MjAwIl0sImZnciI6ImU2MDk4ZWU3YjlhYTIwNzhiOTBlOGNlMjJhOGQxZjY3MWU5YzczMDg1YzEwMDE1NzQzYzUzYzA4NjFlNTA4M2EiLCJrZXkiOiJFUFFKSjR3QjAyci1NZWZGNjczcDpRY0NsSTdtbVQ0NjBOOFgxWHYxbEVnIn0=

  If you're running in Docker, copy the enrollment token and run:
  `docker run -e "ENROLLMENT_TOKEN=<token>" docker.elastic.co/elasticsearch/elasticsearch:8.11.1`
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */