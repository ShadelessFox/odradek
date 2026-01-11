package sh.adelessfox.odradek.app.ui.menu.main.file;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.editors.ObjectEditorInput;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;

@ActionRegistration(text = "Open Streaming Graph", description = "Opens the streaming graph resource")
@ActionContribution(parent = MainMenu.File.ID)
public class OpenGraphAction extends Action {
    @Override
    public void perform(ActionContext context) {
        var application = Application.getInstance();
        var game = application.game();
        application.editors().openEditor(new ObjectEditorInput(game, game.getStreamingGraph().resource(), 0, 0));
    }
}
