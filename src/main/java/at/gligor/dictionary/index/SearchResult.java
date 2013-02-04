package at.gligor.dictionary.index;

public class SearchResult {

    private final ResultRelation resultRelation;
    private final int offset;

    SearchResult(ResultRelation resultRelation, int offset) {
        this.resultRelation = resultRelation;
        this.offset = offset;
    }

    public ResultRelation getResultRelation() {
        return resultRelation;
    }

    public int getOffset() {
        return offset;
    }

}
