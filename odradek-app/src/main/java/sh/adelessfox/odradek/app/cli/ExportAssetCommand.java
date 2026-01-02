package sh.adelessfox.odradek.app.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import sh.adelessfox.odradek.game.Converter;
import sh.adelessfox.odradek.game.Exporter;
import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.rtti.runtime.TypedObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.*;

@Command(name = "export", description = "Export one or more objects in the specified format", showDefaultValues = true)
public class ExportAssetCommand extends AbstractCommand {
    private static final Logger log = LoggerFactory.getLogger(ExportAssetCommand.class);
    @Parameters(arity = "1..", description = "The objects to process in a form of <groupId:objectIndex>, e.g. 2981:65")
    private List<ObjectId> objects;

    @Option(names = {"-f", "--format"}, description = "The output format for the objects. All objects must be convertible to the specified format. Omit this option to see the complete list of supported formats.")
    private String format;

    @Option(names = {"-o", "--output"}, description = "The output directory to save exported files to.", required = true)
    private Path path;

    @Override
    void execute(Game game) {
        if (format == null) {
            log.info("No format specified for export; supported formats are: {}", supportedFormats());
            return;
        }

        @SuppressWarnings("unchecked")
        var exporter = (Exporter<Object>) Exporter.exporter(format).orElse(null);
        if (exporter == null) {
            log.info("Unknown exporter specified: {}; supported formats are: {}", format, supportedFormats());
            return;
        }

        for (ObjectId id : objects) {
            log.info("Exporting object {}", id);

            TypedObject object;
            try {
                object = game.readObject(id.groupId(), id.objectIndex());
            } catch (IOException e) {
                log.error("  Error during read; skipping", e);
                continue;
            }

            Object converted;
            try {
                converted = Converter.convert(object, exporter.supportedType(), game).orElse(null);
            } catch (Exception e) {
                log.error("  Error during conversion; skipping", e);
                continue;
            }

            if (converted == null) {
                log.info("  Nothing were converted; skipping");
                continue;
            }

            var name = formatFileName(id, object, exporter);
            var target = path.resolve(name);

            try (var channel = Files.newByteChannel(target, WRITE, CREATE, TRUNCATE_EXISTING)) {
                exporter.export(converted, channel);
            } catch (Exception e) {
                log.error("  Error during export; skipping", e);
            }
        }
    }

    private static String formatFileName(ObjectId id, TypedObject object, Exporter<?> exporter) {
        return "%s_%s_%s.%s".formatted(
            object.getType().name(),
            id.groupId(),
            id.objectIndex(),
            exporter.extension()
        );
    }

    private static String supportedFormats() {
        return Exporter.exporters()
            .map(Exporter::id)
            .collect(Collectors.joining(", "));
    }
}
