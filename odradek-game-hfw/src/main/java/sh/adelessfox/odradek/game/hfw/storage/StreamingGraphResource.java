package sh.adelessfox.odradek.game.hfw.storage;

import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeId;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.GGUUID;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingDataSourceLocator;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingGroupData;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingSourceSpan;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class StreamingGraphResource {
    private final HorizonForbiddenWest.StreamingGraphResource graph;

    private final List<ClassTypeInfo> types;
    private final Map<GGUUID, StreamingGroupData> groupByUuid = new HashMap<>(); // RootUUID -> Group
    private final Map<Integer, StreamingGroupData> groupById = new HashMap<>(); // GroupId -> Group
    private final Map<GGUUID, Integer> rootIndexByRootUuid = new HashMap<>(); // RootUUIDs -> RootIndices

    private final Map<StreamingGroupData, List<StreamingGroupData>> dependentGroups = new HashMap<>();

    public StreamingGraphResource(HorizonForbiddenWest.StreamingGraphResource graph, TypeFactory factory) throws IOException {
        this.graph = graph;
        this.types = readTypeTable(graph, factory);

        var rootUuids = graph.rootUUIDs();
        var rootIndices = graph.rootIndices();
        var groups = graph.groups();

        for (var group : groups) {
            groupById.put(group.groupID(), group);

            for (int i = group.rootStart(); i < group.rootStart() + group.rootCount(); i++) {
                groupByUuid.put(rootUuids.get(i), group);
                rootIndexByRootUuid.put(rootUuids.get(i), rootIndices[i]);
            }
        }

        for (var group : groups) {
            for (int i = 0; i < group.subGroupCount(); i++) {
                var subgroup = group(graph.subGroups()[group.subGroupStart() + i]);
                dependentGroups.computeIfAbsent(subgroup, _ -> new ArrayList<>()).add(group);
            }
        }
    }

    public long linkTableID() {
        return graph.linkTableID();
    }

    public int linkTableSize() {
        return graph.linkTableSize();
    }

    public List<String> files() {
        return graph.files();
    }

    public List<StreamingSourceSpan> spanTable() {
        return graph.spanTable();
    }

    public List<StreamingGroupData> groups() {
        return graph.groups();
    }

    public List<GGUUID> rootUUIDs() {
        return graph.rootUUIDs();
    }

    public int[] rootIndices() {
        return graph.rootIndices();
    }

    public int[] subGroups() {
        return graph.subGroups();
    }

    public List<StreamingDataSourceLocator> locatorTable() {
        return graph.locatorTable();
    }

    public List<ClassTypeInfo> types() {
        return types;
    }

    public Stream<ClassTypeInfo> types(StreamingGroupData group) {
        return types.subList(group.typeStart(), group.typeStart() + group.typeCount()).stream();
    }

    public List<StreamingGroupData> incomingGroups(StreamingGroupData group) {
        return dependentGroups.getOrDefault(group, List.of());
    }

    public StreamingGroupData group(GGUUID rootUUID) {
        return groupByUuid.get(rootUUID);
    }

    public StreamingGroupData group(int groupId) {
        return groupById.get(groupId);
    }

    public Integer rootIndex(GGUUID rootUUID) {
        return rootIndexByRootUuid.get(rootUUID);
    }

    private static List<ClassTypeInfo> readTypeTable(HorizonForbiddenWest.StreamingGraphResource graph, TypeFactory factory) throws IOException {
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

        var types = new ArrayList<ClassTypeInfo>();

        for (int i = 0; i < count; i++) {
            var index = Short.toUnsignedInt(reader.readShort());
            var hash = graph.typeHashes()[index];
            var type = factory.get(HFWTypeId.of(hash));
            types.add(type.asClass()); // FIXME not just classes
        }

        return List.copyOf(types);
    }
}
