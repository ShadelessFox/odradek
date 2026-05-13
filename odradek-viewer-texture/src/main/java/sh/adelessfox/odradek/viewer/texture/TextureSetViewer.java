package sh.adelessfox.odradek.viewer.texture;

import com.formdev.flatlaf.extras.components.FlatSplitPane;
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
import sh.adelessfox.odradek.viewer.texture.view.ImagePanel;
import sh.adelessfox.odradek.viewer.texture.view.ImageView;

import javax.swing.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.TreeSet;

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

        var panel = new FlatSplitPane();
        panel.setLeftComponent(new JScrollPane(createTextureTree()));
        panel.setRightComponent(new ImagePanel(imageView));
        panel.setStyle("style: grip");

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
        var texture = textureSet.unpack(sourceTexture).orElse(null);
        if (texture != null) {
            var surface = texture.surfaces().getFirst();
            var image = surface.convert(new Surface.Converter.AWT());

            imageView.setImage(image);
            imageView.setZoomToFit(true);
            imageView.fit();
        } else {
            imageView.setImage(null);
        }
    }
}
