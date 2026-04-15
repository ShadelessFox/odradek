package sh.adelessfox.odradek.game.hfw.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.StreamingGraph;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeId;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingGraphResource;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingGroupData;
import sh.adelessfox.odradek.hashing.HashCode;
import sh.adelessfox.odradek.hashing.HashFunction;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class StreamingGraphImpl implements StreamingGraph {
    private static final Logger log = LoggerFactory.getLogger(StreamingGraphImpl.class);

    private final StreamingGraphResource resource;

    private final List<ClassTypeInfo> types;
    private final List<GroupImpl> groups;
    private final List<Group> subGroups;
    private final List<String> files;
    private final byte[] linkTable;

    private final Map<Integer, GroupImpl> groupById;
    private final List<Span> spans;
    private final List<Locator> locators;

    public StreamingGraphImpl(
        StreamingGraphResource graph,
        StreamingGraphStorage storage,
        TypeFactory typeFactory
    ) throws IOException {
        this.resource = graph;

        types = readTypeTable(graph, typeFactory);

        groups = graph.groups().stream()
            .map(group -> new GroupImpl(this, group))
            .toList();

        groupById = groups.stream().collect(Collectors.toMap(
            GroupImpl::id,
            Function.identity()));

        subGroups = IntStream.of(graph.subGroups())
            .mapToObj(this::group)
            .toList();

        spans = graph.spanTable().stream()
            .map(span -> new Span(span.fileIndexAndIsPatch() & 0x7fffffff, span.offset(), span.length()))
            .toList();

        locators = graph.locatorTable().stream()
            .map(locator -> new Locator((int) (locator.data() & 0xffffff), locator.data() >>> 24))
            .toList();

        files = Collections.unmodifiableList(graph.files());

        var linkTableFile = graph.files().get(Math.toIntExact(graph.linkTableID()));
        linkTable = storage.read(linkTableFile, 0, graph.linkTableSize());
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
    public List<Span> spans() {
        return spans;
    }

    @Override
    public List<Locator> locators() {
        return locators;
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
    public Iterator<StreamingGraph.Link> links(int position) {
        var buffer = ByteBuffer.wrap(linkTable, position, linkTable.length - position);
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return buffer.hasRemaining();
            }

            @Override
            public StreamingGraph.Link next() {
                return readLink(buffer);
            }
        };
    }

    @Override
    public HashCode checksum() {
        return HashFunction.murmur3().hash(linkTable);
    }

    @Override
    public TypedObject resource() {
        return resource;
    }

    private static List<ClassTypeInfo> readTypeTable(
        StreamingGraphResource graph,
        TypeFactory factory
    ) throws IOException {
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

    private static StreamingGraph.Link readLink(ByteBuffer buffer) {
        OptionalInt linkGroup;
        int linkIndex;

        int first = buffer.get();
        if ((first & 0x40) != 0) {
            linkGroup = OptionalInt.of(readVarInt(buffer, first & 0xbf));
            linkIndex = readVarInt(buffer, buffer.get());
        } else {
            linkGroup = OptionalInt.empty();
            linkIndex = readVarInt(buffer, first & 0xbf);
        }

        return new StreamingGraph.Link(linkGroup, linkIndex);
    }

    private static int readVarInt(ByteBuffer buffer, int initial) {
        int temp = initial;
        int value = initial & 0x7f;
        while ((temp & 0x80) != 0) {
            temp = buffer.get();
            value = (value << 7) | (temp & 0x7f);
        }
        return value;
    }

    private static final class GroupImpl implements Group {
        private final StreamingGraphImpl graph;
        private final StreamingGroupData inner;
        private final List<Integer> roots;

        private GroupImpl(StreamingGraphImpl graph, StreamingGroupData inner) {
            this.graph = graph;
            this.inner = inner;

            roots = Arrays.stream(graph.resource.rootIndices(), inner.rootStart(), inner.rootStart() + inner.rootCount())
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

        @Override
        public Range<Span> spans() {
            return new Range<>(inner.spanStart(), inner.spanCount(), graph.spans());
        }

        @Override
        public Range<Locator> locators() {
            return new Range<>(inner.locatorStart(), inner.locatorCount(), graph.locators());
        }

        @Override
        public Iterator<Link> links() {
            return graph.links(inner.linkStart());
        }
    }
}
