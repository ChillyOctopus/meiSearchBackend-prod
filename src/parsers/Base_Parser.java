package parsers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class holds methods we use in all other parsers, and is a 'Parent' class.
 */
public class Base_Parser {

    /**
     * This returns the content inside the quotes after String X. We use this as a general parser
     * @param findMe the string whose quotes directly after we are extracting
     * @param meiChunk the chunk of mei in we are searching
     * @return the String inside the quotes in question
     */
    ParsedData getQuotedInTags(String findMe, String meiChunk){
        int indexOfString = meiChunk.indexOf(findMe);
        if(indexOfString == -1) return new ParsedData("Unable to find content '"+findMe+"' in quotes inside string {"+meiChunk+"}", -1, -1, false);
        return getDataBetweenTags("\"", "\"", indexOfString, meiChunk);
    }

    /**
     * This returns the content inside the quotes after String all strings of X. We use this as a general parser.
     * @param findMe the strings whose quotes directly after we are extracting
     * @param meiChunk the chunk of mei in we are searching
     * @return a List<String> inside the quotes in question
     */
    ParsedListData getQuotedInAllTagsNamed(String findMe, String meiChunk){
        int indexOfString = meiChunk.indexOf(findMe);
        List<String> returnMe = new ArrayList<>();
        ParsedData p = new ParsedData("Init data", -1, -1, false);
        //Repeatedly go through the mei until we don't find another tag.
        while(indexOfString != -1){
            p = getDataBetweenTags("\"", "\"", indexOfString, meiChunk);
            if(p.isFound()) returnMe.add(p.getData());
            indexOfString = meiChunk.indexOf(findMe, indexOfString+1);
        }
        if(returnMe.isEmpty()) return new ParsedListData(Collections.singletonList("Unable to find content {" + findMe + "} in quotes inside string '" + meiChunk + "'"), -1, -1, false);
        return new ParsedListData(returnMe, p.getStartIndex(), p.getEndIndex(), true);
    }

    /**
     * This gets the substring between the tags after a certain index, trimmed and whitespace condensed
     * @param openingTag the first tag
     * @param closingTag the second tag
     * @param stringIndex the index we are starting from
     * @param data the entire string we capture data from
     * @return a substring that is in between the starting tags
     */
    ParsedData getDataBetweenTags(String openingTag, String closingTag, int stringIndex, String data){
        ParsedData returnMe;
        // Find the index of the first tag after the string
        int indexOfOpenTag = data.indexOf(openingTag, stringIndex);
        if(indexOfOpenTag == -1) return new ParsedData("Unable to find openingTag "+openingTag+" inside "+data, stringIndex, -1, false);
        // Find the index of the second tag after the first one quotation
        int indexOfCloseTag = data.indexOf(closingTag, indexOfOpenTag + 1);
        if(indexOfCloseTag == -1) return new ParsedData("Unable to find closingTag "+closingTag+" inside "+data, indexOfOpenTag, -1, false);
        // Get the substring between the two points(excluding them)
        return new ParsedData(data.substring(indexOfOpenTag + openingTag.length(), indexOfCloseTag).trim().replaceAll(" +", " "), indexOfOpenTag, indexOfCloseTag, true);
    }

    /**
     * This gets the substring between the tags after a certain index
     * @param openingTag the first tag
     * @param closingTag the second tag
     * @param stringIndex the index we are starting from
     * @param data the entire string we capture data from
     * @return a substring that is in between the starting tags
     */
    ParsedListData getDataBetweenAllTags(String openingTag, String closingTag, int stringIndex, String data){
        List<String> returnMe = new ArrayList<>();

        //Find the first next open tag that fits what we are looking for
        int indexOfLastOpenTag = data.indexOf(openingTag, stringIndex);
        int indexOfCloseTag = -1;

        ParsedData pd = getDataBetweenTags(openingTag, closingTag, stringIndex, data);

        //Until we don't find anymore tags
        while(pd.isFound()){
            //Find the index of the first and second tag after the string
            returnMe.add(pd.getData());
            indexOfCloseTag = pd.getEndIndex();
            indexOfLastOpenTag = pd.getStartIndex();
            pd = getDataBetweenTags(openingTag, closingTag, indexOfCloseTag, data);
        }
        return new ParsedListData(returnMe, indexOfLastOpenTag, indexOfCloseTag, true);
    }
}

