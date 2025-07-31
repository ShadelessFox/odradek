package sh.adelessfox.odradek.app.component.main;

import sh.adelessfox.odradek.app.ObjectStructure;
import sh.adelessfox.odradek.ui.tree.StructuredTree;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

class ObjectTreeTransferHandler extends TransferHandler {
    @Override
    protected Transferable createTransferable(JComponent c) {
        var tree = (StructuredTree<?>) c;
        if (tree.getSelectionPathComponent() instanceof ObjectStructure structure) {
            var string = ObjectStructure.getValueString(structure);
            if (string.isPresent()) {
                return new StringSelection(string.get());
            }
        }
        return super.createTransferable(c);
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }
}
