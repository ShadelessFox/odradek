package sh.adelessfox.odradek.game.hfw.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.decima.DecimaGame;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.decima.StreamingGraph;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.game.hfw.rtti.data.ref.*;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.PointerTypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import sh.adelessfox.odradek.util.LruWeakCache;

import java.io.IOException;
import java.util.*;

import static sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.GGUUID;
import static sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingDataSource;

public class StreamingObjectReader extends HFWTypeReader {
    private static final Logger log = LoggerFactory.getLogger(StreamingObjectReader.class);

    private final DecimaGame game;
    private final StreamingGraph graph;
    private final TypeFactory factory;

    private final LruWeakCache<Integer, GroupResult> cache = new LruWeakCache<>(5000);

    private GroupResult currentGroup;
    private List<GroupResult> currentSubGroups;

    private boolean resolveStreamingLinksAndLocators;
    private Iterator<StreamingGraph.Link> streamingLinks;
    private Iterator<StreamingGraph.Locator> streamingLocators;
    private int depth;

    public record GroupResult(StreamingGraph.Group group, List<TypedObject> objects) {
        public GroupResult {
            objects = List.copyOf(objects);
        }

        @Override
        public String toString() {
            return "GroupInfo[group=" + group + ", objects=" + objects.size() + "]";
        }
    }

    public StreamingObjectReader(DecimaGame game, TypeFactory factory) {
        this.game = game;
        this.graph = game.streamingGraph();
        this.factory = factory;
    }

    public GroupResult readGroup(int id) throws IOException {
        return readGroup(id, true);
    }

    public GroupResult readGroup(int id, boolean readSubgroups) throws IOException {
        return readGroup(id, new HashMap<>(), readSubgroups);
    }

    private GroupResult readGroup(int id, Map<Integer, GroupResult> cache, boolean readSubgroups) throws IOException {
        var group = Objects.requireNonNull(graph.group(id), () -> "Group not found: " + id);

        if (log.isDebugEnabled()) {
            log.debug("{}Reading group {}", indent(), Colors.blue(id));
        }

        var result = cache.get(group.id());
        if (result == null) {
            depth++;
            result = readGroup(group, cache, readSubgroups);
            cache.put(result.group.id(), result);
            depth--;
        }

        return result;
    }

    private synchronized GroupResult readGroup(
        StreamingGraph.Group group,
        Map<Integer, GroupResult> groups,
        boolean readSubgroups
    ) throws IOException {
        var subGroups = new ArrayList<GroupResult>(group.subGroups().size());
        if (readSubgroups) {
            for (StreamingGraph.Group subGroup : group.subGroups()) {
                subGroups.add(readGroup(subGroup, groups, true));
            }
        }

        currentSubGroups = subGroups;
        resolveStreamingLinksAndLocators = readSubgroups;

        var result = cache.get(group.id());
        if (result == null) {
            result = readSingleGroup(group);
            cache.put(group.id(), result);
        }

        return result;
    }

    private GroupResult readSingleGroup(StreamingGraph.Group group) throws IOException {
        var objects = new ArrayList<TypedObject>(group.types().size());
        for (ClassTypeInfo type : group.types()) {
            objects.add(factory.newInstance(type));
        }

        var result = new GroupResult(group, objects);

        currentGroup = result;
        streamingLinks = group.links();
        streamingLocators = group.locators().iterator();

        int index = 0;
        for (StreamingGraph.Span span : group.spans()) {
            var data = getSpanData(span);
            var reader = BinaryReader.wrap(data);

            while (reader.remaining() > 0) {
                var object = objects.get(index++);

                if (log.isDebugEnabled()) {
                    log.debug(
                        "{}Reading {} in {} at offset {}",
                        indent(),
                        Colors.yellow(object.getType()),
                        Colors.yellow(getSpanFile(span)),
                        Colors.blue(span.offset() + reader.position())
                    );
                }

                fillCompound(object.getType(), reader, factory, object);
            }
        }

        return result;
    }

    @Override
    protected void fillCompound(ClassTypeInfo info, BinaryReader reader, TypeFactory factory, Object object) throws IOException {
        super.fillCompound(info, reader, factory, object);

        if (object instanceof StreamingDataSource dataSource) {
            resolveStreamingDataSource(dataSource);
        }
    }

    @Override
    protected Object readPointer(PointerTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
        if (!reader.readByteBoolean()) {
            return null;
        } else if (info.pointerType().equals("UUIDRef")) {
            return new UUIDRef<>((GGUUID) readCompound(factory.get("GGUUID").asClass(), reader, factory));
        } else {
            return resolveLink(info);
        }
    }

    private void resolveStreamingDataSource(StreamingDataSource dataSource) {
        if (!resolveStreamingLinksAndLocators) {
            return;
        }

        if (dataSource.isValid()) {
            var locator = streamingLocators.next();

            if (log.isDebugEnabled()) {
                log.debug(
                    "{}Resolving data source to {} at offset {}",
                    indent(),
                    Colors.yellow(graph.files().get(locator.fileIndex())),
                    Colors.blue(locator.offset())
                );
            }

            dataSource.locator(locator.offset() << 24 | locator.fileIndex() & 0xffffff);
        }
    }

    private Object resolveLink(PointerTypeInfo info) {
        if (!resolveStreamingLinksAndLocators) {
            return null;
        }

        var result = streamingLinks.next();
        var linkGroup = result.group();
        int linkIndex = result.index();

        var pointerType = info.pointerType();
        if (pointerType.equals("StreamingRef")) {
            if (linkGroup.isPresent()) {
                // If linkGroup != -1, then it's the id of the group; it's an equivalent of doing graph.group(linkGroup)
                return new StreamingRef<>(new ObjectId(linkGroup.orElseThrow(), linkIndex));
            } else {
                // No idea how to resolve it otherwise. Presumably points to a runtime singleton?
                return null;
            }
        }

        GroupResult group;
        if (linkGroup.isPresent()) {
            // Seems to reference subgroups
            group = currentSubGroups.get(linkGroup.orElseThrow());
        } else {
            // References the current group being read
            group = currentGroup;
        }

        var object = group.objects().get(linkIndex);
        var matches = info.itemType().asClass().isAssignableFrom(object.getType());

        if (log.isDebugEnabled()) {
            log.debug(
                "{}Resolving {} to object {} (index: {}) in group {} (index: {})",
                indent(),
                Colors.yellow(info.name()),
                Colors.yellow(object.getType()),
                Colors.blue(linkIndex),
                Colors.blue(group.group.id()),
                Colors.blue(linkGroup)
            );
        }

        if (!matches) {
            throw new IllegalStateException("Type mismatch for pointer");
        }

        var objectId = new ObjectId(group.group().id(), linkIndex);
        return switch (pointerType) {
            case "Ref" -> new Ref<>(objectId, object);
            case "WeakPtr" -> new WeakPtr<>(objectId, object);
            case "cptr" -> new CPtr<>(objectId, object);
            default -> throw new UnsupportedOperationException("Unsupported pointer type: " + pointerType);
        };
    }

    private String indent() {
        return "\t".repeat(depth);
    }

    private byte[] getSpanData(StreamingGraph.Span span) throws IOException {
        return game.readFile(getSpanFile(span), span.offset(), span.length());
    }

    private String getSpanFile(StreamingGraph.Span span) {
        return graph.files().get(span.fileIndex());
    }

    private record Colors(CharSequence text, int foreground) {
        Colors(Object value, int foreground) {
            this(value.toString(), foreground);
        }

        static Colors yellow(Object value) {
            return new Colors(value, 33);
        }

        static Colors blue(Object value) {
            return new Colors(value, 34);
        }

        @Override
        public String toString() {
            return "\033[%dm%s\033[0m".formatted(foreground, text);
        }
    }
}
