package sh.adelessfox.odradek.app.ui.menu.main.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.actions.ActionContext;
import sh.adelessfox.odradek.ui.actions.ActionContribution;
import sh.adelessfox.odradek.ui.actions.ActionRegistration;
import sh.adelessfox.odradek.ui.util.Dialogs;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@ActionRegistration(text = "Export Group Remap\u2026", description = "Export streaming graph group index to id remap table")
@ActionContribution(parent = MainMenu.Tools.ID, order = 0)
public class ExportGroupRemapAction extends Action {
    private static final Logger log = LoggerFactory.getLogger(ExportGroupRemapAction.class);
    private static Path lastPath;

    @Override
    public void perform(ActionContext context) {
        var output = chooseOutputPath().orElse(null);
        if (output == null) {
            return;
        }

        try (var writer = Files.newBufferedWriter(output)) {
            writer.write("GroupIndex,GroupId");

            var game = Application.getInstance().game();
            var groups = game.streamingGraph().groups();
            for (int i = 0; i < groups.size(); i++) {
                writer.newLine();
                writer.write("%d,%d".formatted(i, groups.get(i).id()));
            }

            lastPath = output.getParent();
            JOptionPane.showMessageDialog(
                JOptionPane.getRootFrame(),
                "Exported %d rows to %s".formatted(groups.size(), output),
                "Export complete",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            log.error("Failed to export group remap table to CSV", e);
            Dialogs.showExceptionDialog(JOptionPane.getRootFrame(), "Unable to export remap table", e);
        }
    }

    private static Optional<Path> chooseOutputPath() {
        var chooser = new JFileChooser();
        chooser.setDialogTitle("Save remap table");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        chooser.setAcceptAllFileFilterUsed(false);

        var path = lastPath != null ? lastPath.resolve("group-remap.csv").toFile() : new File("group-remap.csv");
        chooser.setSelectedFile(path);

        if (chooser.showSaveDialog(JOptionPane.getRootFrame()) != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        } else {
            return Optional.of(chooser.getSelectedFile().toPath());
        }
    }
}
