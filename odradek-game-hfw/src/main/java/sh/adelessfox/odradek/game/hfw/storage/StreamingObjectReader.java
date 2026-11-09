package sh.adelessfox.odradek.game.hfw.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.decima.StreamingGraph;
import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.game.hfw.rtti.data.ref.*;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.PointerTypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import sh.adelessfox.odradek.util.LruWeakCache;
import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.io.BinarySource;
import wtf.reversed.toolbox.io.BoolFormat;

import java.io.IOException;
import java.util.*;

public final class StreamingObjectReader extends HFWTypeReader {
    private static final Logger log = LoggerFactory.getLogger(StreamingObjectReader.class);

    private final StreamingGraphStorage storage;
    private final StreamingGraph graph;
    private final TypeFactory factory;

    private final LruWeakCache<CacheKey, GroupResult> cache = new LruWeakCache<>(5000);

    private GroupResult currentGroup;
    private List<GroupResult> currentSubGroups;

    private boolean resolveStreamingLinks;
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

    public StreamingObjectReader(StreamingGraphStorage storage, StreamingGraph graph, TypeFactory factory) {
        this.storage = storage;
        this.graph = graph;
        this.factory = factory;
    }

    public synchronized GroupResult readGroup(int id, boolean readSubgroups) throws IOException {
        var group = Objects.requireNonNull(graph.group(id), () -> "Group not found: " + id);
        return readGroup(group, new HashMap<>(), new HashSet<>(), readSubgroups);
    }

    private GroupResult readGroup(
        StreamingGraph.Group group,
        Map<CacheKey, GroupResult> results,
        Set<CacheKey> loading,
        boolean readSubgroups
    ) throws IOException {
        var key = new CacheKey(group.id(), readSubgroups);
        var result = results.get(key);
        if (result != null) {
            return result;
        }

        result = cache.get(key);
        if (result != null) {
            results.put(key, result);
            return result;
        }

        if (!loading.add(key)) {
            throw new IOException("Cyclic subgroup dependency for group " + group.id());
        }

        if (log.isDebugEnabled()) {
            log.debug("{}Reading group {}", indent(), Colors.blue(group.id()));
        }

        depth++;
        try {
            var subGroups = new ArrayList<GroupResult>(group.subGroups().size());
            if (readSubgroups) {
                for (StreamingGraph.Group subGroup : group.subGroups()) {
                    subGroups.add(readGroup(subGroup, results, loading, true));
                }
            }

            currentSubGroups = subGroups;
            resolveStreamingLinks = readSubgroups;

            result = readSingleGroup(group);
            results.put(key, result);
            cache.put(key, result);
            return result;
        } finally {
            depth--;
            loading.remove(key);
        }
    }

    private GroupResult readSingleGroup(StreamingGraph.Group group) throws IOException {
        var objects = group.types().stream()
            .map(ClassTypeInfo::newInstance)
            .toList();
        var result = new GroupResult(group, objects);

        currentGroup = result;
        streamingLinks = group.links();
        streamingLocators = group.locators().iterator();

        int index = 0;
        for (StreamingGraph.Span span : group.spans()) {
            var source = BinarySource.wrap(readSpan(span));
            while (source.remaining() > 0) {
                var object = objects.get(index++);

                if (log.isDebugEnabled()) {
                    log.debug(
                        "{}Reading {} in {} at offset {}",
                        indent(),
                        Colors.yellow(object.getType()),
                        Colors.yellow(getSpanFile(span)),
                        Colors.blue(span.offset() + source.position())
                    );
                }

                fillCompound(object.getType(), source, factory, object);
            }
        }

        return result;
    }

    @Override
    protected void fillCompound(ClassTypeInfo info, BinarySource source, TypeFactory factory, Object target) throws IOException {
        super.fillCompound(info, source, factory, target);

        if (target instanceof HFW.StreamingDataSource dataSource) {
            resolveStreamingDataSource(dataSource);
        }
    }

    @Override
    protected Object readPointer(PointerTypeInfo info, BinarySource source, TypeFactory factory) throws IOException {
        if (!source.readBool(BoolFormat.BYTE)) {
            return null;
        } else if (info.pointerType().equals("UUIDRef")) {
            return new UUIDRef<>((HFW.GGUUID) readCompound(factory.get("GGUUID").asClass(), source, factory));
        } else {
            return resolveLink(info);
        }
    }

    private void resolveStreamingDataSource(HFW.StreamingDataSource dataSource) {
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
        if (!resolveStreamingLinks) {
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
            log.error(
                "Type mismatch for {}: resolved to {} ({}:{})",
                info,
                object.getType(),
                group.group.id(),
                linkIndex);
            return null;
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

    private Bytes readSpan(StreamingGraph.Span span) throws IOException {
        return storage.read(getSpanFile(span), span.offset(), span.length());
    }

    private String getSpanFile(StreamingGraph.Span span) {
        return graph.files().get(span.fileIndex());
    }

    private record CacheKey(int groupId, boolean readSubgroups) {
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
