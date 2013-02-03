package at.gligor.dictionary.index;

public class SearchResult {

    private final boolean subterm;
    private final int startPos;
    private final int offset;

    SearchResult(boolean subterm, int startPos, int offset) {
        this.subterm = subterm;
        this.startPos = startPos;
        this.offset = offset;
    }

    public boolean isSubterm() {
        return subterm;
    }

    public int getStartPos() {
        return startPos;
    }

    public int getOffset() {
        return offset;
    }

}
