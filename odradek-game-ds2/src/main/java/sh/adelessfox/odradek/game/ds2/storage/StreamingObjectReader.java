package sh.adelessfox.odradek.game.ds2.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.game.ds2.rtti.DS2TypeReader;
import sh.adelessfox.odradek.game.ds2.rtti.data.ref.*;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.PointerTypeInfo;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;
import java.util.*;

import static sh.adelessfox.odradek.game.ds2.rtti.DS2.*;

public final class StreamingObjectReader extends DS2TypeReader {
    private static final Logger log = LoggerFactory.getLogger(StreamingObjectReader.class);

    private final ObjectStreamingSystem system;
    private final StreamingGraphResource graph;
    private final TypeFactory factory;

    private final sh.adelessfox.odradek.util.LruWeakCache<Integer, GroupResult> cache = new sh.adelessfox.odradek.util.LruWeakCache<>(5000);

    private GroupResult currentGroup;
    private List<GroupResult> currentSubGroups;

    private boolean resolveStreamingLinksAndLocators;
    private int streamingLinkIndex;
    private int streamingLocatorIndex;
    private int depth;

    public record GroupResult(StreamingGroupData group, List<RTTIRefObject> objects) {
        public GroupResult {
            objects = List.copyOf(objects);
        }

        @Override
        public String toString() {
            return "GroupInfo[group=" + group + ", objects=" + objects.size() + "]";
        }
    }

    public StreamingObjectReader(ObjectStreamingSystem system, TypeFactory factory) {
        this.system = system;
        this.graph = system.graph();
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

        var result = cache.get(group.groupID());
        if (result == null) {
            depth++;
            result = readGroup(group, cache, readSubgroups);
            cache.put(result.group.groupID(), result);
            depth--;
        }

        return result;
    }

    private synchronized GroupResult readGroup(
        StreamingGroupData group,
        Map<Integer, GroupResult> groups,
        boolean readSubgroups
    ) throws IOException {
        var subGroups = new ArrayList<GroupResult>(group.subGroupCount());
        if (readSubgroups) {
            for (int i = 0; i < group.subGroupCount(); i++) {
                subGroups.add(readGroup(graph.subGroups()[group.subGroupStart() + i], groups, true));
            }
        }

        currentSubGroups = subGroups;
        resolveStreamingLinksAndLocators = readSubgroups;

        var result = cache.get(group.groupID());
        if (result == null) {
            result = readSingleGroup(group);
            cache.put(group.groupID(), result);
        }

        return result;
    }

    private GroupResult readSingleGroup(StreamingGroupData group) throws IOException {
        var objects = new ArrayList<RTTIRefObject>(group.numObjects());
        for (int i = 0; i < group.numObjects(); i++) {
            var type = graph.types().get(group.typeStart() + objects.size());
            var object = (RTTIRefObject) factory.newInstance(type);
            objects.add(object);
        }

        var result = new GroupResult(group, objects);

        currentGroup = result;
        streamingLinkIndex = group.linkStart();
        streamingLocatorIndex = group.locatorStart();

        var data = new byte[Math.toIntExact(group.groupSize())];
        for (int i = 0, o = 0; i < group.spanCount(); i++) {
            var span = graph.spanTable().get(group.spanStart() + i);
            readSpanData(span, data, o);
            o += span.length();
        }

        var reader = BinaryReader.wrap(data);
        for (int i = 0; i < group.numObjects(); i++) {
            var object = objects.get(i);
            fillCompound(object.getType(), reader, factory, object);
        }

        if (reader.remaining() > 0) {
            throw new IOException("Not all data was read for group " + group.groupID()
                + ": " + reader.remaining() + " bytes remaining");
        }

        if (resolveStreamingLinksAndLocators) {
            if (streamingLinkIndex != group.linkStart() + group.linkSize()) {
                throw new IOException("Not all links were read for group " + group.groupID() + ": "
                    + (group.linkStart() + group.linkSize() - streamingLinkIndex) + " links remaining");
            }

            if (streamingLocatorIndex != group.locatorStart() + group.locatorCount()) {
                throw new IOException("Not all locators were read for group " + group.groupID() + ": "
                    + (group.locatorStart() + group.locatorCount() - streamingLocatorIndex) + " locators remaining");
            }
        }

        return result;
    }

    @Override
    protected void fillCompound(
        ClassTypeInfo info,
        BinaryReader reader,
        TypeFactory factory,
        Object object
    ) throws IOException {
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
            var locator = graph.locatorTable().get(streamingLocatorIndex++).data();

            if (log.isDebugEnabled()) {
                log.debug(
                    "{}Resolving data source to {} at offset {}",
                    indent(),
                    Colors.yellow(graph.files().get(Math.toIntExact(locator & 0xffffff))),
                    Colors.blue(locator >>> 24)
                );
            }

            dataSource.locator(locator);
        }
    }

    private Object resolveLink(PointerTypeInfo info) {
        if (!resolveStreamingLinksAndLocators) {
            return null;
        }

        var result = system.readLink(streamingLinkIndex);
        int linkGroup = result.group();
        int linkIndex = result.index();

        streamingLinkIndex = result.position();

        var pointerType = info.pointerType();
        if (pointerType.equals("StreamingRef")) {
            if (linkGroup != -1) {
                // If linkGroup != -1, then it's the id of the group; it's an equivalent of doing graph.group(linkGroup)
                return new StreamingRef<>(new ObjectId(linkGroup, linkIndex));
            } else {
                // No idea how to resolve it otherwise. Has something to do with StreamingGraphResource.UUIDLinkTable
                return null;
            }
        }

        GroupResult group;
        if (linkGroup == -1) {
            // References the current group being read
            group = currentGroup;
        } else {
            // Seems to reference subgroups
            group = currentSubGroups.get(linkGroup);
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
                Colors.blue(group.group.groupID()),
                Colors.blue(linkGroup)
            );
        }

        if (!matches) {
            log.error(
                "Type mismatch for {}: resolved to {} ({}:{})",
                info,
                object.getType(),
                group.group.groupID(),
                linkIndex);
            return null;
        }

        var objectId = new ObjectId(group.group().groupID(), linkIndex);
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

    private void readSpanData(StreamingSourceSpan span, byte[] dst, int dstOff) throws IOException {
        system.readFileData(getSpanFile(span), span.offset(), dst, dstOff, span.length());
    }

    private String getSpanFile(StreamingSourceSpan span) {
        return graph.files().get(span.fileIndexAndIsPatch() & 0x7fffffff);
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
