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
import sh.adelessfox.odradek.util.Gatherers;

import javax.swing.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.*;

@ActionRegistration(id = ExportObjectAction.ID, text = "&Export As\u2026\u3000\u3000\u3000Ctrl+E", keystroke = "ctrl E")
@ActionContribution(parent = ActionIds.GRAPH_MENU_ID)
@ActionContribution(parent = ActionIds.FILE_MENU_ID, group = "2000,Export")
public class ExportObjectAction extends Action {
    public static final String ID = "sh.adelessfox.odradek.app.menu.actions.graph.ExportObjectAction";

    private static final Logger log = LoggerFactory.getLogger(ExportObjectAction.class);

    @ActionRegistration(text = "")
    @ActionContribution(parent = ExportObjectAction.ID)
    public static class Placeholder extends Action implements ActionProvider {
        @Override
        public List<Action> create(ActionContext context) {
            var game = context.get(DataKeys.GAME, ForbiddenWestGame.class).orElseThrow();
            return exporters(context)
                .map(batch -> action(game, batch))
                .toList();
        }
    }

    @Override
    public boolean isVisible(ActionContext context) {
        return exporters(context).findAny().isPresent();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Stream<? extends Batch<?>> exporters(ActionContext context) {
        var selection = context.get(DataKeys.SELECTION_LIST).stream()
            .flatMap(Collection::stream)
            .gather(Gatherers.instanceOf(GroupObject.class))
            .toList();

        var types = selection.stream()
            .map(object -> object.type().instanceType())
            .distinct()
            .toList();

        var converters = types.stream()
            .flatMap(Converter::converters)
            .gather(Gatherers.groupingBy(Function.identity(), Collectors.counting()))
            .filter(entry -> entry.getValue() == types.size())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return converters.keySet().stream()
            .flatMap(converter -> Exporter.exporters(converter.resultType())
                .map(exporter -> new Batch(selection, converter, exporter)))
            .map(pipeline -> (Batch<?>) pipeline)
            .sorted(Comparator.comparing(pipeline -> pipeline.exporter().name()));
    }

    private static Action action(ForbiddenWestGame game, Batch<?> batch) {
        return Action.builder()
            .perform(_ -> exportBatch(game, batch))
            .text(_ -> Optional.of(batch.exporter().name()))
            .build();
    }

    private static <T> void exportBatch(ForbiddenWestGame game, Batch<T> batch) {
        var chooser = new JFileChooser();
        chooser.setDialogTitle("Specify output directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showSaveDialog(JOptionPane.getRootFrame()) != JFileChooser.APPROVE_OPTION) {
            log.debug("Export cancelled by user");
            return;
        }

        var directory = chooser.getSelectedFile().toPath();
        int exported = 0;

        for (GroupObject selection : batch.objects()) {
            try {
                var group = game.getStreamingReader().readGroup(selection.group().groupID());
                var object = group.objects().get(selection.index()).object();

                var path = directory.resolve("%s.%s".formatted(
                    object.general().objectUUID().toDisplayString(),
                    batch.exporter().extension()
                ));

                var converted = batch.converter().convert(object, game);
                if (converted.isEmpty()) {
                    log.debug("Unable to convert object {} ({}) to {}", object.general().objectUUID(), object.getType(), path);
                    continue;
                }

                try (var channel = Files.newByteChannel(path, WRITE, CREATE, TRUNCATE_EXISTING)) {
                    batch.exporter().export(converted.get(), channel);
                }

                log.debug("Exported object {} ({}) to {}", object.general().objectUUID(), object.getType(), path);
                exported++;
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

        if (batch.objects().size() == exported) {
            JOptionPane.showMessageDialog(
                JOptionPane.getRootFrame(),
                "All selected objects were successfully exported.",
                "Export successful",
                JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                JOptionPane.getRootFrame(),
                "%d out of %d selected objects were successfully exported.\nFor more information, see the console output.".formatted(
                    exported,
                    batch.objects().size()),
                "Export completed",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private record Batch<T>(List<GroupObject> objects, Converter<Game, T> converter, Exporter<T> exporter) {
    }
}
