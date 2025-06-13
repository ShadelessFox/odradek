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

@ActionRegistration(name = "Copy to clipboard", description = "Copy bytes to clipboard as binary data")
@ActionContribution(parent = ActionIds.OBJECT_MENU_ID)
public class CopyBytesToClipboardAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var structure = (ObjectStructure) context.getData(DataKeys.SELECTION).orElseThrow();
        var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        var contents = new ByteContents((byte[]) structure.value());
        clipboard.setContents(contents, contents);
    }

    @Override
    public boolean isVisible(ActionContext context) {
        Object selection = context.getData(DataKeys.SELECTION).orElse(null);
        return selection instanceof ObjectStructure structure && structure.value() instanceof byte[];
    }
}
