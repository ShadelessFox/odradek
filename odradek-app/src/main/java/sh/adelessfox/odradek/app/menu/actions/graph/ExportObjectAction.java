package sh.adelessfox.odradek.app.menu.actions.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.GraphStructure.GroupObject;
import sh.adelessfox.odradek.app.menu.ActionIds;
import sh.adelessfox.odradek.export.Exporter;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.ui.actions.*;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.data.DataKeys;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.*;

@ActionRegistration(id = ExportObjectAction.ID, text = "&Export As\u2026")
@ActionContribution(parent = ActionIds.GRAPH_MENU_ID)
public class ExportObjectAction extends Action {
    public static final String ID = "sh.adelessfox.odradek.app.menu.actions.graph.ExportObjectAction";

    private static final Logger log = LoggerFactory.getLogger(ExportObjectAction.class);

    @ActionRegistration(text = "")
    @ActionContribution(parent = ExportObjectAction.ID)
    public static class Placeholder extends Action implements ActionProvider {
        @Override
        public List<Action> create(ActionContext context) {
            return exporters(context)
                .map(ExportObjectAction::action)
                .toList();
        }
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return exporters(context).findAny().isPresent();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Stream<? extends ExportPipeline<?>> exporters(ActionContext context) {
        var clazz = context.get(DataKeys.SELECTION, GroupObject.class)
            .map(object -> object.type().instanceType());

        if (clazz.isEmpty()) {
            return Stream.empty();
        }

        return clazz.stream()
            .flatMap(Converter::converters)
            .flatMap(converter -> Exporter.exporters(converter.resultType())
                .map(exporter -> new ExportPipeline(converter, exporter)))
            .map(pipeline -> (ExportPipeline<?>) pipeline)
            .sorted(Comparator.comparing(pipeline -> pipeline.exporter().name()));
    }

    private static Action action(ExportPipeline<?> pipeline) {
        return Action.builder()
            .perform(context -> doExportChecked(context, pipeline))
            .text(_ -> Optional.of(pipeline.exporter().name()))
            .build();
    }

    private static <T> void doExport(
        ForbiddenWestGame game,
        GroupObject selection,
        Converter<Game, T> converter,
        Exporter<T> exporter
    ) throws IOException {
        var chooser = new JFileChooser();
        chooser.setDialogTitle("Specify output file");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileNameExtensionFilter(exporter.name(), exporter.extension()));
        chooser.setSelectedFile(new File("exported." + exporter.extension()));

        if (chooser.showSaveDialog(JOptionPane.getRootFrame()) != JFileChooser.APPROVE_OPTION) {
            log.debug("Export cancelled by user");
            return;
        }

        var group = game.getStreamingReader().readGroup(selection.group().groupID());
        var object = group.objects().get(selection.index()).object();

        var converted = converter.convert(object, game);
        if (converted.isEmpty()) {
            JOptionPane.showMessageDialog(
                JOptionPane.getRootFrame(),
                "Failed to convert object " + object.getType() + " using " + exporter + ".\nThis may be due to unsupported object type.",
                "Unable to export object",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try (var channel = Files.newByteChannel(chooser.getSelectedFile().toPath(), WRITE, CREATE, TRUNCATE_EXISTING)) {
            exporter.export(converted.get(), channel);
        }

        JOptionPane.showMessageDialog(
            JOptionPane.getRootFrame(),
            "Exported to " + chooser.getSelectedFile(),
            "Export successful",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private static <T> void doExportChecked(ActionContext context, ExportPipeline<T> pipeline) {
        try {
            var game = context.get(DataKeys.GAME, ForbiddenWestGame.class).orElseThrow();
            var selection = context.get(DataKeys.SELECTION, GroupObject.class).orElseThrow();

            doExport(game, selection, pipeline.converter(), pipeline.exporter());
        } catch (Exception e) {
            log.error("Failed to export object", e);
            JOptionPane.showMessageDialog(
                JOptionPane.getRootFrame(),
                "Failed to export object: " + e.getMessage(),
                "Unable to export object",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private record ExportPipeline<T>(Converter<Game, T> converter, Exporter<T> exporter) {
    }
}
