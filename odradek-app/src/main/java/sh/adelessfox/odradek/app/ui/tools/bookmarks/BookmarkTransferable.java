package sh.adelessfox.odradek.app.ui.tools.bookmarks;

import sh.adelessfox.odradek.game.decima.ObjectId;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.List;

record BookmarkTransferable(List<ObjectId> ids) implements Transferable {
    public static final DataFlavor bookmarkListFlavor = new DataFlavor(BookmarkTransferable.class, "Odradek Bookmarks");

    BookmarkTransferable {
        ids = List.copyOf(ids);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{bookmarkListFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(bookmarkListFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (flavor.equals(bookmarkListFlavor)) {
            return ids;
        }
        throw new UnsupportedFlavorException(flavor);
    }
}
