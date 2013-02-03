package at.gligor.dictionary.index;

public class SearchTerm {

    private final String term;
    private final SearchResult searchResult;

    SearchTerm(String term, boolean subterm, int startPos, int offset) {
        this.term = term;
        this.searchResult = new SearchResult(subterm, startPos, offset);
    }

    public String getTerm() {
        return term;
    }

    public SearchResult getSearchResult() {
        return searchResult;
    }

    @Override
    public String toString() {
        return term;
    }

}
