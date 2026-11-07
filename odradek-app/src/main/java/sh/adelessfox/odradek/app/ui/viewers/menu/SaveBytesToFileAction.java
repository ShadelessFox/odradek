package sh.adelessfox.odradek.app.ui.viewers.menu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.viewers.ObjectStructure;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.data.DataKeys;
import wtf.reversed.toolbox.collect.Bytes;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

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
        var node = context.get(DataKeys.SELECTION, ObjectStructure.Node.class).orElseThrow();
        var bytes = Converter.convert(node.type(), node.value(), Bytes.class, game);

        if (bytes.isPresent()) {
            try (var channel = Files.newByteChannel(chooser.getSelectedFile().toPath(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                var buffer = bytes.get().asBuffer();
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
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
        return context.get(DataKeys.SELECTION, ObjectStructure.Node.class)
            .flatMap(structure -> Converter.converter(structure.type(), Bytes.class))
            .isPresent();
    }
}
