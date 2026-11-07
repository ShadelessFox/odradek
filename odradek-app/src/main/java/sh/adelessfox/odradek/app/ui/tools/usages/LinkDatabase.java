package sh.adelessfox.odradek.app.ui.tools.usages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.decima.DecimaGame;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.game.decima.ObjectIdHolder;
import sh.adelessfox.odradek.game.decima.StreamingGraph;
import sh.adelessfox.odradek.io.BinaryWriter;
import sh.adelessfox.odradek.io.BytesBinaryWriter;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.rtti.util.PathTypeVisitor;
import sh.adelessfox.odradek.rtti.util.TypePath;
import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.collect.Ints;
import wtf.reversed.toolbox.hash.HashCode;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

public final class LinkDatabase implements Closeable {
    public record Link(int groupId, int objectIndex, TypePath path) {
    }

    private static final Logger log = LoggerFactory.getLogger(LinkDatabase.class);

    private static final int FILE_MAGIC = 'G' | 'R' << 8 | 'P' << 16 | 'H' << 24;
    private static final int FILE_VERSION = 1;

    private final DecimaGame game;
    private final BinarySource source;
    private final Ints offsets;

    private LinkDatabase(DecimaGame game, BinarySource source, Ints offsets) {
        this.game = game;
        this.source = source;
        this.offsets = offsets;
    }

    public static LinkDatabase open(DecimaGame game, Path path) throws IOException {
        var source = BinarySource.open(path);
        try {
            int magic = source.readInt();
            if (magic != FILE_MAGIC) {
                throw new IllegalArgumentException("Invalid database file magic");
            }

            int version = source.readInt();
            if (version != FILE_VERSION) {
                throw new IllegalArgumentException("Unsupported database file version");
            }

            long checksum = source.readLong();
            if (checksum != computeHash(game).asLong()) {
                throw new IllegalArgumentException("Link table checksum mismatch");
            }

            int count = game.streamingGraph().types().size();
            var offsets = source.readInts(count);
            return new LinkDatabase(game, source, offsets);
        } catch (Exception e) {
            source.close();
            throw e;
        }
    }

    public static void build(
        DecimaGame game,
        Path path,
        BiConsumer<Integer, Integer> progress
    ) throws IOException {
        var graph = game.streamingGraph();

        int objects = graph.types().size();
        var links = IntStream.range(0, objects)
            .mapToObj(_ -> new ArrayList<PackedLink>())
            .toList();

        log.debug("Scanning graph groups...");
        for (int i = 0; i < graph.groups().size(); i++) {
            var group = graph.groups().get(i);
            progress.accept(i + 1, graph.groups().size());

            var info = visitGroup(group, game);
            for (int j = 0; j < info.objects().size(); j++) {
                var object = info.objects().get(j);
                for (Link link : object.out()) {
                    var targetGroup = graph.group(link.groupId());
                    int targetObject = targetGroup.typeStart() + link.objectIndex();
                    links.get(targetObject).add(PackedLink.pack(group.id(), j, link.path()));
                }
            }
        }

        log.debug("Serializing the database...");
        try (BinaryWriter writer = BinaryWriter.open(path)) {
            var offsets = Ints.allocate(objects);

            writer.position(16 + (long) objects * Integer.BYTES);
            for (int i = 0; i < objects; i++) {
                offsets.set(i, Math.toIntExact(writer.position()));

                writeVarInt(writer, links.get(i).size());
                for (PackedLink link : links.get(i)) {
                    writeLink(link, writer);
                }
            }

            writer.position(0);
            writer.writeInt(FILE_MAGIC);
            writer.writeInt(FILE_VERSION);
            writer.writeLong(computeHash(game).asLong());
            writer.writeInts(offsets);
        }
    }

    public static HashCode computeHash(DecimaGame game) {
        return game.streamingGraph().checksum();
    }

    public List<Link> getIncomingLinks(ObjectId target) throws IOException {
        var graph = game.streamingGraph();
        var group = graph.group(target.groupId());
        int index = group.typeStart() + target.objectIndex();

        synchronized (source) {
            return source
                .position(offsets.get(index))
                .readObjects(readVarInt(source), r -> readLink(graph, r));
        }
    }

    public List<Link> getOutgoingLinks(ObjectId source) throws IOException {
        var object = game.readObject(source);
        var info = visitObject(object);
        return info.out();
    }

    public void close() throws IOException {
        source.close();
    }

    private static GroupInfo visitGroup(StreamingGraph.Group group, DecimaGame game) throws IOException {
        var result = game.readGroup(group.id());
        var objects = new ArrayList<ObjectInfo>(group.types().size());

        for (int i = 0; i < group.types().size(); i++) {
            objects.add(visitObject(result.get(i)));
        }

        return new GroupInfo(group, objects);
    }

    private static ObjectInfo visitObject(TypedObject object) {
        var links = new ArrayList<Link>();
        var visitor = new PathTypeVisitor<RuntimeException>() {
            @Override
            public void visitContainer(ContainerTypeInfo typeInfo, Object object1, TypePath.Builder builder) {
                if (!(typeInfo.itemType() instanceof AtomTypeInfo)) {
                    super.visitContainer(typeInfo, object1, builder);
                }
            }

            @Override
            public void visitPointer(PointerTypeInfo typeInfo, Object object1, TypePath.Builder builder) {
                if (object1 instanceof ObjectIdHolder holder) {
                    var objectId = holder.objectId();
                    links.add(new Link(objectId.groupId(), objectId.objectIndex(), builder.build()));
                }
            }
        };
        visitor.visit(object);
        return new ObjectInfo(links);
    }

    private static Link readLink(StreamingGraph graph, BinarySource source) throws IOException {
        int groupId = readVarInt(source);
        int objectIndex = readVarInt(source);
        var objectType = graph.group(groupId).types().get(objectIndex);
        var path = readPath(objectType, source);
        return new Link(groupId, objectIndex, path);
    }

    private static void writeLink(PackedLink link, BinaryWriter writer) throws IOException {
        writeVarInt(writer, link.groupId());
        writeVarInt(writer, link.objectIndex());
        writeVarInt(writer, link.path().length());
        writer.writeBytes(link.path());
    }

    private static TypePath readPath(ClassTypeInfo root, BinarySource source) throws IOException {
        var elements = new ArrayList<TypePath.Element>();
        var parent = (TypeInfo) root;

        for (long end = readVarInt(source) + source.position(); source.position() < end; ) {
            int element = readVarInt(source);
            int index = element >>> 1;
            if ((element & 1) == 0) {
                var type = parent.asClass();
                var attr = type.orderedAttrs().get(index);
                elements.add(new TypePath.Element.Attr(type, attr));
                parent = attr.type();
            } else {
                var type = parent.asContainer();
                elements.add(new TypePath.Element.Index(type, index));
                parent = type.itemType();
            }
        }

        return new TypePath(elements);
    }

    private static void writePath(TypePath path, BinaryWriter writer) throws IOException {
        for (TypePath.Element element : path.elements()) {
            switch (element) {
                // @formatter:off
                case TypePath.Element.Attr(var type, var attr) ->
                    writeVarInt(writer, type.orderedAttrs().indexOf(attr) << 1);
                case TypePath.Element.Index(_, int index) ->
                    writeVarInt(writer, index << 1 | 1);
                // @formatter:on
            }
        }
    }

    private static int readVarInt(BinarySource source) throws IOException {
        int len = 0;
        int out = 0;
        while (true) {
            int tmp = source.readByte();
            out |= (tmp & 0x7F) << len;
            if ((tmp & 0x80) == 0) {
                break;
            }
            len += 7;
            if (len >= 32) {
                throw new IOException("VarInt too long");
            }
        }
        return out;
    }

    private static void writeVarInt(BinaryWriter writer, int value) throws IOException {
        while ((value & ~0x7F) != 0) {
            writer.writeByte((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        writer.writeByte((byte) value);
    }

    private record ObjectInfo(List<Link> out) {
        private ObjectInfo {
            out = List.copyOf(out);
        }

        @Override
        public String toString() {
            return "ObjectInfo{out=" + out.size() + '}';
        }
    }

    private record GroupInfo(StreamingGraph.Group group, List<ObjectInfo> objects) {
        private GroupInfo {
            objects = List.copyOf(objects);
        }

        @Override
        public String toString() {
            return "GroupInfo{objects=" + objects.size() + '}';
        }
    }

    private record PackedLink(int groupId, int objectIndex, Bytes path) {
        static PackedLink pack(int groupId, int objectIndex, TypePath path) throws IOException {
            Bytes bytes;
            try (BytesBinaryWriter writer = new BytesBinaryWriter()) {
                writePath(path, writer);
                bytes = writer.toBytes();
            }
            return new PackedLink(groupId, objectIndex, bytes);
        }
    }
}
