package sh.adelessfox.odradek.game.hfw;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeWriter;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.game.hfw.storage.StreamingObjectReader;
import sh.adelessfox.odradek.io.BinaryWriter;
import sh.adelessfox.odradek.io.BytesBinaryWriter;
import sh.adelessfox.odradek.io.DirectStorageWriter;
import sh.adelessfox.odradek.rtti.data.TypedObject;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class GraphPatcher implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(GraphPatcher.class);

    private final ForbiddenWestGame game;

    private final BytesBinaryWriter buffer = new BytesBinaryWriter();
    private final List<Patch> patches = new ArrayList<>();

    public GraphPatcher(ForbiddenWestGame game) {
        this.game = game;
    }

    @SuppressWarnings("unchecked")
    public <T extends TypedObject> void patchObject(
        int groupId,
        int objectIndex,
        Consumer<T> patcher
    ) throws IOException {
        var result = game.getStreamingReader().readGroup(groupId, true, false);
        var object = result.objects().get(objectIndex);

        // Patch the object
        patcher.accept((T) object);

        // Serialize the object
        var position = buffer.position();
        new HFWTypeWriter().write(object, buffer);
        var length = Math.toIntExact(buffer.position() - position);

        patches.add(new Patch(
            result.group(),
            objectIndex,
            result.ranges().get(objectIndex),
            new StreamingObjectReader.Range(position, length)));
    }

    @Override
    public void close() throws IOException {
        var factory = game.getTypeFactory();
        var graph = game.getStreamingGraph().resource();

        graph.files(new ArrayList<>(graph.files()));
        graph.packFileLengths(new ArrayList<>(graph.packFileLengths()));
        graph.packFileOffsets(new ArrayList<>(graph.packFileOffsets()));
        graph.spanTable(new ArrayList<>(graph.spanTable()));

        int fileIndex = graph.files().size();
        graph.files().add("cache:package/patch/odradek.00.00.core");
        graph.packFileLengths().add(new int[0]);
        graph.packFileOffsets().add(new int[0]);

        for (Patch patch : patches) {
            var group = patch.group();
            var patchedRange = patch.patchedRange();
            var originalRange = patch.originalRange();

            log.debug(
                "Patching object {}:{}: offset {}, size {} -> {} ({})",
                group.groupID(), patch.objectIndex(),
                originalRange.offset(), originalRange.size(), patchedRange.size(),
                "%+d bytes".formatted(patchedRange.size() - originalRange.size()));

            // TODO ASSUMES that the very first span of a group is patched.
            var newSpan = factory.newInstance(HorizonForbiddenWest.StreamingSourceSpan.class);
            newSpan.fileIndexAndIsPatch(1 << 31 | fileIndex);
            newSpan.offset(Math.toIntExact(patchedRange.offset()));
            newSpan.length(patchedRange.size());

            int spanStart = graph.spanTable().size();
            graph.spanTable().add(newSpan);
            graph.spanTable().addAll(graph.spanTable()
                .subList(group.spanStart() + 1, group.spanStart() + group.spanCount()));

            group.spanStart(spanStart);
            group.spanCount(group.spanCount()); // remains the same in this particular instance
            group.groupSize(group.groupSize() + (patchedRange.size() - originalRange.size()));
        }

        var path = game.resolvePath(graph.files().get(fileIndex));
        log.debug("Writing package to {}", path);
        try (var writer = new DirectStorageWriter(BinaryWriter.open(path), buffer.position())) {
            writer.writeBytes(buffer.toByteArray());
        }

        log.debug("Patching streaming_graph");
        try (var writer = BinaryWriter.open(game.resolvePath("cache:package/streaming_graph.core"))) {
            var data = serialize(graph);
            writer.writeLong(0x929d7af6a30cd1c5L);
            writer.writeInt(data.length);
            writer.writeBytes(data);
            writer.writeInt(0 /* padding */);
        }
    }

    private byte[] serialize(TypedObject object) throws IOException {
        // TODO can be inlined
        try (var writer = new BytesBinaryWriter()) {
            new HFWTypeWriter().write(object, writer);
            return writer.toByteArray();
        }
    }

    private record Patch(
        HorizonForbiddenWest.StreamingGroupData group,
        int objectIndex,
        StreamingObjectReader.Range originalRange,
        StreamingObjectReader.Range patchedRange
    ) {
    }
}
