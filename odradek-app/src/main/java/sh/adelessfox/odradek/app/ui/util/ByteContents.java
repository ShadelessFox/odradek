package sh.adelessfox.odradek.app.ui.util;

import java.awt.datatransfer.*;
import java.io.ByteArrayInputStream;

public record ByteContents(byte[] data) implements Transferable, ClipboardOwner {
    private static final DataFlavor byteArrayInputStreamFlavor;
    private static final DataFlavor[] flavors;

    static {
        try {
            byteArrayInputStreamFlavor = new DataFlavor("application/octet-stream; class=java.io.ByteArrayInputStream");
            flavors = new DataFlavor[]{byteArrayInputStreamFlavor};
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return byteArrayInputStreamFlavor.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (byteArrayInputStreamFlavor.equals(flavor)) {
            return new ByteArrayInputStream(data);
        }

        throw new UnsupportedFlavorException(flavor);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // we don't care
    }
}
