package sh.adelessfox.odradek.app.ui.component.bookmarks;

import jakarta.inject.Inject;
import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.editors.ObjectEditorInputLazy;
import sh.adelessfox.odradek.ui.components.tool.ToolPanel;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;

import javax.swing.*;

public class BookmarkToolPanel implements ToolPanel {
    private StructuredTree<BookmarkStructure> tree;

    @Inject
    public BookmarkToolPanel() {
    }

    @Override
    public JComponent createComponent() {
        tree = new StructuredTree<>(new BookmarkStructure.Root());
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setLabelProvider(new BookmarkLabelProvider());
        tree.addActionListener(event -> {
            if (event.getLastPathComponent() instanceof BookmarkStructure.Bookmark bookmark) {
                var manager = Application.getInstance().getEditorManager();
                var input = new ObjectEditorInputLazy(bookmark.id.groupId(), bookmark.id.objectIndex());
                manager.openEditor(input);
            }
        });

        return new JScrollPane(tree);
    }

    @Override
    public boolean isFocused() {
        return tree.isFocusOwner();
    }

    @Override
    public void setFocus() {
        tree.requestFocusInWindow();
    }
}
