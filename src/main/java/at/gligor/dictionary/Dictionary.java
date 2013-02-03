package at.gligor.dictionary;

import at.gligor.dictionary.index.SearchResult;
import at.gligor.dictionary.index.TextStreamReader;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Dictionary {
    //"<b>zut alors</b><br/>bla bla bla bla bla<br/><a href=\"http://www.google.com/?q=" + lookup + "\">look it up in google!</a>"
    public static final String GOOGLE_TRANSLATE_HTML_FORMAT = "<a href=\"http://translate.google.com/#%s%%7C%s%%7C%s\">try Google Translate</a>";
    public static final String ERROR_HTML_FORMAT = "An error occurred: %s";
    public static final String RESULT_OPEN_HTML = "<div style=\"margin: 5px;\">";
    public static final String RESULT_CLOSE_HTML = "</div>";
    public static final String NOT_FOUND_HTML = "nothing found...";
    public static final String NOT_ENOUGH_HTML = "unhappy?";
    public static final String BLOCK_SEPARATOR_HTML = "<hr style=\"margin: 5px;\"/>";

    private final Lang lang;
    private final Multimap<String, SearchResult> index;

    public Dictionary(Lang lang, Multimap<String, SearchResult> index) {
        this.lang = lang;
        this.index = index;
    }

    public String translate(String lookup, Lang toLang) {
        try {
            final List<String> perfectMatches = new ArrayList<String>();
            final List<String> prefixMatches = new ArrayList<String>();
            final List<String> matches = new ArrayList<String>();

            final Collection<SearchResult> results = index.get(lookup);
            for (SearchResult result : results) {
                final InputStream in = getClass().getResourceAsStream(lang.getDictResourcePath());
                final long skipped = in.skip(result.getOffset());
                final TextStreamReader reader = new TextStreamReader(in, "ISO-8859-15");
                reader.nextLine();

                if (!result.isSubterm()) {
                    perfectMatches.add(reader.getLine());
                } else if (result.getStartPos() == 0) {
                    prefixMatches.add(reader.getLine()); // todo also support postfix results
                } else {
                    matches.add(reader.getLine());
                }

                reader.close();
            }

            final String translateLink = String.format(GOOGLE_TRANSLATE_HTML_FORMAT,
                    URLEncoder.encode(lang.getGoogleTranslateCode(), "UTF-8"),
                    URLEncoder.encode(toLang.getGoogleTranslateCode(), "UTF-8"),
                    URLEncoder.encode(lookup, "UTF-8"));

            final StringBuilder result = new StringBuilder(RESULT_OPEN_HTML);
            mergeMatches(result, perfectMatches, prefixMatches, matches);

            result.append(BLOCK_SEPARATOR_HTML);
            if (result.length() == 0) {
                result.append(NOT_FOUND_HTML);
            } else {
                result.append(NOT_ENOUGH_HTML);
            }
            result.append(" ").append(translateLink);

            result.append(RESULT_CLOSE_HTML);

            return result.toString();
        } catch (IOException e) {
            return String.format(ERROR_HTML_FORMAT, e.getMessage());
        }
    }

    private StringBuilder mergeMatches(StringBuilder result, List<String> perfectMatches, List<String> wordMatches, List<String> matches) {
        final String perfectMatchesBlock = join(perfectMatches, "<br/>");
        final String wordMatchesBlock = join(wordMatches, "<br/>");
        final String matchesBlock = join(matches, "<br/>");

        if (!perfectMatchesBlock.isEmpty()) {
            result.append(perfectMatchesBlock);
        }

        if (!wordMatchesBlock.isEmpty()) {
            if (result.length() > 0) {
                result.append(BLOCK_SEPARATOR_HTML);
            }
            result.append(wordMatchesBlock);
        }

        if (!matchesBlock.isEmpty()) {
            if (result.length() > 0) {
                result.append(BLOCK_SEPARATOR_HTML);
            }
            result.append(matchesBlock);
        }

        return result;
    }

    private String join(List<String> strings, String separator) {
        if (strings.size() == 0) {
            return "";
        } else if (strings.size() == 1) {
            return strings.get(0);
        } else {
            final StringBuilder builder = new StringBuilder();
            builder.append(strings.get(0));
            for (int i = 1; i < strings.size(); i++) {
                builder.append(separator).append(strings.get(i));
            }
            return builder.toString();
        }
    }
}
