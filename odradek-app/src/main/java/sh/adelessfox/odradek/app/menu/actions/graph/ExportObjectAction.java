package sh.adelessfox.odradek.app.menu.actions.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.Gatherers;
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
import java.nio.file.Files;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
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
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        if (converters.values().stream().anyMatch(matches -> types.size() != matches)) {
            // At least one selected object can't be exported using the same converter as the others
            return Stream.empty();
        }

        return converters.keySet().stream()
            .flatMap(converter -> Exporter.exporters(converter.resultType())
                .map(exporter -> new Batch(selection, converter, exporter)))
            .map(pipeline -> (Batch<?>) pipeline)
            .sorted(Comparator.comparing(pipeline -> pipeline.exporter().name()));
    }

    private static Action action(Batch<?> pipeline) {
        return Action.builder()
            .perform(context -> exportBatch(context, pipeline))
            .text(_ -> Optional.of(pipeline.exporter().name()))
            .build();
    }

    private static <T> void exportBatch(ActionContext context, Batch<T> batch) {
        var chooser = new JFileChooser();
        chooser.setDialogTitle("Specify output directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showSaveDialog(JOptionPane.getRootFrame()) != JFileChooser.APPROVE_OPTION) {
            log.debug("Export cancelled by user");
            return;
        }

        var game = context.get(DataKeys.GAME, ForbiddenWestGame.class).orElseThrow();
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
