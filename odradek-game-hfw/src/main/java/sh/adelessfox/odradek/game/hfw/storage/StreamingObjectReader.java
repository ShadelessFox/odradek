package sh.adelessfox.odradek.game.hfw.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.game.hfw.rtti.data.StreamingLink;
import sh.adelessfox.odradek.game.hfw.rtti.data.UUIDRef;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataHolder;
import sh.adelessfox.odradek.rtti.data.Ref;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import sh.adelessfox.odradek.rtti.runtime.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.runtime.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.runtime.PointerTypeInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.*;

public class StreamingObjectReader extends HFWTypeReader {
    private static final Logger log = LoggerFactory.getLogger(StreamingObjectReader.class);

    private final ObjectStreamingSystem system;
    private final StreamingGraphResource graph;
    private final TypeFactory factory;

    private final LruWeakCache<StreamingGroupData, GroupResult> cache = new LruWeakCache<>(5000);

    private GroupResult currentGroup;
    private List<GroupResult> currentSubGroups;

    private boolean resolveStreamingLinksAndLocators;
    private int streamingLinkIndex;
    private int streamingLocatorIndex;
    private int depth;

    public record GroupResult(StreamingGroupData group, List<ObjectInfo> objects) {
        public GroupResult {
            objects = List.copyOf(objects);
        }

        @Override
        public String toString() {
            return "GroupInfo[group=" + group + ", objects=" + objects.size() + "]";
        }
    }

    public record ObjectResult(GroupResult group, ObjectInfo object) {
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
        var groups = new ArrayList<GroupResult>();
        var result = readGroup(id, groups, readSubgroups);
        assert result == groups.getLast();
        return result;
    }

    private GroupResult readGroup(int id, List<GroupResult> groups, boolean readSubgroups) throws IOException {
        var group = Objects.requireNonNull(graph.group(id), () -> "Group not found: " + id);

        if (log.isDebugEnabled()) {
            log.debug("{}Reading group {}", indent(), Colors.blue(id));
        }

        for (GroupResult result : groups) {
            if (result.group == group) {
                return result;
            }
        }

        depth++;
        var result = readGroup(group, groups, readSubgroups);
        groups.add(result);
        depth--;

        return result;
    }

    private GroupResult readGroup(StreamingGroupData group, List<GroupResult> groups, boolean readSubgroups) throws IOException {
        var subGroups = new ArrayList<GroupResult>(group.subGroupCount());
        if (readSubgroups) {
            for (int i = 0; i < group.subGroupCount(); i++) {
                subGroups.add(readGroup(graph.subGroups()[group.subGroupStart() + i], groups, true));
            }
        }

        currentSubGroups = subGroups;
        resolveStreamingLinksAndLocators = readSubgroups;

        GroupResult result = cache.get(group);
        if (result == null) {
            result = readSingleGroup(group);
            cache.put(group, result);
        }

        return result;
    }

    private GroupResult readSingleGroup(StreamingGroupData group) throws IOException {
        var objects = new ArrayList<ObjectInfo>(group.numObjects());
        for (int i = 0; i < group.numObjects(); i++) {
            var type = graph.types().get(group.typeStart() + objects.size());
            var object = (RTTIRefObject) type.newInstance();
            objects.add(new ObjectInfo(type, object));
        }

        var result = new GroupResult(group, objects);

        currentGroup = result;
        streamingLinkIndex = group.linkStart();
        streamingLocatorIndex = group.locatorStart();

        for (int i = 0, j = 0; i < group.spanCount(); i++) {
            var span = graph.spanTable().get(group.spanStart() + i);
            var data = getSpanData(span);
            var reader = BinaryReader.wrap(data);

            while (reader.remaining() > 0) {
                var object = objects.get(j++);

                if (log.isDebugEnabled()) {
                    log.debug(
                        "{}Reading {} in {} at offset {}",
                        indent(),
                        Colors.yellow(object.type()),
                        Colors.yellow(getSpanFile(span)),
                        Colors.blue(span.offset() + reader.position())
                    );
                }

                fillCompound(object, reader);
            }
        }

        return result;
    }

    private void fillCompound(ObjectInfo info, BinaryReader reader) throws IOException {
        var object = info.object();
        for (ClassAttrInfo attr : info.type().serializableAttrs()) {
            attr.set(object, read(attr.type().get(), reader, factory));
        }
        if (object instanceof ExtraBinaryDataHolder holder) {
            holder.deserialize(reader, factory);
        }
    }

    @Override
    protected Object readCompound(ClassTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
        Object object = super.readCompound(info, reader, factory);

        if (object instanceof StreamingDataSource dataSource) {
            resolveStreamingDataSource(dataSource);
        }

        return object;
    }

    @Override
    protected Ref<?> readPointer(PointerTypeInfo info, BinaryReader reader, TypeFactory factory) throws IOException {
        if (!reader.readByteBoolean()) {
            return null;
        } else if (info.name().name().equals("UUIDRef")) {
            return new UUIDRef<>((GGUUID) readCompound(factory.get(GGUUID.class), reader, factory));
        } else {
            return resolveStreamingLink(info);
        }
    }

    private void resolveStreamingDataSource(StreamingDataSource dataSource) {
        if (!resolveStreamingLinksAndLocators) {
            return;
        }

        if (dataSource.channel() != -1 && dataSource.length() > 0) {
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

    private Ref<?> resolveStreamingLink(PointerTypeInfo info) {
        if (!resolveStreamingLinksAndLocators) {
            return null;
        }

        var result = system.readLink(streamingLinkIndex);
        int linkGroup = result.group();
        int linkIndex = result.index();

        streamingLinkIndex = result.position();

        if (info.name().name().equals("StreamingRef")) {
            // Can't resolve streaming references without actually running the game
            return null;
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
        var matches = info.itemType().get().type().isInstance(object.object());

        if (log.isDebugEnabled()) {
            log.debug(
                "{}Resolving {} to object {} (index: {}) in group {} (index: {})",
                indent(),
                Colors.yellow(info.name()),
                Colors.yellow(object.type()),
                Colors.blue(linkIndex),
                Colors.blue(group.group.groupID()),
                Colors.blue(linkGroup)
            );
        }

        if (!matches) {
            throw new IllegalStateException("Type mismatch for pointer");
        }

        return new StreamingLink<>(object.object(), group.group().groupID(), linkIndex);
    }

    private String indent() {
        return "\t".repeat(depth);
    }

    private byte[] getSpanData(StreamingSourceSpan span) throws IOException {
        return system.getFileData(getSpanFile(span), span.offset(), span.length());
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
