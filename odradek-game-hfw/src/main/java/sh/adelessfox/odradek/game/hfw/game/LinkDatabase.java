package sh.adelessfox.odradek.game.hfw.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.game.LinkProvider;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.game.ObjectIdHolder;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingGroupData;
import sh.adelessfox.odradek.game.hfw.storage.StreamingGraphResource;
import sh.adelessfox.odradek.game.hfw.storage.StreamingObjectReader;
import sh.adelessfox.odradek.hashing.HashCode;
import sh.adelessfox.odradek.hashing.HashFunction;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.io.BinaryWriter;
import sh.adelessfox.odradek.io.ByteArrayBinaryWriter;
import sh.adelessfox.odradek.rtti.*;
import sh.adelessfox.odradek.rtti.data.TypePath;
import sh.adelessfox.odradek.rtti.data.TypeVisitor;
import sh.adelessfox.odradek.rtti.data.TypedObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

public final class LinkDatabase implements LinkProvider {
    private static final Logger log = LoggerFactory.getLogger(LinkDatabase.class);

    private static final int FILE_MAGIC = 'G' | 'R' << 8 | 'P' << 16 | 'H' << 24;
    private static final int FILE_VERSION = 1;

    private final ForbiddenWestGame game;
    private final BinaryReader reader;
    private final int[] offsets;

    private LinkDatabase(ForbiddenWestGame game, BinaryReader reader, int[] offsets) {
        this.game = game;
        this.reader = reader;
        this.offsets = offsets;
    }

    public static LinkDatabase open(ForbiddenWestGame game, Path path) throws IOException {
        var reader = BinaryReader.open(path);
        try {
            int magic = reader.readInt();
            if (magic != FILE_MAGIC) {
                throw new IllegalArgumentException("Invalid database file magic");
            }

            int version = reader.readInt();
            if (version != FILE_VERSION) {
                throw new IllegalArgumentException("Unsupported database file version");
            }

            long checksum = reader.readLong();
            if (checksum != computeHash(game).asLong()) {
                throw new IllegalArgumentException("Link table checksum mismatch");
            }

            int count = game.getStreamingGraph().types().size();
            var offsets = reader.readInts(count);
            return new LinkDatabase(game, reader, offsets);
        } catch (Exception e) {
            reader.close();
            throw e;
        }
    }

    public static void build(
        ForbiddenWestGame game,
        Path path,
        BiConsumer<Integer, Integer> progress
    ) throws IOException {
        var graph = game.getStreamingGraph();
        var reader = game.getStreamingReader();

        int objects = graph.types().size();
        var links = IntStream.range(0, objects)
            .mapToObj(_ -> new ArrayList<PackedLink>())
            .toList();

        for (int i = 0; i < graph.groups().size(); i++) {
            var group = graph.groups().get(i);
            progress.accept(i + 1, graph.groups().size());

            var info = visitGroup(group, reader);
            for (int j = 0; j < info.objects().size(); j++) {
                var object = info.objects().get(j);
                for (LinkProvider.Link link : object.out()) {
                    var targetGroup = graph.group(link.groupId());
                    int targetObject = targetGroup.typeStart() + link.objectIndex();
                    links.get(targetObject).add(PackedLink.pack(group.groupID(), j, link.path()));
                }
            }
        }

        log.debug("Serializing...");
        try (BinaryWriter writer = BinaryWriter.open(path)) {
            var offsets = new int[objects];

            writer.position(32 + (long) objects * Integer.BYTES);
            for (int i = 0; i < objects; i++) {
                offsets[i] = Math.toIntExact(writer.position());

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

    public static HashCode computeHash(ForbiddenWestGame game) throws IOException {
        var graph = game.getStreamingGraph();
        var system = game.getStreamingSystem();
        var linkTable = system.getFileData(Math.toIntExact(graph.linkTableID()), 0, graph.linkTableSize());
        return HashFunction.murmur3().hash(linkTable);
    }

    @Override
    public List<LinkProvider.Link> getIncomingLinks(ObjectId target) throws IOException {
        var graph = game.getStreamingGraph();
        var group = graph.group(target.groupId());
        int index = group.typeStart() + target.objectIndex();

        synchronized (reader) {
            return reader
                .position(offsets[index])
                .readObjects(readVarInt(reader), r -> readLink(graph, r));
        }
    }

    @Override
    public List<Link> getOutgoingLinks(ObjectId source) throws IOException {
        var object = game.readObject(source.groupId(), source.objectIndex());
        var info = visitObject(object);
        return info.out();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private static GroupInfo visitGroup(StreamingGroupData group, StreamingObjectReader reader) throws IOException {
        var result = reader.readGroup(group.groupID());
        var objects = new ArrayList<ObjectInfo>(group.numObjects());

        for (int i = 0; i < group.numObjects(); i++) {
            objects.add(visitObject(result.objects().get(i).object()));
        }

        return new GroupInfo(group, objects);
    }

    private static ObjectInfo visitObject(TypedObject object) {
        var links = new ArrayList<Link>();
        var visitor = new TypeVisitor() {
            @Override
            protected void visitContainer(ContainerTypeInfo typeInfo, Object object, TypePath.Builder builder) {
                if (typeInfo.itemType() instanceof AtomTypeInfo) {
                    return;
                }
                super.visitContainer(typeInfo, object, builder);
            }

            @Override
            protected void visitPointer(PointerTypeInfo typeInfo, Object object, TypePath.Builder builder) {
                if (object instanceof ObjectIdHolder holder) {
                    var objectId = holder.objectId();
                    links.add(new Link(objectId.groupId(), objectId.objectIndex(), builder.build()));
                }
            }
        };

        visitor.visit(object.getType(), object);

        return new ObjectInfo(links);
    }

    private static LinkProvider.Link readLink(StreamingGraphResource graph, BinaryReader reader) throws IOException {
        int groupId = readVarInt(reader);
        int objectIndex = readVarInt(reader);
        var objectType = graph.types().get(graph.group(groupId).typeStart() + objectIndex);
        var path = readPath(objectType, reader);
        return new LinkProvider.Link(groupId, objectIndex, path);
    }

    private static void writeLink(PackedLink link, BinaryWriter writer) throws IOException {
        writeVarInt(writer, link.groupId());
        writeVarInt(writer, link.objectIndex());
        writeVarInt(writer, link.path().length);
        writer.writeBytes(link.path());
    }

    private static TypePath readPath(ClassTypeInfo root, BinaryReader reader) throws IOException {
        var elements = new ArrayList<TypePath.Element>();
        var parent = (TypeInfo) root;

        for (long end = readVarInt(reader) + reader.position(); reader.position() < end; ) {
            int element = readVarInt(reader);
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

    private static int readVarInt(BinaryReader reader) throws IOException {
        int len = 0;
        int out = 0;
        while (true) {
            int tmp = reader.readByte();
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

    private record ObjectInfo(List<LinkProvider.Link> out) {
        private ObjectInfo {
            out = List.copyOf(out);
        }

        @Override
        public String toString() {
            return "ObjectInfo{out=" + out.size() + '}';
        }
    }

    private record GroupInfo(StreamingGroupData data, List<ObjectInfo> objects) {
        private GroupInfo {
            objects = List.copyOf(objects);
        }

        @Override
        public String toString() {
            return "GroupInfo{objects=" + objects.size() + '}';
        }
    }

    private record PackedLink(int groupId, int objectIndex, byte[] path) {
        static PackedLink pack(int groupId, int objectIndex, TypePath path) throws IOException {
            byte[] bytes;
            try (ByteArrayBinaryWriter writer = new ByteArrayBinaryWriter()) {
                writePath(path, writer);
                bytes = writer.toByteArray();
            }
            return new PackedLink(groupId, objectIndex, bytes);
        }
    }
}
