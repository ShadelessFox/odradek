package sh.adelessfox.odradek.viewer.texture;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.texture.TextureFormat;
import sh.adelessfox.odradek.texture.TextureSet;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.components.tree.StyledTreeLabelProvider;
import sh.adelessfox.odradek.ui.components.tree.TreeStructure;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

public final class TextureSetViewer implements Viewer {
    public static final class Provider implements Viewer.Provider<TextureSet> {
        @Override
        public Viewer create(TextureSet object, Game game) {
            return new TextureSetViewer(object);
        }

        @Override
        public String name() {
            return "Texture Set";
        }

        @Override
        public Optional<String> icon() {
            return Optional.of("fugue:images");
        }
    }

    private final TextureSet textureSet;
    private ImageView imageView;

    private TextureSetViewer(TextureSet textureSet) {
        this.textureSet = textureSet;
    }

    @Override
    public JComponent createComponent() {
        // var list = new JList<TextureSet.SourceTexture>();
        // list.setListData(textureSet.sourceTextures().toArray(TextureSet.SourceTexture[]::new));
        // list.setCellRenderer(StyledListCellRenderer.adapter((value, c) -> {
        //     c.append(value.path());
        //     c.append(" (" + value.type() + ")", StyledFragment.GRAYED);
        // }));
        // list.addListSelectionListener(e -> {
        //     if (!e.getValueIsAdjusting() && list.getSelectedIndex() >= 0) {
        //         show(textureSet.sourceTextures().get(list.getSelectedIndex()));
        //     }
        // });

        TreeSet<FilePath> paths = new TreeSet<>();
        Map<FilePath, TextureSet.SourceTexture> textures = new HashMap<>();

        for (TextureSet.SourceTexture sourceTexture : textureSet.sourceTextures()) {
            FilePath path = FilePath.of(sourceTexture.path().toLowerCase(Locale.ROOT));
            paths.add(path);
            textures.put(path, sourceTexture);
        }

        var root = new Node(new TreeSet<>(paths), null, FilePath.of(), false);
        var tree = new StructuredTree<>(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setLabelProvider((StyledTreeLabelProvider<Node>) element -> {
            if (!element.leaf && element.parent != null) {
                return Optional.of(StyledText.of(element.path.subpath(element.parent.path.length()).full()));
            } else if (element.leaf) {
                return Optional.of(StyledText.builder()
                    .add(element.path.last())
                    .add(" (" + textures.get(element.path).type() + ")", StyledFragment.GRAYED)
                    .build());
            } else {
                return Optional.empty();
            }
        });
        tree.addActionListener(event -> {
            if (!(event.getLastPathComponent() instanceof Node node && node.leaf)) {
                return;
            }
            show(textures.get(node.path));
        });

        imageView = new ImageView();
        imageView.setZoomToFit(true);

        var panel = new JSplitPane();
        panel.setLeftComponent(new JScrollPane(tree));
        panel.setRightComponent(TextureViewer.createImagePane(imageView));

        return panel;
    }

    private void show(TextureSet.SourceTexture sourceTexture) {
        var packedTexture = textureSet.packedTextures().stream()
            .filter(pt -> pt.packing().contains(sourceTexture.type()))
            .findFirst().orElseThrow();
        var packing = packedTexture.packing();

        var channels = EnumSet.noneOf(Channel.class);
        if (sourceTexture.type().equals(packing.red().orElse(null))) {
            channels.add(Channel.R);
        }
        if (sourceTexture.type().equals(packing.green().orElse(null))) {
            channels.add(Channel.G);
        }
        if (sourceTexture.type().equals(packing.blue().orElse(null))) {
            channels.add(Channel.B);
        }
        if (sourceTexture.type().equals(packing.alpha().orElse(null))) {
            channels.add(Channel.A);
        }

        var converted = packedTexture.texture().convert(TextureFormat.B8G8R8A8_UNORM);
        var image = TextureViewer.createImage(converted.surfaces().getFirst());

        imageView.setImage(image);
        imageView.setChannels(channels);
        imageView.fit();
    }

    private static class Node implements TreeStructure<Node> {
        private static final Comparator<Node> COMPARATOR = Comparator
            .comparingInt((Node node) -> node.leaf ? 1 : -1)
            .thenComparing((Node node) -> node.path.last());

        private final SortedSet<FilePath> paths;
        private final Node parent;
        private final FilePath path;
        private final boolean leaf;

        Node(SortedSet<FilePath> paths, Node parent, FilePath path, boolean leaf) {
            this.paths = paths;
            this.parent = parent;
            this.path = path;
            this.leaf = leaf;
        }

        @Override
        public List<? extends Node> getChildren() {
            var files = paths.subSet(path, path.concat("*"));
            var children = new HashMap<FilePath, Node>();

            for (FilePath directory : getCommonPrefixes(files, path.length())) {
                if (path.equals(directory)) {
                    continue;
                }

                children.computeIfAbsent(
                    directory,
                    path -> new Node(files, this, path, false)
                );
            }

            for (FilePath file : files) {
                if (file.length() - path.length() <= 1) {
                    children.computeIfAbsent(
                        file,
                        path -> new Node(files, this, path, true)
                    );
                }
            }

            return children.values().stream()
                .sorted(COMPARATOR)
                .toList();
        }

        @Override
        public boolean hasChildren() {
            return !leaf;
        }
    }

    private record FilePath(List<String> parts) implements Comparable<FilePath> {
        private FilePath {
            parts = List.copyOf(parts);
        }

        public static FilePath of() {
            return new FilePath(List.of());
        }

        public static FilePath of(String path) {
            return new FilePath(List.of(path.split("/")));
        }

        String part(int index) {
            return parts.get(index);
        }

        FilePath subpath(int fromIndex) {
            return subpath(fromIndex, length());
        }

        FilePath subpath(int fromIndex, int toIndex) {
            Objects.checkFromToIndex(fromIndex, toIndex, length());
            return new FilePath(parts.subList(fromIndex, toIndex));
        }

        FilePath concat(String part) {
            var newParts = new ArrayList<>(parts);
            newParts.add(part);
            return new FilePath(newParts);
        }

        public String full() {
            return String.join("/", parts);
        }

        int length() {
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

        public String last() {
            return parts.getLast();
        }
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
