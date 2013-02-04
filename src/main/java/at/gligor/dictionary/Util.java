package at.gligor.dictionary;

import java.util.List;

public class Util {

    public static String join(List<String> strings, String separator) {
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
