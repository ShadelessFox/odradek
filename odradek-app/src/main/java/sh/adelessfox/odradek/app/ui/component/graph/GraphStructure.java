package sh.adelessfox.odradek.app.ui.component.graph;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.game.ObjectId;
import sh.adelessfox.odradek.game.ObjectIdHolder;
import sh.adelessfox.odradek.game.ObjectSupplier;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingGroupData;
import sh.adelessfox.odradek.game.hfw.storage.StreamingGraphResource;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;
import sh.adelessfox.odradek.ui.components.tree.TreeStructure;
import sh.adelessfox.odradek.util.Gatherers;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public sealed interface GraphStructure extends TreeStructure<GraphStructure> {
    abstract sealed class GroupableByGroup {
        public enum Option {
            GROUP_BY_GROUP,
            SORT_BY_COUNT
        }

        final Set<Option> options = EnumSet.of(Option.GROUP_BY_GROUP);
        final StreamingGraphResource graph;

        GroupableByGroup(StreamingGraphResource graph) {
            this.graph = graph;
        }

        List<? extends GraphStructure> getGroupedChildren() {
            if (options.contains(Option.GROUP_BY_GROUP)) {
                var comparator = options.contains(Option.SORT_BY_COUNT)
                    ? Comparator.<Map.Entry<StreamingGroupData, int[]>>comparingInt(e -> e.getValue().length).reversed()
                    : Comparator.<Map.Entry<StreamingGroupData, int[]>>comparingInt(e -> e.getKey().groupID());
                return groupEntries()
                    .sorted(comparator)
                    .map(entry -> new GroupedByGroup(this, entry.getKey(), entry.getValue()))
                    .toList();
            } else {
                return groupEntries()
                    .flatMap(entry -> IntStream.of(entry.getValue())
                        .mapToObj(index -> new GroupObject(graph, entry.getKey(), index)))
                    .toList();
            }
        }

        GroupObject toGroupObject(StreamingGroupData group, int index) {
            return new GroupObject(graph, group, index);
        }

        public Set<Option> options() {
            return options;
        }

        public Stream<StreamingGroupData> groups() {
            return groupEntries().map(Map.Entry::getKey);
        }

        protected abstract Stream<Map.Entry<StreamingGroupData, int[]>> groupEntries();
    }

    abstract sealed class GroupableByType {
        public enum Option {
            GROUP_BY_TYPE,
            SORT_BY_COUNT
        }

        final Set<Option> options = EnumSet.noneOf(Option.class);
        final StreamingGraphResource graph;

        GroupableByType(StreamingGraphResource graph) {
            this.graph = graph;
        }

        List<? extends GraphStructure> getGroupedChildren() {
            if (options.contains(Option.GROUP_BY_TYPE)) {
                var comparator = options.contains(Option.SORT_BY_COUNT)
                    ? Comparator.comparingInt((Map.Entry<ClassTypeInfo, List<Integer>> e) -> e.getValue().size()).reversed()
                    : Comparator.comparing((Map.Entry<ClassTypeInfo, List<Integer>> e) -> e.getKey().name());
                return keys()
                    .boxed()
                    .gather(Gatherers.groupingBy(this::type, IdentityHashMap::new, Collectors.toList()))
                    .sorted(comparator)
                    .map(entry -> new GroupedByType(this, entry.getKey(), entry.getValue().stream().mapToInt(Integer::intValue).toArray()))
                    .toList();
            } else {
                return keys()
                    .mapToObj(this::toGroupObject)
                    .toList();
            }
        }

        GroupObject toGroupObject(int key) {
            return new GroupObject(graph, group(key), index(key));
        }

        public Set<Option> options() {
            return options;
        }

        public Stream<ClassTypeInfo> types() {
            return keys().mapToObj(this::type);
        }

        protected abstract IntStream keys();

        protected abstract StreamingGroupData group(int key);

        protected abstract int index(int key);

        private ClassTypeInfo type(int key) {
            return graph.types().get(group(key).typeStart() + index(key));
        }
    }

    record GroupedByGroup(GroupableByGroup parent, StreamingGroupData group, int[] indices) implements GraphStructure {
        List<? extends GraphStructure> getGroupedChildren() {
            return IntStream.of(indices)
                .mapToObj(index -> parent.toGroupObject(group, index))
                .toList();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof GroupedByGroup that
                && parent.equals(that.parent)
                && group.groupID() == that.group.groupID();
        }

        @Override
        public int hashCode() {
            return Objects.hash(parent, group.groupID());
        }

        @Override
        public String toString() {
            return "Group %d (%d)".formatted(group.groupID(), indices.length);
        }
    }

    record GroupedByType(GroupableByType parent, ClassTypeInfo info, int[] keys) implements GraphStructure {
        List<? extends GraphStructure> getGroupedChildren() {
            return IntStream.of(keys)
                .mapToObj(parent::toGroupObject)
                .toList();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof GroupedByType that && parent.equals(that.parent) && info.equals(that.info);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parent, info);
        }

        @Override
        public String toString() {
            return "%s (%d)".formatted(info, keys.length);
        }
    }

    record Graph(StreamingGraphResource graph) implements GraphStructure {
        @Override
        public String toString() {
            return "Graph";
        }
    }

    record GraphGroups(StreamingGraphResource graph) implements GraphStructure {
        @Override
        public String toString() {
            return "Groups (" + graph.groups().size() + ")";
        }
    }

    record GraphObjects(StreamingGraphResource graph) implements GraphStructure {
        @Override
        public String toString() {
            return "Objects (" + graph.types().size() + ")";
        }
    }

    final class GraphRoots extends GroupableByType implements GraphStructure {
        GraphRoots(StreamingGraphResource graph) {
            super(graph);
        }

        @Override
        protected IntStream keys() {
            return IntStream.range(0, graph.rootIndices().length);
        }

        @Override
        protected StreamingGroupData group(int key) {
            return graph.group(graph.rootUUIDs().get(key));
        }

        @Override
        protected int index(int key) {
            return graph.rootIndices()[key];
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GraphRoots that && graph.equals(that.graph);
        }

        @Override
        public int hashCode() {
            return graph.hashCode();
        }

        @Override
        public String toString() {
            return "Roots (" + graph.rootIndices().length + ")";
        }
    }

    final class GraphObjectSet extends GroupableByGroup implements GraphStructure {
        private final ClassTypeInfo info;
        private final int count;

        GraphObjectSet(StreamingGraphResource graph, ClassTypeInfo info, int count) {
            super(graph);
            this.info = info;
            this.count = count;
        }

        public ClassTypeInfo info() {
            return info;
        }

        public int count() {
            return count;
        }

        @Override
        protected Stream<Map.Entry<StreamingGroupData, int[]>> groupEntries() {
            return graph.groups().stream()
                .filter(group -> graph.types(group).anyMatch(type -> type == info))
                .sorted(Comparator.comparingInt(StreamingGroupData::groupID))
                .map(group -> {
                    var indices = IntStream.range(0, group.typeCount())
                        .filter(index -> graph.types().get(group.typeStart() + index) == info)
                        .toArray();
                    return Map.entry(group, indices);
                });
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof GraphObjectSet that && info.equals(that.info);
        }

        @Override
        public int hashCode() {
            return info.hashCode();
        }

        @Override
        public String toString() {
            return "%s (%d)".formatted(info, count);
        }
    }

    record Group(
        StreamingGraphResource graph,
        StreamingGroupData group,
        boolean filterable
    ) implements GraphStructure, Comparable<Group> {
        @Override
        public int compareTo(Group o) {
            return Integer.compare(group.groupID(), o.group.groupID());
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Group that && group.groupID() == that.group.groupID();
        }

        @Override
        public int hashCode() {
            return group.groupID();
        }

        @Override
        public String toString() {
            return "Group " + group.groupID();
        }
    }

    record GroupDependencies(StreamingGraphResource graph, StreamingGroupData group) implements GraphStructure {
        @Override
        public boolean equals(Object o) {
            return o instanceof GroupDependencies that && Objects.equals(group, that.group);
        }

        @Override
        public int hashCode() {
            return group.groupID();
        }

        @Override
        public String toString() {
            return "Dependencies (" + group.subGroupCount() + ")";
        }
    }

    record GroupDependents(StreamingGraphResource graph, StreamingGroupData group) implements GraphStructure {
        @Override
        public boolean equals(Object o) {
            return o instanceof GroupDependents that && Objects.equals(group, that.group);
        }

        @Override
        public int hashCode() {
            return group.groupID();
        }

        @Override
        public String toString() {
            return "Dependents (" + graph.incomingGroups(group).size() + ")";
        }
    }

    final class GroupObjects extends GroupableByType implements GraphStructure {
        private final StreamingGroupData group;

        public GroupObjects(StreamingGraphResource graph, StreamingGroupData group) {
            super(graph);
            this.group = group;
        }

        @Override
        protected IntStream keys() {
            return IntStream.range(0, group.typeCount());
        }

        @Override
        protected StreamingGroupData group(int key) {
            return group;
        }

        @Override
        protected int index(int key) {
            return key;
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof GroupObjects that && group.groupID() == that.group.groupID();
        }

        @Override
        public int hashCode() {
            return group.groupID();
        }

        @Override
        public String toString() {
            return "Objects (" + group.numObjects() + ")";
        }
    }

    final class GroupRoots extends GroupableByType implements GraphStructure {
        private final StreamingGroupData group;

        public GroupRoots(StreamingGraphResource graph, StreamingGroupData group) {
            super(graph);
            this.group = group;
        }

        @Override
        protected IntStream keys() {
            return IntStream.range(0, group.rootCount());
        }

        @Override
        protected StreamingGroupData group(int key) {
            return group;
        }

        @Override
        protected int index(int key) {
            return graph.rootIndices()[group.rootStart() + key];
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof GroupRoots that && group.groupID() == that.group.groupID();
        }

        @Override
        public int hashCode() {
            return group.groupID();
        }

        @Override
        public String toString() {
            return "Roots (" + group.rootCount() + ")";
        }
    }

    record GroupObject(
        StreamingGraphResource graph,
        StreamingGroupData group,
        int index
    ) implements GraphStructure, ObjectSupplier, ObjectIdHolder {
        @Override
        public TypedObject readObject(Game game) throws IOException {
            return game.readObject(group.groupID(), index);
        }

        @Override
        public ClassTypeInfo objectType() {
            return graph.types().get(group.typeStart() + index);
        }

        @Override
        public ObjectId objectId() {
            return new ObjectId(group.groupID(), index);
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof GroupObject that
                && group.groupID() == that.group.groupID()
                && index == that.index;
        }

        @Override
        public int hashCode() {
            return Objects.hash(group.groupID(), index);
        }

        @Override
        public String toString() {
            return "[%d] %s".formatted(index, objectType());
        }
    }

    @Override
    default List<? extends GraphStructure> getChildren() {
        return switch (this) {
            case Graph(var graph) -> List.of(
                new GraphGroups(graph),
                new GraphObjects(graph),
                new GraphRoots(graph)
            );
            case GraphGroups(var graph) -> graph.groups().stream()
                .map(group -> new Group(graph, group, true))
                .sorted()
                .toList();
            case GraphObjects(var graph) -> graph.types().stream()
                .gather(Gatherers.groupingBy(Function.identity(), IdentityHashMap::new, Collectors.counting()))
                .sorted(Comparator.comparing(x -> x.getKey().name()))
                .map(entry -> new GraphObjectSet(graph, entry.getKey(), Math.toIntExact(entry.getValue())))
                .toList();
            case Group(var graph, var group, _) -> List.of(
                new GroupObjects(graph, group),
                new GroupRoots(graph, group),
                new GroupDependencies(graph, group),
                new GroupDependents(graph, group)
            );
            case GroupDependencies(var graph, var group) ->
                Arrays.stream(graph.subGroups(), group.subGroupStart(), group.subGroupStart() + group.subGroupCount())
                    .mapToObj(graph::group)
                    .map(Objects::requireNonNull)
                    .map(subGroup -> new Group(graph, subGroup, false))
                    .toList();
            case GroupDependents(var graph, var group) -> graph.incomingGroups(group).stream()
                .sorted(Comparator.comparingInt(StreamingGroupData::groupID))
                .map(inGroup -> new Group(graph, inGroup, false))
                .toList();

            case GroupableByType groupableByType -> groupableByType.getGroupedChildren();
            case GroupedByType groupedByType -> groupedByType.getGroupedChildren();

            case GroupableByGroup groupableByGroup -> groupableByGroup.getGroupedChildren();
            case GroupedByGroup groupedByGroup -> groupedByGroup.getGroupedChildren();

            case GroupObject _ -> List.of();
        };
    }

    @Override
    default boolean hasChildren() {
        return switch (this) {
            case GroupDependencies(var _, var group) -> group.subGroupCount() > 0;
            case GroupDependents(var graph, var group) -> !graph.incomingGroups(group).isEmpty();
            case GroupRoots roots -> roots.group.rootCount() > 0;
            case GroupObject _ -> false;
            default -> true;
        };
    }
}
