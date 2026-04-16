package sh.adelessfox.odradek.app.ui.menu.object;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.editors.ObjectStructure;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@ActionRegistration(text = "Save blob as...", description = "Save bytes to a file as binary data")
@ActionContribution(parent = ObjectMenu.ID)
public class SaveBytesToFileAction extends Action {
    private static final Logger log = LoggerFactory.getLogger(SaveBytesToFileAction.class);

    @Override
    public void perform(ActionContext context) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Specify output filename");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setSelectedFile(new File("output.bin"));

        if (chooser.showSaveDialog(JOptionPane.getRootFrame()) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        var game = context.get(DataKeys.GAME).orElseThrow();
        var structure = context.get(DataKeys.SELECTION, ObjectStructure.class).orElseThrow();
        var bytes = Converter.convert(structure.type(), structure.value(), byte[].class, game);

        if (bytes.isPresent()) {
            try {
                Files.write(chooser.getSelectedFile().toPath(), bytes.get());
                return;
            } catch (IOException e) {
                log.error("Failed to save bytes to file", e);
            }
        }

        JOptionPane.showMessageDialog(
            JOptionPane.getRootFrame(),
            "Unable to copy data to clipboard",
            "Copy to clipboard",
            JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return context.get(DataKeys.SELECTION, ObjectStructure.class)
            .flatMap(structure -> Converter.converter(structure.type(), byte[].class))
            .isPresent();
    }
}
