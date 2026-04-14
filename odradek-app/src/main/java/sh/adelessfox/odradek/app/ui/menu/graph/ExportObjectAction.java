package sh.adelessfox.odradek.app.ui.menu.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.editors.ObjectEditorMenu;
import sh.adelessfox.odradek.app.ui.menu.MenuIds;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.ObjectSupplier;
import sh.adelessfox.odradek.ui.actions.*;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.actions.EditorMenu;
import sh.adelessfox.odradek.ui.util.Dialogs;
import sh.adelessfox.odradek.util.Gatherers;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.*;

@ActionRegistration(id = ExportObjectAction.ID, text = "&Export As\u2026", icon = "fugue:blue-document-export", keystroke = "ctrl E")
@ActionContribution(parent = GraphMenu.ID, group = MenuIds.GROUP_EXPORT)
@ActionContribution(parent = MainMenu.File.ID, group = MenuIds.GROUP_EXPORT)
@ActionContribution(parent = EditorMenu.ID, group = MenuIds.GROUP_EXPORT)
@ActionContribution(parent = ObjectEditorMenu.TOOLBAR_ID)
public class ExportObjectAction extends Action {
    public static final String ID = "sh.adelessfox.odradek.app.menu.graph.ExportObjectAction";

    private static final Logger log = LoggerFactory.getLogger(ExportObjectAction.class);
    private static Path lastPath;

    @ActionRegistration(text = "")
    @ActionContribution(parent = ExportObjectAction.ID)
    public static class Placeholder extends Action implements ActionProvider {
        @Override
        public List<Action> create(ActionContext context) {
            var game = context.get(DataKeys.GAME).orElseThrow();
            return exporters(context)
                .gather(Gatherers.groupingBy(x -> x.exporter().namespace().orElse("")))
                .sorted(Map.Entry.comparingByKey())
                .map(e -> (Action) new GroupAction(e.getValue().stream()
                    .map(batch -> action(batch, game))
                    .toList()))
                .toList();
        }

        @Override
        public boolean isList() {
            return true;
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
            .gather(Gatherers.instanceOf(ObjectSupplier.class))
            .toList();

        if (selection.isEmpty()) {
            return Stream.empty();
        }

        var types = selection.stream()
            .map(ObjectSupplier::objectType)
            .distinct()
            .toList();

        var converters = types.stream()
            .flatMap(Converter::converters)
            .gather(Gatherers.groupingBy(Function.identity(), Collectors.counting()))
            .filter(entry -> entry.getValue() == types.size())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return converters.keySet().stream()
            .flatMap(converter -> Exporter.exporters(converter.outputType())
                .map(exporter -> new Batch(selection, converter, exporter)))
            .map(pipeline -> (Batch<?>) pipeline)
            .sorted(Comparator.comparing(pipeline -> pipeline.exporter().name()));
    }

    private static Action action(Batch<?> batch, Game game) {
        return Action.builder()
            .perform(_ -> exportBatch(batch, game))
            .text(_ -> Optional.of(batch.exporter().name()))
            .icon(_ -> batch.exporter().icon())
            .build();
    }

    private static <T> void exportBatch(Batch<T> batch, Game game) {
        var singleFile = batch.objects().size() == 1;
        var exporter = batch.exporter();
        var chooser = new JFileChooser();

        if (singleFile) {
            var name = makeObjectName(exporter, batch.objects().getFirst());
            var path = lastPath != null ? lastPath.resolve(name).toFile() : new File(name);

            chooser.setDialogTitle("Specify output name");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new FileNameExtensionFilter(exporter.name(), exporter.extension()));
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setSelectedFile(path);
        } else {
            var path = lastPath != null ? lastPath.toFile() : new File("");

            chooser.setDialogTitle("Specify output directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setSelectedFile(path);
        }

        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            log.debug("Export cancelled by user");
            return;
        }

        var output = chooser.getSelectedFile().toPath();
        int exported = 0;

        lastPath = singleFile ? output.getParent() : output;

        for (ObjectSupplier selection : batch.objects()) {
            try {
                var object = selection.readObject(game);
                var type = object.getType();
                var path = singleFile ? output : output.resolve(makeObjectName(exporter, selection));

                var converted = batch.converter().convert(object, game);
                if (converted.isEmpty()) {
                    log.debug("Unable to convert object {} ({}) to {}", object, type, path);
                    continue;
                }

                try (var channel = Files.newByteChannel(path, WRITE, CREATE, TRUNCATE_EXISTING)) {
                    exporter.export(converted.get(), channel);
                } catch (Exception e) {
                    Files.delete(path);
                    throw e;
                }

                log.debug("Exported object {} ({}) to {}", object, type, path);
                exported++;
            } catch (Exception e) {
                log.error("Failed to export object {} ({})", selection.objectId(), selection.objectType(), e);
                Dialogs.showExceptionDialog(
                    JOptionPane.getRootFrame(),
                    "Unable to export object " + selection.objectId(),
                    e);
            }
        }

        if (batch.objects().size() == exported) {
            JOptionPane.showMessageDialog(
                JOptionPane.getRootFrame(),
                "All selected objects were successfully exported.",
                "Export successful",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(
                JOptionPane.getRootFrame(),
                "%d out of %d selected objects were successfully exported.\nFor more information, see the console output.".formatted(
                    exported,
                    batch.objects().size()),
                "Export completed",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private static String makeObjectName(Exporter<?> exporter, ObjectSupplier object) {
        return "%s_%s_%s.%s".formatted(
            object.objectType(),
            object.objectId().groupId(),
            object.objectId().objectIndex(),
            exporter.extension());
    }

    private record Batch<R>(List<ObjectSupplier> objects, Converter<Object, R, Game> converter, Exporter<R> exporter) {
    }

    private static final class GroupAction extends Action implements ActionProvider {
        private final List<Action> actions;

        GroupAction(List<Action> actions) {
            this.actions = actions;
        }

        @Override
        public List<Action> create(ActionContext context) {
            return actions;
        }

        @Override
        public boolean isList() {
            return true;
        }
    }
}
