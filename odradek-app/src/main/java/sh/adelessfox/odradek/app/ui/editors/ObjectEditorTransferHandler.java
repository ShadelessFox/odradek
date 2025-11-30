package sh.adelessfox.odradek.app.ui.editors;

import sh.adelessfox.odradek.ui.components.tree.StructuredTree;

import javax.swing.*;
import java.awt.datatransfer.Transferable;

class ObjectEditorTransferHandler extends TransferHandler {
    @Override
    protected Transferable createTransferable(JComponent c) {
        var tree = (StructuredTree<?>) c;
        if (tree.getSelectionPathComponent() instanceof ObjectStructure structure) {
            // var valueBuilder = ObjectEditor.valueTextBuilder(false).orElse(null);
            // if (valueBuilder != null) {
            //     var builder = StyledText.builder();
            //     valueBuilder.accept(builder);
            //     return new StringSelection(builder.build().toString());
            // }
        }
        return super.createTransferable(c);
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }
}
