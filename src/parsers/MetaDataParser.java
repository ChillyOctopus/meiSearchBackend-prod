package parsers;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class parses out all the metadata from an MEI string. It uses a static final hashmap of the tags we are searching for.
 */
public class MetaDataParser {
    /**
     * Maps MEI metadata tags to their corresponding Elasticsearch field keys.
     * These tags represent various metadata elements that may appear once or multiple times in MEI documents.
     */
    private static final Map<String, TagSpec> META_TAGS = Map.ofEntries(
            Map.entry("encoding", new TagSpec("date", "type", "encoding")),
            Map.entry("rism_id", new TagSpec("idno", "type", "RISM")),

            Map.entry("musical_source", new TagSpec("notes", "type", "musical source")),
            Map.entry("media", new TagSpec("media", null, null)),
            Map.entry("library", new TagSpec("library", null, null)),
            Map.entry("classmark", new TagSpec("classmark", null, null)),

            Map.entry("composers", new TagSpec("composer", null, null)),
            Map.entry("arrangers", new TagSpec("arranger", null, null)),
            Map.entry("librettists", new TagSpec("librettist", null, null)),
            Map.entry("titles", new TagSpec("title", "type", "main")),
            Map.entry("incipits", new TagSpec("incipText", null, null)),

            Map.entry("collection_title", new TagSpec("title", "type", "collection")),
            Map.entry("collection_number", new TagSpec("collection", "type", "number")),
            Map.entry("collection_editor", new TagSpec("editor", "type", "collection")),

            Map.entry("series_title", new TagSpec("title", "type", "series")),
            Map.entry("series_number", new TagSpec("number", "type", "series")),
            Map.entry("series_editor", new TagSpec("editor", "type", "series")),

            Map.entry("publisher", new TagSpec("publisher", null, null)),
            Map.entry("date", new TagSpec("date", null, null)),
            Map.entry("edition", new TagSpec("edition", null, null)),
            Map.entry("published_place", new TagSpec("pubPlace", null, null)),
            Map.entry("plate_number", new TagSpec("plateNum", null, null)),

            Map.entry("cdc_tableau", new TagSpec("notes", "type", "CdC tableau")),
            Map.entry("cdc_tableau_division", new TagSpec("notes", "type", "CdC tableau division")),
            Map.entry("cdc_number", new TagSpec("notes", "type", "CdC Number")),

            Map.entry("musette_division", new TagSpec("notes", "type", "Musette division")),
            Map.entry("poetic_form_title", new TagSpec("notes", "type", "poetic form associated title")),
            Map.entry("poetic_form_notes", new TagSpec("notes", "type", "poetic form")),
            Map.entry("rhyme_pattern", new TagSpec("notes", "type", "specific rhyme pattern")),

            Map.entry("musical_form_title", new TagSpec("title", "type", "musical form")),
            Map.entry("musical_form_notes", new TagSpec("notes", "type", "musical form")),
            Map.entry("notes", new TagSpec("notes", null, null))
    );

    /**
     * Parses metadata content from the given MEI element by extracting text within configured metadata tags.
     * The returned map associates Elasticsearch field keys with the concatenated, lowercased text contents found inside those tags.
     * Some fields are combined post-parsing to produce consolidated entries like {@code titles} and {@code keywords}.
     *
     * @param meiHead the MEI XML chunk containing metadata tags and values
     * @return a HashMap mapping metadata field keys to their extracted string values, or {@code null} if no data found
     */
    public HashMap<String, String> getDataFromElement(Element meiHead) {
//        System.out.println("(getDataFromEl): "+DocumentParser.elementToString(meiHead));
        HashMap<String, String> metadata = new HashMap<>();

        for (Map.Entry<String, TagSpec> entry : META_TAGS.entrySet()) {
            String fieldKey = entry.getKey();
            TagSpec spec = entry.getValue();

            List<String> values = getTextContentForTag(spec, meiHead);

            if (!values.isEmpty()) {
                metadata.put(fieldKey, String.join(" ", values).toLowerCase());
            } else {
                metadata.put(fieldKey, null);
            }
        }

        combineTitleFields(metadata);
        compileKeywordsField(metadata);
        return metadata;
    }

    /**
     * Extracts the trimmed text content of all {@link Element} nodes under the given {@code root}
     * that match a tag specification defined by {@link TagSpec}. This includes an optional attribute
     * name and value filter, where only elements with matching attribute values are considered.
     *
     * <p>For each matching element, its text content is retrieved via {@link Element#getTextContent()},
     * trimmed, and included in the result list if non-empty.</p>
     *
     * @param spec the {@link TagSpec} defining the tag name and optional attribute filter
     * @param root the root {@link Element} under which to search for matching elements
     * @return a list of non-empty, trimmed text contents of matching elements
     */
    private List<String> getTextContentForTag(TagSpec spec, Element root) {
        List<String> result = new ArrayList<>();
        NodeList elements = root.getElementsByTagName(spec.tagName());

        for (int i = 0; i < elements.getLength(); i++) {
            Element elem = (Element) elements.item(i);

            if (spec.attrName() != null) {
                String attrVal = elem.getAttribute(spec.attrName());
                if (!spec.attrValue().equals(attrVal)) {
                    continue; // skip non-matching attributes
                }
            }

            String text = elem.getTextContent().trim();
            if (!text.isEmpty()) {
                result.add(text);
            }
        }

        return result;
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

        metadata.put("keywords", keywords.isEmpty() ? null : keywords.toString().trim());
    }

    /**
     * Helper method to append a non-empty string to a {@link StringBuilder}, with a trailing space.
     *
     * @param sb    the StringBuilder to append to
     * @param value the string to append if non-empty
     */
    private void appendIfNotEmpty(StringBuilder sb, String value) {
        if (value != null && !value.trim().isEmpty()) {
            sb.append(value).append(' ');
        }
    }

}

