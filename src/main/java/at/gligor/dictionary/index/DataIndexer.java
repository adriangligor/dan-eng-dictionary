package at.gligor.dictionary.index;

import at.gligor.dictionary.Lang;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import static at.gligor.dictionary.Util.join;
import static java.lang.String.format;

public class DataIndexer {

    public static final int MIN_SUBTERM_LENGTH = 4;
    public static final int MAX_SUBTERM_LENGTH = 999;
    public static final String MARKER_BEGIN = "<b>";
    public static final String MARKER_END = "</b>";

    public static final String IDX_TERM_SEPARATOR = ":";
    public static final String IDX_RESULT_SEPARATOR = ";";
    public static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSSS");

    public static void main(String[] args) throws Exception {
        final DataIndexer dataIndexer = new DataIndexer();

        final Multimap<String, SearchResult> danIndex = dataIndexer.createIndex(Lang.DAN);
        dataIndexer.storeIndex(danIndex, "./src/main/resources/" + Lang.DAN.getIndexResourcePath());

        final Multimap<String, SearchResult> engIndex = dataIndexer.createIndex(Lang.ENG);
        dataIndexer.storeIndex(engIndex, "./src/main/resources/" + Lang.ENG.getIndexResourcePath());

        /*
        final Multimap<String, SearchResult> danIndex1 = dataIndexer.loadIndex(Lang.DAN);
        final Multimap<String, SearchResult> engIndex1 = dataIndexer.loadIndex(Lang.ENG);

        final Collection<SearchResult> results = danIndex1.get("mørk");
        for (SearchResult result : results) {
            final InputStream in = DataIndexer.class.getResourceAsStream(Lang.DAN.getDictResourcePath());
            in.skip(result.getOffset());
            final TextStreamReader reader = new TextStreamReader(in, "ISO-8859-15");
            reader.nextLine();
            System.out.println("result: " + reader.getLine());
            reader.close();
        }
        */
    }

    public Multimap<String, SearchResult> createIndex(Lang lang) throws IOException {
        final InputStream in = getClass().getResourceAsStream(lang.getDictResourcePath());
        final TextStreamReader reader = new TextStreamReader(in, "ISO-8859-15");
        final Multimap<String, SearchResult> index = LinkedListMultimap.create();

        System.out.println(format("validating %s", lang.getDictResourcePath()));

        while (reader.nextLine()) {
            try {
                final String relevantText = extractRelevantText(reader.getLine(), reader.getLineNr());
                final String rawText = removeHtmlMarkup(relevantText, reader.getLineNr());
                final String text = removeRomanLiterals(rawText, reader.getLineNr());
                final LinkedList<String> rawTerms = splitIntoTerms(text, reader.getLineNr());
                final LinkedList<String> terms = removeSpecialCharacters(rawTerms, reader.getLineNr());
                final LinkedList<String> normalizedTerms = coalesceAndLowercase(terms, reader.getLineNr());
                final LinkedList<SearchTerm> searchableTerms = generateSubterms(normalizedTerms, reader.getOffset());

                for (final SearchTerm searchableTerm : searchableTerms) {
                    index.put(searchableTerm.getTerm(), searchableTerm.getSearchResult());
                }

                //System.out.println(format("%d: %s ==> %s", reader.getOffset(), text, join(searchableTerms, ",")));
            } catch (LineParsingException e) {
                System.out.println(e.getMessage());
            }
        }

        reader.close();
        System.out.println(format("finished %s", lang.getDictResourcePath()));

        return index;
    }

    public void storeIndex(Multimap<String, SearchResult> index, String filename) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));

        for (final String term : index.keySet()) {
            final Collection<SearchResult> results = index.get(term);
            final LinkedList<String> serializedResults = new LinkedList<String>();

            for (final SearchResult result : results) {
                if (result.getResultRelation() == ResultRelation.FULL) {
                    serializedResults.add(Integer.toString(result.getOffset()));
                }
            }

            if (!serializedResults.isEmpty()) {
                writer.append(term).append(IDX_TERM_SEPARATOR).append(join(serializedResults, IDX_RESULT_SEPARATOR));
                writer.newLine();
            }
        }

        writer.close();
    }

    public Multimap<String, SearchResult> loadIndex(Lang lang) throws IOException {
        System.out.println(format("[%s] loading %s", TIME_FORMAT.format(new Date()), lang.getIndexResourcePath()));
        final Multimap<String, SearchResult> index = LinkedListMultimap.create(70000);
        final InputStream in = getClass().getResourceAsStream(lang.getIndexResourcePath());
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

        String line;
        while ((line = reader.readLine()) != null) {
            final int termSeparatorIdx = line.indexOf(IDX_TERM_SEPARATOR);

            final String term = line.substring(0, termSeparatorIdx);
            final LinkedList<String> terms = new LinkedList<String>();
            terms.add(term);

            final String[] results = line.substring(termSeparatorIdx + 1).split(IDX_RESULT_SEPARATOR);
            for (final String result : results) {
                final Integer offset = Integer.valueOf(result);

                final LinkedList<SearchTerm> searchableTerms = generateSubterms(terms, offset);
                for (final SearchTerm searchableTerm : searchableTerms) {
                    index.put(searchableTerm.getTerm(), searchableTerm.getSearchResult());
                }
            }
        }

        System.out.println(format("[%s] finished %s", TIME_FORMAT.format(new Date()), lang.getIndexResourcePath()));
        return index;
    }

    private static String extractRelevantText(String line, int linenr) {
        final int beginMarkerPos = line.indexOf(MARKER_BEGIN);
        final int endMarkerPos = line.indexOf(MARKER_END);

        if (beginMarkerPos == -1 || endMarkerPos == -1) {
            throw new LineParsingException("no terms found", line, linenr);
        }

        if (endMarkerPos < beginMarkerPos) {
            throw new LineParsingException("unexpected markup", line, linenr);
        }

        return line.substring(beginMarkerPos, endMarkerPos + MARKER_END.length());
    }

    private static String removeHtmlMarkup(String relevantText, int linenr) {
        if (relevantText.length() < MARKER_BEGIN.length() + MARKER_END.length()) {
            throw new LineParsingException("relevant text too short", relevantText, linenr);
        }

        final int endIndex = relevantText.length() - MARKER_END.length();
        final String innerText = relevantText.substring(MARKER_BEGIN.length(), endIndex).trim();

        return innerText
                .replace("&amp;", "&")
                .replace("\u0081", "ü")
                .replace("\u0082", "é")
                .replace("\u0083", "â")
                .replace("\u0084", "ä")
                .replace("\u0087", "ç")
                .replace("\u0088", "ê")
                .replace("\u008A", "è")
                .replace("\u008B", "ï")
                .replace("\u0093", "ô")
                .replace("\u009C", "£");
    }

    private static String removeRomanLiterals(String rawText, int linenr) {
        if (rawText.matches(".*(&[^;]+;)|(\\\\u\\d{4}).*")) {
            throw new LineParsingException("html markup found", rawText, linenr);
        }

        if (rawText.matches("^[IVX]+\\. .*")) {
            return rawText.substring(rawText.indexOf(". ") + 2);
        }

        return rawText;
    }

    private static LinkedList<String> splitIntoTerms(String text, @SuppressWarnings("unused") int linenr) {
        final LinkedList<String> rawTerms = new LinkedList<String>();

        final String[] strings = text.split("[ ,-/'`&]");
        for (final String string : strings) {
            if (string.length() > 0) {
                rawTerms.add(string);
            }
        }

        return rawTerms;
    }

    private static LinkedList<String> removeSpecialCharacters(LinkedList<String> rawTerms, @SuppressWarnings("unused") int linenr) {
        final LinkedList<String> terms = new LinkedList<String>();

        for (final String rawTerm : rawTerms) {
            final String fixedTerm = rawTerm.replaceAll("[.()!]", "");
            if (fixedTerm.length() > 0) {
                terms.add(fixedTerm);
            }
        }

        return terms;
    }

    private static LinkedList<String> coalesceAndLowercase(LinkedList<String> terms, int linenr) {
        final LinkedList<String> searchTerms = new LinkedList<String>();

        for (final String term : terms) {
            final String searchTerm = term.toLowerCase()
                    .replace("ü", "u")
                    .replace("á", "a")
                    .replace("é", "e")
                    .replace("â", "a")
                    .replace("ä", "a")
                    .replace("ç", "c")
                    .replace("ê", "e")
                    .replace("è", "e")
                    .replace("ï", "i")
                    .replace("ô", "o");
            if (searchTerm.matches(".*[^0-9A-Za-zæøå$£].*")) {
                throw new LineParsingException("unexpected character", searchTerm, linenr);
            }
            searchTerms.add(searchTerm);
        }

        return searchTerms;
    }

    private static LinkedList<SearchTerm> generateSubterms(LinkedList<String> terms, int offset) {
        final LinkedList<SearchTerm> subterms = new LinkedList<SearchTerm>();

        for (String term : terms) {
            for (int len = MIN_SUBTERM_LENGTH; len < Math.min(MAX_SUBTERM_LENGTH, term.length()); len++) {
                for (int pos = 0; pos < term.length() - len + 1; pos++) {
                    final ResultRelation relation = ResultRelation.calculate(term, pos, len);
                    subterms.add(new SearchTerm(term.substring(pos, pos + len), relation, offset));
                }
            }
            subterms.add(new SearchTerm(term, ResultRelation.FULL, offset));
        }

        return subterms;
    }

    private static String prettyprint(String message, String text, int linenr) {
        return format("line %d: %s - ›%s‹", linenr, message, shorten(text, 50));
    }

    private static String shorten(String text, int maxLength) {
        return (text.length() <= maxLength ? text : text.substring(0, maxLength));
    }

    private static class LineParsingException extends RuntimeException {
        public LineParsingException(String message, String text, int linenr) {
            super(prettyprint(message, text, linenr));
        }
    }

}
