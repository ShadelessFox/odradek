package sh.adelessfox.odradek.ui.util;

import java.awt.datatransfer.*;
import java.io.ByteArrayInputStream;
import java.util.HexFormat;

public record ByteContents(byte[] data) implements Transferable, ClipboardOwner {
    private static final DataFlavor byteArrayInputStreamFlavor = createByteArrayInputStreamFlavor();
    private static final DataFlavor stringFlavor = DataFlavor.stringFlavor;
    private static final DataFlavor[] flavors = {
        byteArrayInputStreamFlavor,
        stringFlavor
    };

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (DataFlavor f : flavors) {
            if (flavor.equals(f)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (flavor.equals(byteArrayInputStreamFlavor)) {
            return new ByteArrayInputStream(data);
        } else if (flavor.equals(stringFlavor)) {
            return HexFormat.of().formatHex(data);
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // we don't care
    }

    private static DataFlavor createByteArrayInputStreamFlavor() {
        try {
            return new DataFlavor("application/octet-stream; class=java.io.ByteArrayInputStream");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
