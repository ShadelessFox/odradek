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
import sh.adelessfox.odradek.rtti.data.TypedObject;

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
    private Path output;

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

            switch (exporter) {
                case Exporter.OfSingleOutput<Object> e -> {
                    var path = output.resolve(makeObjectName(id, object, e));
                    try (var channel = Files.newByteChannel(path, WRITE, CREATE, TRUNCATE_EXISTING)) {
                        e.export(converted, channel);
                        log.debug("Exported object {} ({}) to {}", object, object.getType(), path);
                    } catch (Exception ex) {
                        log.error("  Failed to export object {} ({}); skipping", object, object.getType(), ex);
                    }
                }
                case Exporter.OfMultipleOutputs<Object> e -> {
                    var path = output.resolve(makeObjectName(id, object));
                    try (var provider = new Exporter.OfMultipleOutputs.DefaultOutputProvider(path)) {
                        e.export(converted, provider);
                        log.debug("Exported object {} ({}) to {}", object, object.getType(), path);
                    } catch (Exception ex) {
                        log.error("  Failed to export object {} ({}); skipping", object, object.getType(), ex);
                    }
                }
            }
        }
    }

    private static String makeObjectName(ObjectId id, TypedObject object) {
        return "%s_%s_%s".formatted(object.getType(), id.groupId(), id.objectIndex());
    }

    private static String makeObjectName(ObjectId id, TypedObject object, Exporter.OfSingleOutput<?> exporter) {
        return makeObjectName(id, object) + '.' + exporter.extension();
    }

    private static String supportedFormats() {
        return Exporter.exporters()
            .map(Exporter::id)
            .collect(Collectors.joining(", "));
    }
}
