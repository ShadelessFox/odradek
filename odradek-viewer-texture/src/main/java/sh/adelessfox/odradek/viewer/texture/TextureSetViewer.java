package sh.adelessfox.odradek.viewer.texture;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.texture.TextureFormat;
import sh.adelessfox.odradek.texture.TextureSet;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.components.tree.StyledTreeLabelProvider;
import sh.adelessfox.odradek.viewer.texture.component.CompactFileProvider;
import sh.adelessfox.odradek.viewer.texture.component.FilePath;
import sh.adelessfox.odradek.viewer.texture.component.FileStructure;

import javax.swing.*;
import java.util.*;

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

        var provider = new CompactFileProvider(paths);
        var tree = new StructuredTree<>(FileStructure.of(provider));
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
        tree.addActionListener(event -> {
            if (!(event.getLastPathComponent() instanceof FileStructure.File file)) {
                return;
            }
            show(textures.get(file.path()));
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
}
