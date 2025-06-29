package sh.adelessfox.odradek.app.menu.actions.object;

import sh.adelessfox.odradek.app.ObjectStructure;
import sh.adelessfox.odradek.app.menu.ActionIds;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.util.ByteContents;

import java.awt.*;

@ActionRegistration(text = "Copy to clipboard", description = "Copy bytes to clipboard as binary data")
@ActionContribution(parent = ActionIds.OBJECT_MENU_ID)
public class CopyBytesToClipboardAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var structure = context.get(DataKeys.SELECTION, ObjectStructure.class).orElseThrow();
        var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        var contents = new ByteContents((byte[]) structure.value());
        clipboard.setContents(contents, contents);
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return context.get(DataKeys.SELECTION, ObjectStructure.class)
            .filter(structure -> structure.value() instanceof byte[])
            .isPresent();
    }
}
