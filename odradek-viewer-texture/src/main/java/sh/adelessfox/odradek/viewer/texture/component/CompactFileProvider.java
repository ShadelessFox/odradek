package sh.adelessfox.odradek.viewer.texture.component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CompactFileProvider implements FileProvider {
    private final SortedSet<FilePath> paths;

    public CompactFileProvider(Set<FilePath> paths) {
        this.paths = new TreeSet<>(paths);
    }

    @Override
    public Collection<FilePath> getChildren(FilePath parent) {
        var files = paths.subSet(parent, parent.concat("*"));
        var children = new HashMap<FilePath, FilePath>();

        for (FilePath directory : getCommonPrefixes(files, parent.length())) {
            if (!parent.equals(directory)) {
                children.computeIfAbsent(directory, Function.identity());
            }
        }

        for (FilePath file : files) {
            if (file.length() - parent.length() <= 1) {
                children.computeIfAbsent(file, Function.identity());
            }
        }

        return children.values();
    }

    @Override
    public boolean hasChildren(FilePath parent) {
        return !paths.tailSet(parent).isEmpty();
    }

    @Override
    public boolean isFile(FilePath path) {
        return paths.contains(path);
    }

    private static List<FilePath> getCommonPrefixes(Collection<FilePath> paths, int offset) {
        return paths.stream()
            .collect(Collectors.groupingBy(p -> p.part(offset)))
            .values().stream()
            .map(p -> getCommonPrefix(p, offset))
            .toList();
    }

    private static FilePath getCommonPrefix(List<FilePath> paths, int offset) {
        FilePath path = paths.getFirst();
        int position = Math.min(offset, path.length() - 1);

        outer:
        while (position < path.length() - 1) {
            for (FilePath other : paths) {
                if (other.length() < position || !path.part(position).equals(other.part(position))) {
                    break outer;
                }
            }

            position += 1;
        }

        return path.subpath(0, position);
    }
}
