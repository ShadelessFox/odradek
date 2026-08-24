package sh.adelessfox.odradek.app.ui.tools.bookmarks;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.bookmarks.BookmarkKey;
import sh.adelessfox.odradek.app.ui.bookmarks.FolderId;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.components.tree.TreeItem;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.util.Gatherers;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

final class BookmarkTransferHandler extends TransferHandler {
    private static final DataFlavor FLAVOR = new DataFlavor(Payload.class, "Odradek Bookmarks");

    private final StructuredTree<BookmarkStructure> tree;

    BookmarkTransferHandler(StructuredTree<BookmarkStructure> tree) {
        this.tree = tree;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        return new Payload(selectedBookmarks(), selectedFolders());
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (!support.isDataFlavorSupported(FLAVOR)) {
            return false;
        }
        var data = getData(support);
        var target = findDropFolder(support).orElse(null);
        if (target == null) {
            return false;
        }
        var bookmarks = Application.getInstance().bookmarks();
        for (FolderId folder : data.folders()) {
            if (bookmarks.isDescendant(folder, target)) {
                return false;
            }
        }
        for (BookmarkKey bookmark : data.bookmarks()) {
            if (bookmarks.getParent(bookmark).equals(target)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        var data = getData(support);
        var target = findDropFolder(support).orElse(null);
        if (target != null) {
            var bookmarks = Application.getInstance().bookmarks();
            data.bookmarks().forEach(id -> bookmarks.move(id, target));
            data.folders().forEach(id -> bookmarks.moveFolder(id, target));
            return true;
        }
        return false;
    }

    private static Payload getData(TransferSupport support) {
        try {
            return (Payload) support.getTransferable().getTransferData(FLAVOR);
        } catch (UnsupportedFlavorException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Optional<FolderId> findDropFolder(TransferSupport support) {
        TreePath path;
        if (support.isDrop()) {
            path = ((JTree.DropLocation) support.getDropLocation()).getPath();
        } else {
            path = tree.getSelectionPath();
        }
        if (path == null) {
            return Optional.empty();
        }
        for (int i = path.getPathCount() - 1; i >= 0; i--) {
            var component = path.getPathComponent(i);
            if (component instanceof TreeItem<?> item) {
                component = item.getValue();
            }
            if (component instanceof BookmarkStructure.Folder folder) {
                return Optional.of(folder.id());
            }
        }
        return Optional.empty();
    }

    private List<BookmarkKey> selectedBookmarks() {
        return tree.get(DataKeys.SELECTION_LIST).stream()
            .flatMap(Collection::stream)
            .gather(Gatherers.instanceOf(BookmarkStructure.Bookmark.class))
            .map(BookmarkStructure.Bookmark::key)
            .toList();
    }

    private List<FolderId> selectedFolders() {
        return tree.get(DataKeys.SELECTION_LIST).stream()
            .flatMap(Collection::stream)
            .gather(Gatherers.instanceOf(BookmarkStructure.Folder.class))
            .map(BookmarkStructure.Folder::id)
            .toList();
    }

    private record Payload(List<BookmarkKey> bookmarks, List<FolderId> folders) implements Transferable {
        Payload {
            bookmarks = List.copyOf(bookmarks);
            folders = List.copyOf(folders);
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{FLAVOR};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(FLAVOR);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (flavor.equals(FLAVOR)) {
                return this;
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
