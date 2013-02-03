package at.gligor.dictionary;

public enum Lang {

    DAN("/dansk-engelsk.html", "/dan-eng.idx.txt", "da"),
    ENG("/engelsk-dansk.html", "/eng-dan.idx.txt", "en");

    private final String dictResourcePath;
    private final String googleTranslateCode;
    private String indexResourcePath;

    Lang(String dictResourcePath, String indexResourcePath, String googleTranslateCode) {
        this.dictResourcePath = dictResourcePath;
        this.indexResourcePath = indexResourcePath;
        this.googleTranslateCode = googleTranslateCode;
    }

    public String getDictResourcePath() {
        return dictResourcePath;
    }

    public String getGoogleTranslateCode() {
        return googleTranslateCode;
    }

    public String getIndexResourcePath() {
        return indexResourcePath;
    }

}
