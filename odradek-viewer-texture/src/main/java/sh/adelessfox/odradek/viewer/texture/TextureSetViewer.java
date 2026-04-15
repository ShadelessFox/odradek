package sh.adelessfox.odradek.viewer.texture;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.texture.Surface;
import sh.adelessfox.odradek.texture.TextureSet;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.components.tree.StyledTreeLabelProvider;
import sh.adelessfox.odradek.ui.components.tree.TreeActionListener;
import sh.adelessfox.odradek.viewer.texture.component.CompactFileProvider;
import sh.adelessfox.odradek.viewer.texture.component.FilePath;
import sh.adelessfox.odradek.viewer.texture.component.FileStructure;
import sh.adelessfox.odradek.viewer.texture.view.Channel;
import sh.adelessfox.odradek.viewer.texture.view.ImagePanel;
import sh.adelessfox.odradek.viewer.texture.view.ImageView;

import javax.swing.*;
import java.util.*;

public final class TextureSetViewer implements Viewer {
    private StructuredTree<FileStructure> tree;

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
        this.imageView = new ImageView();

        var panel = new JSplitPane();
        panel.setLeftComponent(new JScrollPane(createTextureTree()));
        panel.setRightComponent(new ImagePanel(imageView));

        return panel;
    }

    private StructuredTree<FileStructure> createTextureTree() {
        var paths = new TreeSet<FilePath>();
        var textures = new HashMap<FilePath, TextureSet.SourceTexture>();

        for (TextureSet.SourceTexture sourceTexture : textureSet.sourceTextures()) {
            var path = FilePath.of(sourceTexture.path().toLowerCase(Locale.ROOT));
            paths.add(path);
            textures.put(path, sourceTexture);
        }

        tree = new StructuredTree<>(FileStructure.of(new CompactFileProvider(paths)));
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setLabelProvider((StyledTreeLabelProvider<FileStructure>) element -> switch (element) {
            case FileStructure.File file -> StyledText.builder()
                .add(file.path().last())
                .add(" (" + textures.get(file.path()).type() + ")", StyledFragment.GRAYED)
                .build();
            case FileStructure.Folder folder -> {
                int start = folder.parent().isPresent() ? folder.parent().get().length() : 0;
                var builder = StyledText.builder();
                for (int i = start; i < folder.path().length(); i++) {
                    if (i > start) {
                        builder.add("\u2009/\u2009", StyledFragment.GRAYED);
                    }
                    builder.add(folder.path().part(i));
                }
                yield builder.build();
            }
        });
        tree.addActionListener(TreeActionListener.treePathSelectedAdapter(event -> {
            if (!(event.getLastPathComponent() instanceof FileStructure.File file)) {
                return;
            }
            show(textures.get(file.path()));
        }));

        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }

        return tree;
    }

    @Override
    public void activate() {
        if (tree.getSelectionPath() != null) {
            return;
        }
        for (int row = 0; row < tree.getRowCount(); row++) {
            var path = tree.getPathForRow(row);
            if (tree.getLastPathComponent(path) instanceof FileStructure.File) {
                tree.setSelectionPath(path);
                break;
            }
        }
    }

    private void show(TextureSet.SourceTexture sourceTexture) {
        var packedTexture = textureSet.packedTextures().stream()
            .filter(pt -> pt.packing().contains(sourceTexture.type()))
            .findFirst().orElse(null);

        if (packedTexture != null) {
            var texture = packedTexture.texture();
            var surface = texture.surfaces().getFirst();
            var image = surface.convert(texture.format(), new Surface.Converter.AWT());

            imageView.setImage(image);
            imageView.setChannels(determineChannelsUsed(sourceTexture, packedTexture));
            imageView.setZoomToFit(true);
            imageView.fit();
        } else {
            imageView.setImage(null);
        }
    }

    private static Set<Channel> determineChannelsUsed(
        TextureSet.SourceTexture sourceTexture,
        TextureSet.PackedTexture packedTexture
    ) {
        var packing = packedTexture.packing();
        var channels = EnumSet.noneOf(Channel.class);

        if (packingMatches(packing.red(), sourceTexture.type())) {
            channels.add(Channel.R);
        }
        if (packingMatches(packing.green(), sourceTexture.type())) {
            channels.add(Channel.G);
        }
        if (packingMatches(packing.blue(), sourceTexture.type())) {
            channels.add(Channel.B);
        }
        if (packingMatches(packing.alpha(), sourceTexture.type())) {
            channels.add(Channel.A);
        }

        return channels;
    }

    private static boolean packingMatches(Optional<TextureSet.PackingChannel> packing, String type) {
        return packing.filter(x -> x.type().equals(type)).isPresent();
    }
}
