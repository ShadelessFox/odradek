package sh.adelessfox.odradek.app.ui.tools.bookmarks;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.components.tree.TreeItem;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.util.Gatherers;

import javax.swing.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

final class BookmarkTransferHandler extends TransferHandler {
    private final StructuredTree<BookmarkStructure> tree;

    BookmarkTransferHandler(StructuredTree<BookmarkStructure> tree) {
        this.tree = tree;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return getSelection().isEmpty() ? NONE : MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        return new BookmarkTransferable(getSelection());
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDataFlavorSupported(BookmarkTransferable.bookmarkListFlavor);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        var ids = getTransferable(support);
        var location = (JTree.DropLocation) support.getDropLocation();

        var path = location.getPath();
        for (int i = path.getPathCount() - 1; i >= 0; i--) {
            var component = path.getPathComponent(i);
            if (component instanceof TreeItem<?> item) {
                component = item.getValue();
            }
            if (component instanceof BookmarkStructure.Folder folder) {
                moveBookmarks(folder, ids);
                return true;
            }
            path = path.getParentPath();
        }
        return false;
    }

    private static void moveBookmarks(BookmarkStructure.Folder folder, List<ObjectId> ids) {
        for (ObjectId id : ids) {
            Application.getInstance().bookmarks().move(id, folder.id());
        }
    }

    @SuppressWarnings("unchecked")
    private static List<ObjectId> getTransferable(TransferSupport support) {
        try {
            return (List<ObjectId>) support.getTransferable().getTransferData(BookmarkTransferable.bookmarkListFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<ObjectId> getSelection() {
        return tree.get(DataKeys.SELECTION_LIST).stream()
            .flatMap(Collection::stream)
            .gather(Gatherers.instanceOf(BookmarkStructure.Bookmark.class))
            .map(BookmarkStructure.Bookmark::id)
            .toList();
    }
}
