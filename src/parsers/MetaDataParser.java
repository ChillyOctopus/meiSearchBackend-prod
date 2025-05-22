package parsers;

import java.util.*;

/**
 * This class parses out all the metadata from an MEI string. It uses a static final hashmap of the tags we are searching for.
 */
public class MetaDataParser extends Base_Parser {
    /**
     * Maps MEI metadata tags to their corresponding Elasticsearch field keys.
     * These tags represent various metadata elements that may appear once or multiple times in MEI documents.
     */
    public static final Map<String, String> META_TAGS = Map.ofEntries(
            Map.entry("<date type=\"encoding\">", "encoding"),
            Map.entry("<idno type=\"RISM\">", "rism_id"),

            Map.entry("<notes type=\"musical source\">", "musical_source"),
            Map.entry("<media>", "media"),
            Map.entry("<library>", "library"),
            Map.entry("<classmark>", "classmark"),

            Map.entry("<composer>", "composers"),
            Map.entry("<arranger>", "arrangers"),
            Map.entry("<librettist>", "librettists"),
            Map.entry("<title type=\"main\">", "titles"),
            Map.entry("<incipText>", "incipits"),

            Map.entry("<title type=\"collection\">", "collection_title"),
            Map.entry("<collection type=\"number\">", "collection_number"),
            Map.entry("<editor type=\"collection\">", "collection_editor"),

            Map.entry("<title type=\"series\">", "series_title"),
            Map.entry("<number type=\"series\">", "series_number"),
            Map.entry("<editor type=\"series\">", "series_editor"),

            Map.entry("<publisher>", "publisher"),
            Map.entry("<date>", "date"),
            Map.entry("<edition>", "edition"),
            Map.entry("<pubPlace>", "published_place"),
            Map.entry("<plateNum>", "plate_number"),

            Map.entry("<notes type=\"CdC tableau\">", "cdc_tableau"),
            Map.entry("<notes type=\"CdC tableau division\">", "cdc_tableau_division"),
            Map.entry("<notes type=\"CdC Number\">", "cdc_number"),

            Map.entry("<notes type=\"Musette division\">", "musette_division"),

            Map.entry("<notes type=\"poetic form associated title\">", "poetic_form_title"),
            Map.entry("<notes type=\"poetic form\">", "poetic_form_notes"),

            Map.entry("<notes type=\"specific rhyme pattern\">", "rhyme_pattern"),

            Map.entry("<title type=\"musical form\">", "musical_form_title"),
            Map.entry("<notes type=\"musical form\">", "musical_form_notes"),

            Map.entry("<notes>", "notes")
    );

    /**
     * Parses metadata content from the given MEI chunk string by extracting text within configured metadata tags.
     * The returned map associates Elasticsearch field keys with the concatenated, lowercased text contents found inside those tags.
     * Some fields are combined post-parsing to produce consolidated entries like {@code titles} and {@code keywords}.
     *
     * @param meiChunk the MEI XML chunk containing metadata tags and values
     * @return a HashMap mapping metadata field keys to their extracted string values, or {@code null} if no data found
     */
    public HashMap<String, String> getData(String meiChunk) {
        HashMap<String, String> metadata = new HashMap<>();
        if (meiChunk == null || meiChunk.trim().isEmpty()) {
            return metadata;
        }

        for (String openTag : META_TAGS.keySet()) {
            ParsedListData parsedData = getDataBetweenAllTags(openTag, "</", 0, meiChunk);
            String fieldKey = META_TAGS.get(openTag);
            if (parsedData.isFound()) {
                String concatenated = String.join(" ",
                        parsedData.getData().stream()
                                .map(String::toLowerCase)
                                .toList());
                metadata.put(fieldKey, concatenated);
            } else {
                metadata.put(fieldKey, null);
            }
        }

        combineTitleFields(metadata);
        compileKeywordsField(metadata);

        return metadata;
    }

    /**
     * Combines individual title-related metadata fields into a single {@code titles} entry.
     *
     * @param metadata the metadata map to update
     */
    private void combineTitleFields(HashMap<String, String> metadata) {
        StringBuilder combinedTitles = new StringBuilder();

        appendIfNotEmpty(combinedTitles, metadata.get("titles"));
        appendIfNotEmpty(combinedTitles, metadata.get("collection_title"));
        appendIfNotEmpty(combinedTitles, metadata.get("series_title"));

        metadata.put("titles", combinedTitles.toString().trim());
    }

    /**
     * Concatenates all non-empty metadata field values into a unified {@code keywords} entry.
     *
     * @param metadata the metadata map to update
     */
    private void compileKeywordsField(HashMap<String, String> metadata) {
        StringBuilder keywords = new StringBuilder();

        for (String value : metadata.values()) {
            appendIfNotEmpty(keywords, value);
        }

        if (!keywords.isEmpty()) {
            metadata.put("keywords", keywords.toString().trim());
        } else {
            metadata.put("keywords", null);
        }
    }

    /**
     * Helper method to append a non-empty string to a {@link StringBuilder}, with a trailing space.
     *
     * @param sb the StringBuilder to append to
     * @param value the string to append if non-empty
     */
    private void appendIfNotEmpty(StringBuilder sb, String value) {
        if (value != null && !value.trim().isEmpty()) {
            sb.append(value).append(' ');
        }
    }

}
