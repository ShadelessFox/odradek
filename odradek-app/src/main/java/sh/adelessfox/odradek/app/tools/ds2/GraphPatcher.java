package sh.adelessfox.odradek.app.tools.ds2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.decima.StreamingGraph;
import sh.adelessfox.odradek.game.ds2.game.DS2Game;
import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.game.ds2.rtti.DS2TypeFactory;
import sh.adelessfox.odradek.game.ds2.rtti.DS2TypeWriter;
import sh.adelessfox.odradek.io.BinaryWriter;
import sh.adelessfox.odradek.io.BytesBinaryWriter;
import sh.adelessfox.odradek.io.DirectStorageWriter;
import sh.adelessfox.odradek.rtti.data.TypedObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public final class GraphPatcher {
    private static final Logger log = LoggerFactory.getLogger(GraphPatcher.class);

    private final DS2Game game;

    private final BytesBinaryWriter buffer = new BytesBinaryWriter();
    private final List<CorePatch> patches = new ArrayList<>();

    public GraphPatcher(DS2Game game) {
        this.game = game;
    }

    @SuppressWarnings("unchecked")
    public <T extends TypedObject> void patchObject(ObjectId id, Consumer<T> patcher) throws IOException {
        var spans = new ArrayList<StreamingGraph.Span>();
        var objects = game.readGroup(id.groupId(), true, spans);

        var span = spans.get(id.objectIndex());
        var object = objects.get(id.objectIndex());

        // Patch the object
        patcher.accept((T) object);

        // Serialize the object
        var offset = buffer.position();
        new DS2TypeWriter().write(object, object.getType(), buffer);
        var length = Math.toIntExact(buffer.position() - offset);

        patches.add(new CorePatch(
            id,
            span.fileIndex(),
            span.offset(),
            span.length(),
            offset,
            length));
    }

    public void persist() throws IOException {
        var graph = (DS2.StreamingGraphResource) game.streamingGraph().resource();

        // ensure we have mutable lists to modify
        graph.files(new ArrayList<>(graph.files()));
        graph.packFileLengths(new ArrayList<>(graph.packFileLengths()));
        graph.packFileOffsets(new ArrayList<>(graph.packFileOffsets()));
        graph.spanTable(new ArrayList<>(graph.spanTable()));

        applyCorePatches(graph);

        var path = game.resolvePath("cache:package/streaming_graph.core");
        log.debug("Patching {}", path);
        writeCoreFile(path, graph, game.getTypeFactory());
    }

    private void applyCorePatches(DS2.StreamingGraphResource graph) throws IOException {
        int patchFileIndex = graph.files().size();
        graph.files().add("cache:package/patch/odradek.00.00.core");
        graph.packFileLengths().add(new int[0]);
        graph.packFileOffsets().add(new int[0]);

        for (var patch : patches) {
            applyCorePatch(patch, graph, patchFileIndex);
        }

        var path = game.resolvePath(graph.files().get(patchFileIndex));
        log.debug("Writing patched package to {}", path);
        Files.createDirectories(path.getParent());
        try (var writer = new DirectStorageWriter(BinaryWriter.open(path), buffer.position())) {
            writer.writeBytes(buffer.toByteArray());
        }
    }

    private void applyCorePatch(CorePatch patch, DS2.StreamingGraphResource graph, int patchFileIndex) {
        var fileIndex = patch.fileIndex();
        var sourceOffset = patch.sourceOffset();
        var sourceSize = patch.sourceSize();
        var patchOffset = patch.patchOffset();
        var patchSize = patch.patchSize();

        log.debug(
            "Patching object {}: file {}, offset {} ({} bytes) -> {} ({} bytes)",
            patch.id(),
            graph.files().get(fileIndex),
            sourceOffset,
            sourceSize,
            patchSize,
            "%+d".formatted(patchSize - sourceSize));

        var group = (DS2.StreamingGroupData) game.streamingGraph().group(patch.id().groupId()).resource();
        var span = IntStream.range(group.spanStart(), group.spanStart() + group.spanCount())
            .mapToObj(i -> graph.spanTable().get(i))
            .filter(s -> s.fileIndex() == fileIndex && s.contains(sourceOffset, sourceSize))
            .findFirst().orElseThrow(() -> new IllegalStateException("Could not find span to patch!"));

        var split = split(
            span,
            createSpan(patchFileIndex, Math.toIntExact(patchOffset), patchSize, true),
            sourceOffset,
            sourceSize);

        var oldSpans = graph.spanTable().subList(group.spanStart(), group.spanStart() + group.spanCount());
        var newSpans = new ArrayList<>(oldSpans);

        int oldSpanIndex = newSpans.indexOf(span);
        newSpans.remove(oldSpanIndex);
        newSpans.addAll(oldSpanIndex, split);

        int spanStart = graph.spanTable().size();
        int spanCount = newSpans.size();

        group.spanStart(spanStart);
        group.spanCount(spanCount);
        group.groupSize(group.groupSize() + (patchSize - sourceSize));

        graph.spanTable().addAll(newSpans);
    }

    private static void writeCoreFile(Path path, TypedObject resource, DS2TypeFactory factory) throws IOException {
        try (var writer = BinaryWriter.open(path)) {
            // Object data
            writer.position(12);
            new DS2TypeWriter().write(resource, writer);
            var length = writer.position() - 12;

            // Object header
            writer.position(0);
            writer.writeLong(factory.getId(resource.getType()).hash());
            writer.writeInt(Math.toIntExact(length));

            // Links, unused in retail game
            writer.position(length + 12);
            writer.writeInt(0);
        }
    }

    private List<DS2.StreamingSourceSpan> split(
        DS2.StreamingSourceSpan original,
        DS2.StreamingSourceSpan patch,
        long offset,
        int length
    ) {
        var end = offset + length;
        if (offset > original.end() || end > original.end()) {
            throw new IllegalArgumentException("Inserted range is outside original");
        }

        List<DS2.StreamingSourceSpan> result = new ArrayList<>(3);

        var leftLength = offset - original.offset();
        if (leftLength > 0) {
            result.add(createSpan(
                original.fileIndex(),
                original.offset(),
                Math.toIntExact(leftLength),
                original.isPatch()));
        }

        result.add(patch);

        var rightLength = original.end() - end;
        if (rightLength > 0) {
            result.add(createSpan(
                original.fileIndex(),
                Math.toIntExact(end),
                Math.toIntExact(rightLength),
                original.isPatch()));
        }

        return List.copyOf(result);
    }

    private DS2.StreamingSourceSpan createSpan(int fileIndex, int offset, int length, boolean isPatch) {
        var factory = game.getTypeFactory();

        var span = factory.newInstance(DS2.StreamingSourceSpan.class);
        span.fileIndex(fileIndex);
        span.offset(Math.toIntExact(offset));
        span.length(Math.toIntExact(length));
        span.isPatch(isPatch);

        return span;
    }

    private record CorePatch(
        ObjectId id,
        int fileIndex,
        long sourceOffset, int sourceSize,
        long patchOffset, int patchSize
    ) {
    }
}
