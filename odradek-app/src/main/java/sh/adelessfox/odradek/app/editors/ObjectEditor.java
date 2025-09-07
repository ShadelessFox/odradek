package sh.adelessfox.odradek.app.editors;

import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import sh.adelessfox.odradek.app.ObjectStructure;
import sh.adelessfox.odradek.app.menu.ActionIds;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.hfw.rtti.data.StreamingLink;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;
import sh.adelessfox.odradek.ui.Viewer;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.components.tree.TreeItem;
import sh.adelessfox.odradek.ui.components.tree.TreeLabelProvider;
import sh.adelessfox.odradek.ui.editors.Editor;
import sh.adelessfox.odradek.ui.editors.EditorInput;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.util.Optional;

final class ObjectEditor implements Editor {
    private final ObjectEditorInput input;
    private FlatTabbedPane pane;

    public ObjectEditor(ObjectEditorInput input) {
        this.input = input;
    }

    @Override
    public JComponent createComponent() {
        if (pane == null) {
            pane = createRoot(input.game(), input.object());
        }
        return pane;
    }

    @Override
    public EditorInput getInput() {
        return input;
    }

    @Override
    public boolean isFocused() {
        return pane.getSelectedComponent().isFocusOwner();
    }

    @Override
    public void setFocus() {
        pane.getSelectedComponent().requestFocusInWindow();
    }

    private static FlatTabbedPane createRoot(Game game, TypedObject object) {
        FlatTabbedPane pane = new FlatTabbedPane();
        pane.setTabPlacement(SwingConstants.BOTTOM);
        pane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        Converter.converters(object).forEach(converter -> {
            @SuppressWarnings("unchecked")
            var clazz = (Class<Object>) converter.resultType();
            Viewer.viewers(clazz).forEach(viewer -> {
                var result = converter.convert(object, game);
                if (result.isEmpty()) {
                    return;
                }
                pane.add(viewer.displayName(), viewer.createPreview(result.get()));
            });
        });

        pane.add("Object", new JScrollPane(createObjectTree(game, object)));
        pane.setSelectedIndex(0);

        return pane;
    }

    private static StructuredTree<?> createObjectTree(Game game, TypedObject object) {
        var tree = new StructuredTree<>(new ObjectStructure.Compound(game, object.getType(), object));
        tree.setTransferHandler(new ObjectEditorTransferHandler());
        tree.setLabelProvider(new TreeLabelProvider<>() {
            @Override
            public Optional<String> getText(ObjectStructure element) {
                return Optional.of(element.toString());
            }

            @Override
            public Optional<Icon> getIcon(ObjectStructure element) {
                return Optional.of(Fugue.getIcon("blue-document"));
            }
        });
        tree.addActionListener(event -> {
            var component = event.getLastPathComponent();
            if (component instanceof TreeItem<?> wrapper) {
                component = wrapper.getValue();
            }
            if (component instanceof ObjectStructure structure && structure.value() instanceof StreamingLink<?> link) {
                // showObjectInfo(link.get(), link.groupId(), link.objectIndex());
            }
        });
        Actions.installContextMenu(tree, ActionIds.OBJECT_MENU_ID, tree);
        return tree;
    }
}
