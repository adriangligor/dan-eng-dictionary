package at.gligor.dictionary.index;

import java.io.*;

public class TextStreamReader {

    private final InputStream in;
    private final BufferedReader reader;

    private String line;
    private int lineNr;
    private int offset;

    public TextStreamReader(InputStream in, String charset) {
        this.in = in;
        this.lineNr = 1;
        this.offset = 0;

        try {
            this.reader = new BufferedReader(new InputStreamReader(in, charset));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            reader.close();
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean nextLine() {
        try {
            if (line != null) {
                lineNr += 1;
                offset += line.length() + 1;
            }

            this.line = reader.readLine();

            return (line != null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLine() {
        return line;
    }

    public int getLineNr() {
        return lineNr;
    }

    public int getOffset() {
        return offset;
    }

}
