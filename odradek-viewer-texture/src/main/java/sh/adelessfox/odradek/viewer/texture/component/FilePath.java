package sh.adelessfox.odradek.viewer.texture.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record FilePath(List<String> parts) implements Comparable<FilePath> {
    public FilePath {
        parts = List.copyOf(parts);
    }

    public static FilePath of() {
        return new FilePath(List.of());
    }

    public static FilePath of(String path) {
        return new FilePath(List.of(path.split("/")));
    }

    public String part(int index) {
        return parts.get(index);
    }

    public FilePath subpath(int fromIndex, int toIndex) {
        Objects.checkFromToIndex(fromIndex, toIndex, length());
        return new FilePath(parts.subList(fromIndex, toIndex));
    }

    public FilePath concat(String part) {
        var newParts = new ArrayList<>(parts);
        newParts.add(part);
        return new FilePath(newParts);
    }

    public String full(String separator) {
        return String.join(separator, parts);
    }

    public String full() {
        return full("/");
    }

    public String last() {
        return parts.getLast();
    }

    public int length() {
        return parts.size();
    }

    @Override
    public int compareTo(FilePath o) {
        int length = Math.min(length(), o.length());

        for (int i = 0; i < length; i++) {
            String a = part(i);
            String b = o.part(i);

            if (a.equals("*")) {
                return 1;
            }

            if (b.equals("*")) {
                return -1;
            }

            int value = a.compareTo(b);

            if (value != 0) {
                return value;
            }
        }

        return length() - o.length();
    }

    @Override
    public String toString() {
        return full();
    }
}
