package sh.adelessfox.odradek.app.ui.menu.object;

import sh.adelessfox.odradek.app.ui.editors.ObjectStructure;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.util.ByteContents;

import javax.swing.*;
import java.awt.*;

@ActionRegistration(text = "Copy to clipboard", description = "Copy bytes to clipboard as binary data")
@ActionContribution(parent = ObjectMenu.ID)
public class CopyBytesToClipboardAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var game = context.get(DataKeys.GAME).orElseThrow();
        var structure = context.get(DataKeys.SELECTION, ObjectStructure.class).orElseThrow();
        var bytes = Converter.convert(structure.type(), structure.value(), game, byte[].class);

        if (bytes.isPresent()) {
            var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            var contents = new ByteContents(bytes.get());
            clipboard.setContents(contents, contents);
        } else {
            JOptionPane.showMessageDialog(
                JOptionPane.getRootFrame(),
                "Unable to copy data to clipboard",
                "Copy to clipboard",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return context.get(DataKeys.SELECTION, ObjectStructure.class)
            .flatMap(structure -> Converter.converter(structure.type(), byte[].class))
            .isPresent();
    }
}
