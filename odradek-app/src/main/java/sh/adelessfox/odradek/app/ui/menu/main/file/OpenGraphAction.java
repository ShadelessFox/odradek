package sh.adelessfox.odradek.app.ui.menu.main.file;

import sh.adelessfox.odradek.app.ui.editors.ObjectEditorInput;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.game.decima.DecimaGame;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

@ActionRegistration(text = "Open Streaming Graph", description = "Open the streaming graph resource")
@ActionContribution(parent = MainMenu.File.ID)
public class OpenGraphAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var game = context.require(DataKeys.GAME, DecimaGame.class);
        var editors = context.require(DataKeys.EDITOR_MANAGER);
        editors.openEditor(new ObjectEditorInput(
            game,
            game.streamingGraph().resource(),
            new ObjectId(0, 0)));
    }
}
