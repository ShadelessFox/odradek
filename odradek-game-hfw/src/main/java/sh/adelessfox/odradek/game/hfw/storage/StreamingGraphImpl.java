package sh.adelessfox.odradek.game.hfw.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.decima.StreamingGraph;
import sh.adelessfox.odradek.game.hfw.rtti.HFW.StreamingGraphResource;
import sh.adelessfox.odradek.game.hfw.rtti.HFW.StreamingGroupData;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeId;
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

    private final List<? extends Group> groups;
    private final Map<Integer, Group> groupIds;
    private final List<? extends Group> subGroups;
    private final Map<Group, List<Group>> superGroups;

    private final List<ClassTypeInfo> typeTable;
    private final byte[] linkTable;

    private final List<Span> spans;
    private final List<Locator> locators;
    private final List<String> files;

    public StreamingGraphImpl(
        StreamingGraphResource graph,
        StreamingGraphStorage storage,
        TypeFactory typeFactory
    ) throws IOException {
        this.resource = graph;

        groups = computeGroups(this);
        groupIds = computeGroupIdToGroup(groups);
        subGroups = computeSubgroups(graph);
        superGroups = computeGroupToSuperGroups(groups);

        typeTable = readTypeTable(graph, typeFactory);
        linkTable = readLinkTable(graph, storage);

        spans = computeSpans(graph);
        locators = computeLocators(graph);
        files = computeFiles(graph);
    }

    @Override
    public List<ClassTypeInfo> types() {
        return typeTable;
    }

    @Override
    public List<? extends Group> groups() {
        return groups;
    }

    @Override
    public List<String> files() {
        return files;
    }

    @Override
    public Group group(int id) {
        return Objects.requireNonNull(groupIds.get(id), () -> "Group not found: " + id);
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

    private static List<String> computeFiles(StreamingGraphResource graph) {
        return Collections.unmodifiableList(graph.files());
    }

    private static List<Locator> computeLocators(StreamingGraphResource graph) {
        return graph.locatorTable().stream()
            .map(locator -> new Locator((int) (locator.data() & 0xffffff), locator.data() >>> 24))
            .toList();
    }

    private static List<Span> computeSpans(StreamingGraphResource graph) {
        return graph.spanTable().stream()
            .map(span -> new Span(span.fileIndexAndIsPatch() & 0x7fffffff, span.offset(), span.length()))
            .toList();
    }

    private static List<? extends Group> computeGroups(StreamingGraphImpl graph) {
        return graph.resource.groups().stream()
            .map(group -> new GroupImpl(graph, group))
            .toList();
    }

    private static Map<Integer, Group> computeGroupIdToGroup(List<? extends Group> groups) {
        return groups.stream().collect(Collectors.toMap(Group::id, Function.identity()));
    }

    private static Map<Group, List<Group>> computeGroupToSuperGroups(List<? extends Group> groups) {
        var result = new HashMap<Group, List<Group>>();
        for (Group group : groups) {
            for (Group subGroup : group.subGroups()) {
                result.computeIfAbsent(subGroup, _ -> new ArrayList<>()).add(group);
            }
        }
        result.replaceAll((_, parents) -> List.copyOf(parents));
        return Map.copyOf(result);
    }

    private List<Group> computeSubgroups(StreamingGraphResource graph) {
        return IntStream.of(graph.subGroups())
            .mapToObj(this::group)
            .toList();
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

    private static byte[] readLinkTable(
        StreamingGraphResource graph,
        StreamingGraphStorage storage
    ) throws IOException {
        var file = graph.files().get(Math.toIntExact(graph.linkTableID()));
        return storage.read(file, 0, graph.linkTableSize());
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
        public int typeStart() {
            return inner.typeStart();
        }

        @Override
        public List<? extends Group> subGroups() {
            return graph.subGroups.subList(inner.subGroupStart(), inner.subGroupStart() + inner.subGroupCount());
        }

        @Override
        public List<? extends Group> superGroups() {
            return graph.superGroups.getOrDefault(this, List.of());
        }

        @Override
        public List<Integer> roots() {
            return roots;
        }

        @Override
        public List<ClassTypeInfo> types() {
            return graph.typeTable.subList(inner.typeStart(), inner.typeStart() + inner.typeCount());
        }

        @Override
        public List<Span> spans() {
            return graph.spans.subList(inner.spanStart(), inner.spanStart() + inner.spanCount());
        }

        @Override
        public List<Locator> locators() {
            return graph.locators.subList(inner.locatorStart(), inner.locatorStart() + inner.locatorCount());
        }

        @Override
        public Iterator<Link> links() {
            return graph.links(inner.linkStart());
        }
    }
}
