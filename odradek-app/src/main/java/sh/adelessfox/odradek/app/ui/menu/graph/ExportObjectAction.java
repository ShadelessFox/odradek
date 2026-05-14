package sh.adelessfox.odradek.app.ui.menu.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.ui.component.bookmarks.menu.BookmarkMenu;
import sh.adelessfox.odradek.app.ui.editors.ObjectEditorMenu;
import sh.adelessfox.odradek.app.ui.menu.MenuIds;
import sh.adelessfox.odradek.app.ui.menu.main.MainMenu;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.game.Exporter.OfMultipleOutputs.DefaultOutputProvider;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.decima.DecimaGame;
import sh.adelessfox.odradek.game.decima.ObjectIdHolder;
import sh.adelessfox.odradek.ui.actions.*;
import sh.adelessfox.odradek.ui.actions.Action;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.editors.actions.EditorMenu;
import sh.adelessfox.odradek.ui.util.ProgressMonitor;
import sh.adelessfox.odradek.util.Gatherers;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.*;

@ActionRegistration(id = ExportObjectAction.ID, text = "&Export As\u2026", icon = "fugue:blue-document-export", keystroke = "ctrl E")
@ActionContribution(parent = BookmarkMenu.ID, group = MenuIds.GROUP_EXPORT)
@ActionContribution(parent = EditorMenu.ID, group = MenuIds.GROUP_EXPORT)
@ActionContribution(parent = GraphMenu.ID, group = MenuIds.GROUP_EXPORT)
@ActionContribution(parent = MainMenu.File.ID, group = MenuIds.GROUP_EXPORT)
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
            var game = context.get(DataKeys.GAME, DecimaGame.class).orElseThrow();
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

    private static Stream<? extends Batch<?>> exporters(ActionContext context) {
        var game = context.get(DataKeys.GAME, DecimaGame.class).orElse(null);
        if (game == null) {
            return Stream.empty();
        }

        var selection = context.get(DataKeys.SELECTION_LIST).stream()
            .flatMap(Collection::stream)
            .gather(Gatherers.instanceOf(ObjectIdHolder.class))
            .toList();

        if (selection.isEmpty()) {
            return Stream.empty();
        }

        var types = selection.stream()
            .map(holder -> holder.objectType(game))
            .distinct()
            .toList();

        var converters = types.stream()
            .flatMap(Converter::converters)
            .gather(Gatherers.groupingBy(Function.identity(), Collectors.counting()))
            .filter(entry -> entry.getValue() == types.size())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return converters.keySet().stream()
            .flatMap(converter -> Exporter.exporters(converter.outputType())
                .map(exporter -> new Batch<>(selection, converter, exporter)))
            .map(pipeline -> (Batch<?>) pipeline)
            .sorted(Comparator.comparing(pipeline -> pipeline.exporter().name()));
    }

    private static Action action(Batch<?> batch, DecimaGame game) {
        return Action.builder()
            .perform(_ -> exportBatch(batch, game))
            .text(_ -> Optional.of(batch.exporter().name()))
            .icon(_ -> batch.exporter().icon())
            .build();
    }

    private static <T> void exportBatch(Batch<T> batch, DecimaGame game) {
        var singleFile = batch.objects().size() == 1 && batch.exporter() instanceof Exporter.OfSingleOutput<?>;
        var output = chooseOutputPath(batch, game, singleFile);

        if (output != null) {
            lastPath = singleFile ? output.getParent() : output;
            new ExportWorker<>(batch, game, output, singleFile).execute();
        }
    }

    private static Path chooseOutputPath(Batch<?> batch, DecimaGame game, boolean singleFile) {
        var chooser = new JFileChooser();

        if (singleFile) {
            var exporter1 = (Exporter.OfSingleOutput<?>) batch.exporter();
            var name = makeObjectName(batch.objects().getFirst(), game, exporter1);
            var path = lastPath != null ? lastPath.resolve(name).toFile() : new File(name);

            chooser.setDialogTitle("Specify output name");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new FileNameExtensionFilter(batch.exporter().name(), exporter1.extension()));
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setSelectedFile(path);
        } else {
            var path = lastPath != null ? lastPath.toFile() : new File("");

            chooser.setDialogTitle("Specify output directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setSelectedFile(path);
        }

        if (chooser.showSaveDialog(JOptionPane.getRootFrame()) != JFileChooser.APPROVE_OPTION) {
            log.debug("Export cancelled by user");
            return null;
        }

        return chooser.getSelectedFile().toPath();
    }

    private static String makeObjectName(ObjectIdHolder object, DecimaGame game) {
        return "%s_%s_%s".formatted(
            object.objectType(game),
            object.objectId().groupId(),
            object.objectId().objectIndex());
    }

    private static String makeObjectName(ObjectIdHolder object, DecimaGame game, Exporter.OfSingleOutput<?> exporter) {
        return makeObjectName(object, game) + '.' + exporter.extension();
    }

    private record Batch<R>(List<ObjectIdHolder> objects, Converter<Object, R, Game> converter, Exporter<R> exporter) {
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

    private static final class ExportWorker<T> extends SwingWorker<Integer, ObjectIdHolder> {
        private final Batch<T> batch;
        private final DecimaGame game;
        private final Path output;
        private final boolean singleFile;

        private final AtomicInteger exported = new AtomicInteger();
        private final ProgressMonitor monitor;

        ExportWorker(Batch<T> batch, DecimaGame game, Path output, boolean singleFile) {
            this.batch = batch;
            this.game = game;
            this.output = output;
            this.singleFile = singleFile;
            this.monitor = new ProgressMonitor(JOptionPane.getRootFrame(), "Exporting ...", 0, batch.objects().size());
        }

        @Override
        protected Integer doInBackground() {
            batch.objects().parallelStream()
                .gather(Gatherers.groupingBy(holder -> holder.objectId().groupId()))
                .forEach(group -> group.getValue().parallelStream()
                    .forEach(this::export));
            return exported.get();
        }

        @Override
        protected void process(List<ObjectIdHolder> chunks) {
            var object = chunks.getLast();
            monitor.setText("Exporting %s (%s)".formatted(object.objectType(game), object.objectId()));
            monitor.setProgress(exported.get());
        }

        @Override
        protected void done() {
            monitor.close();

            if (batch.objects().size() == exported.get()) {
                JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(),
                    "All selected objects were successfully exported.",
                    "Export successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(
                    JOptionPane.getRootFrame(),
                    "%d out of %d selected objects were successfully exported.\nFor more information, see the console output.".formatted(
                        exported.get(),
                        batch.objects().size()),
                    "Export completed",
                    JOptionPane.WARNING_MESSAGE);
            }
        }

        private void export(ObjectIdHolder object) {
            if (monitor.isCanceled()) {
                // Not quite a proper termination though, but works
                return;
            }

            publish(object);

            try {
                boolean success = export(object, batch.converter(), batch.exporter(), game, output, singleFile);
                if (success) {
                    exported.incrementAndGet();
                }
            } catch (Exception e) {
                log.error("Failed to export object {} ({})", object.objectId(), object.objectType(game), e);
            }
        }

        private static <T> boolean export(
            ObjectIdHolder selection,
            Converter<Object, T, Game> converter,
            Exporter<T> exporter,
            DecimaGame game,
            Path output,
            boolean singleFile
        ) throws IOException {
            var object = game.readObject(selection.objectId());
            var type = object.getType();

            var converted = converter.convert(object, game);
            if (converted.isEmpty()) {
                log.debug("Unable to convert object {} ({})", object, type);
                return false;
            }

            switch (exporter) {
                case Exporter.OfSingleOutput<T> e -> {
                    var path = singleFile ? output : output.resolve(makeObjectName(selection, game, e));
                    try (var channel = Files.newByteChannel(path, WRITE, CREATE, TRUNCATE_EXISTING)) {
                        e.export(converted.get(), channel);
                        log.debug("Exported object {} ({}) to {}", object, type, path);
                    } catch (Exception ex) {
                        Files.delete(path);
                        throw ex;
                    }
                }
                case Exporter.OfMultipleOutputs<T> e -> {
                    var path = output.resolve(makeObjectName(selection, game));
                    try (var provider = new DefaultOutputProvider(path)) {
                        e.export(converted.get(), provider);
                    }
                }
            }

            return true;
        }
    }
}
