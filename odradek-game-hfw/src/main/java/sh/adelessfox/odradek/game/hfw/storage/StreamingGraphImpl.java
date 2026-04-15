package sh.adelessfox.odradek.game.hfw.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.decima.LinkTable;
import sh.adelessfox.odradek.game.decima.StreamingGraph;
import sh.adelessfox.odradek.game.hfw.game.ForbiddenWestGame;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeId;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingGraphResource;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingGroupData;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class StreamingGraphImpl implements StreamingGraph {
    private static final Logger log = LoggerFactory.getLogger(StreamingGraphImpl.class);

    private final StreamingGraphResource inner;

    private final List<ClassTypeInfo> types;
    private final List<GroupImpl> groups;
    private final List<GroupImpl> subGroups;
    private final List<String> files;
    private final LinkTable linkTable;

    private final Map<Integer, GroupImpl> groupById;

    public StreamingGraphImpl(
        StreamingGraphResource inner,
        TypeFactory typeFactory,
        ForbiddenWestGame game
    ) throws IOException {
        this.inner = inner;

        types = readTypeTable(inner, typeFactory);

        groups = inner.groups().stream()
            .map(group -> new GroupImpl(this, group))
            .toList();

        subGroups = IntStream.of(inner.subGroups())
            .mapToObj(groups::get)
            .toList();

        files = Collections.unmodifiableList(inner.files());

        var linkTableFile = inner.files().get(Math.toIntExact(inner.linkTableID()));
        var linkTableData = game.readFile(linkTableFile, 0, inner.linkTableSize());
        linkTable = new LinkTableImpl(linkTableData);

        groupById = groups.stream().collect(Collectors.toMap(
            GroupImpl::id,
            Function.identity()));
    }

    @Override
    public List<ClassTypeInfo> types() {
        return types;
    }

    @Override
    public List<? extends Group> groups() {
        return groups;
    }

    @Override
    public List<? extends Group> subGroups() {
        return subGroups;
    }

    @Override
    public List<String> files() {
        return files;
    }

    @Override
    public Group group(int id) {
        return Objects.requireNonNull(groupById.get(id), () -> "Group not found: " + id);
    }

    @Override
    public LinkTable linkTable() {
        return linkTable;
    }

    @Override
    public TypedObject resource() {
        return inner;
    }

    private static List<ClassTypeInfo> readTypeTable(HorizonForbiddenWest.StreamingGraphResource graph, TypeFactory factory) throws IOException {
        log.debug("Reading type table");

        var reader = BinaryReader.wrap(graph.typeTableData());

        var compression = reader.readInt();
        var stride = reader.readInt();
        var count = reader.readInt();
        var count2 = reader.readInt();
        var unk10 = reader.readInt();

        if (compression != 0) {
            throw new IOException("Unsupported compression: " + compression);
        }
        if (stride != 2) {
            throw new IOException("Unsupported stride: " + stride);
        }
        if (count != count2) {
            throw new IOException("Count mismatch");
        }
        if (unk10 != 1) {
            throw new IOException("Unexpected unknown value: " + unk10);
        }

        var types = new ArrayList<ClassTypeInfo>(count);

        for (int i = 0; i < count; i++) {
            var index = Short.toUnsignedInt(reader.readShort());
            var hash = graph.typeHashes()[index];
            var type = factory.get(HFWTypeId.of(hash));
            types.add(type.asClass());
        }

        return List.copyOf(types);
    }

    private static final class GroupImpl implements Group {
        private final StreamingGraphImpl graph;
        private final StreamingGroupData inner;
        private final List<Integer> roots;

        private GroupImpl(StreamingGraphImpl graph, StreamingGroupData inner) {
            this.graph = graph;
            this.inner = inner;

            roots = Arrays.stream(graph.inner.rootIndices(), inner.rootStart(), inner.rootStart() + inner.rootCount())
                .boxed()
                .toList();
        }

        @Override
        public int id() {
            return inner.groupID();
        }

        @Override
        public Range<? extends Group> subGroups() {
            return new Range<>(inner.subGroupStart(), inner.subGroupCount(), graph.subGroups());
        }

        @Override
        public List<Integer> roots() {
            return roots;
        }

        @Override
        public Range<ClassTypeInfo> types() {
            return new Range<>(inner.typeStart(), inner.typeCount(), graph.types());
        }
    }
}
