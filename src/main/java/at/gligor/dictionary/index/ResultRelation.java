package at.gligor.dictionary.index;

public enum ResultRelation {

    PREFIX,
    INFIX,
    POSTFIX,
    FULL;

    public static ResultRelation calculate(String term, int subtermPos, int subtermLen) {
        if (subtermPos == 0 && subtermLen == term.length()) {
            return FULL;
        } else if (subtermPos == 0 && subtermLen < term.length()) {
            return PREFIX;
        } else if (subtermPos + subtermLen == term.length()) {
            return POSTFIX;
        } else if (subtermPos > 0 && subtermPos + subtermLen < term.length()) {
            return INFIX;
        }

        throw new IllegalArgumentException("cannot calculate result relation");
    }

}

