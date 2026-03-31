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
        private static final Comparator<Map.Entry<StreamingGroupData, int[]>>
            DEFAULT_COMPARATOR = Comparator.comparingInt(e -> e.getKey().groupID()),
            COUNT_COMPARATOR = Comparator.comparingInt(e -> -e.getValue().length);

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
                    ? COUNT_COMPARATOR
                    : DEFAULT_COMPARATOR;
                return groups()
                    .map(group -> Map.entry(group, indices(group)))
                    .sorted(comparator)
                    .map(entry -> new GroupedByGroup(this, entry.getKey(), entry.getValue()))
                    .toList();
            } else {
                return groups()
                    .flatMap(group -> IntStream.of(indices(group))
                        .mapToObj(index -> toGroupObject(group, index, true)))
                    .toList();
            }
        }

        GroupObject toGroupObject(StreamingGroupData group, int index, boolean includeGroupId) {
            return new GroupObject(graph, group, index, includeGroupId);
        }

        public Set<Option> options() {
            return options;
        }

        /** Get the groups that belong to this grouping. */
        protected abstract Stream<StreamingGroupData> groups();

        /** Get the indices of the objects in the given group. */
        protected abstract int[] indices(StreamingGroupData group);
    }

    abstract sealed class GroupableByType {
        public static final Comparator<Map.Entry<ClassTypeInfo, List<Integer>>>
            DEFAULT_COMPARATOR = Comparator.comparing(e -> e.getKey().name()),
            COUNT_COMPARATOR = Comparator.comparingInt(e -> -e.getValue().size());

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
                    ? COUNT_COMPARATOR
                    : DEFAULT_COMPARATOR;
                return keys()
                    .boxed()
                    .gather(Gatherers.groupingBy(this::type, IdentityHashMap::new, Collectors.toList()))
                    .sorted(comparator)
                    .map(entry -> new GroupedByType(
                        this,
                        entry.getKey(),
                        entry.getValue().stream()
                            .mapToInt(Integer::intValue)
                            .toArray()))
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
                .mapToObj(index -> parent.toGroupObject(group, index, false))
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
        GroupObject toGroupObject(int key) {
            return new GroupObject(graph, group(key), index(key), true);
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
        protected Stream<StreamingGroupData> groups() {
            return graph.groups().stream()
                .filter(group -> graph.types(group).anyMatch(type -> type == info))
                .sorted(Comparator.comparingInt(StreamingGroupData::groupID));
        }

        @Override
        protected int[] indices(StreamingGroupData group) {
            return IntStream.range(0, group.typeCount())
                .filter(index -> graph.types().get(group.typeStart() + index) == info)
                .toArray();
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
        int indexAndIncludeGroupId
    ) implements GraphStructure, ObjectSupplier, ObjectIdHolder {
        public GroupObject(StreamingGraphResource graph, StreamingGroupData group, int index, boolean includeGroupId) {
            Objects.checkIndex(index, group.numObjects());
            this(graph, group, index | (includeGroupId ? 0x80000000 : 0));
        }

        int index() {
            return indexAndIncludeGroupId & 0x7FFFFFFF;
        }

        boolean includeGroupId() {
            return indexAndIncludeGroupId >>> 31 != 0;
        }

        @Override
        public TypedObject readObject(Game game) throws IOException {
            return game.readObject(group.groupID(), index());
        }

        @Override
        public ClassTypeInfo objectType() {
            return graph.types().get(group.typeStart() + index());
        }

        @Override
        public ObjectId objectId() {
            return new ObjectId(group.groupID(), index());
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof GroupObject that
                && group.groupID() == that.group.groupID()
                && indexAndIncludeGroupId == that.indexAndIncludeGroupId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(group.groupID(), indexAndIncludeGroupId);
        }

        @Override
        public String toString() {
            if (includeGroupId()) {
                return "[%d:%d] %s".formatted(group.groupID(), index(), objectType());
            } else {
                return "[%d] %s".formatted(index(), objectType());
            }
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
