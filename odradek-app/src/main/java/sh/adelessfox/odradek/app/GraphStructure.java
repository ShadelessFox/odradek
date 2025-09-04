package sh.adelessfox.odradek.app;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingGroupData;
import sh.adelessfox.odradek.game.hfw.storage.StreamingGraphResource;
import sh.adelessfox.odradek.rtti.runtime.ClassTypeInfo;
import sh.adelessfox.odradek.ui.components.tree.TreeStructure;
import sh.adelessfox.odradek.util.Gatherers;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public sealed interface GraphStructure extends TreeStructure<GraphStructure> {
    abstract sealed class GroupableByType<K> implements GraphStructure {
        public enum Option {
            GROUP_BY_TYPE,
            SORT_BY_COUNT
        }

        final Set<Option> options = EnumSet.noneOf(Option.class);
        final StreamingGraphResource graph;

        GroupableByType(StreamingGraphResource graph) {
            this.graph = graph;
        }

        List<? extends GraphStructure> getChildren() {
            if (options.contains(Option.GROUP_BY_TYPE)) {
                var comparator = options.contains(Option.SORT_BY_COUNT)
                    ? Comparator.comparingInt((Map.Entry<ClassTypeInfo, List<K>> e) -> e.getValue().size()).reversed()
                    : Comparator.comparing((Map.Entry<ClassTypeInfo, List<K>> e) -> e.getKey().name().name());
                return keys()
                    .gather(Gatherers.groupingBy(this::type, IdentityHashMap::new, Collectors.toList()))
                    .sorted(comparator)
                    .map(entry -> new GroupedByType<>(this, entry.getKey(), entry.getValue()))
                    .toList();
            } else {
                return keys()
                    .map(this::toGroupObject)
                    .toList();
            }
        }

        GroupObject toGroupObject(K key) {
            return new GroupObject(graph, group(key), index(key));
        }

        public Set<Option> options() {
            return options;
        }

        public Stream<ClassTypeInfo> types() {
            return keys().map(this::type);
        }

        protected abstract Stream<K> keys();

        protected abstract StreamingGroupData group(K key);

        protected abstract int index(K key);

        private ClassTypeInfo type(K key) {
            return graph.types().get(group(key).typeStart() + index(key));
        }
    }

    record GroupedByType<K>(GroupableByType<K> parent, ClassTypeInfo info, List<K> keys) implements GraphStructure {
        List<? extends GraphStructure> getChildren() {
            return keys.stream()
                .map(parent::toGroupObject)
                .toList();
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof GroupedByType<?> that
                && parent.equals(that.parent)
                && info.equals(that.info)
                && keys.equals(that.keys);
        }

        @Override
        public String toString() {
            return "%s (%d)".formatted(info, keys.size());
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

    record GraphObjectSet(StreamingGraphResource graph, ClassTypeInfo info, int count) implements GraphStructure {
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

    final class GraphObjectSetGroup extends GroupableByType<Integer> {
        private final StreamingGroupData group;
        private final int[] indices;

        GraphObjectSetGroup(StreamingGraphResource graph, StreamingGroupData group, int[] indices) {
            super(graph);
            this.group = group;
            this.indices = indices;
        }

        @Override
        protected Stream<Integer> keys() {
            return Arrays.stream(indices).boxed();
        }

        @Override
        protected StreamingGroupData group(Integer key) {
            return group;
        }

        @Override
        protected int index(Integer key) {
            return key;
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof GraphObjectSetGroup that
                && group.groupID() == that.group.groupID()
                && Arrays.equals(indices, that.indices);
        }

        @Override
        public int hashCode() {
            return Objects.hash(group.groupID(), Arrays.hashCode(indices));
        }

        @Override
        public String toString() {
            return "Group %s (%d)".formatted(group.groupID(), indices.length);
        }
    }

    record Group(StreamingGraphResource graph, StreamingGroupData group) implements GraphStructure, Comparable<Group> {
        @Override
        public int compareTo(Group o) {
            return Integer.compare(group.groupID(), o.group.groupID());
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Group that
                && group.groupID() == that.group.groupID();
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
        public String toString() {
            return "Dependencies (" + group.subGroupCount() + ")";
        }
    }

    record GroupDependents(StreamingGraphResource graph, StreamingGroupData group) implements GraphStructure {
        @Override
        public String toString() {
            return "Dependents (" + graph.incomingGroups(group).size() + ")";
        }
    }

    final class GroupObjects extends GroupableByType<Integer> {
        private final StreamingGroupData group;

        public GroupObjects(StreamingGraphResource graph, StreamingGroupData group) {
            super(graph);
            this.group = group;
        }

        @Override
        protected Stream<Integer> keys() {
            return IntStream.range(0, group.typeCount()).boxed();
        }

        @Override
        protected StreamingGroupData group(Integer key) {
            return group;
        }

        @Override
        protected int index(Integer key) {
            return key;
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof GroupObjects that
                && group.groupID() == that.group.groupID();
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

    final class GroupRoots extends GroupableByType<Integer> {
        private final StreamingGroupData group;

        public GroupRoots(StreamingGraphResource graph, StreamingGroupData group) {
            super(graph);
            this.group = group;
        }

        @Override
        protected Stream<Integer> keys() {
            return IntStream.range(0, group.rootCount()).boxed();
        }

        @Override
        protected StreamingGroupData group(Integer key) {
            return group;
        }

        @Override
        protected int index(Integer key) {
            return graph.rootIndices()[group.rootStart() + key];
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof GroupRoots that
                && group.groupID() == that.group.groupID();
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

    record GroupObject(StreamingGraphResource graph, StreamingGroupData group, int index) implements GraphStructure {
        public ClassTypeInfo type() {
            return graph.types().get(group.typeStart() + index);
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
            ClassTypeInfo type = type();
            return "[%d] %s".formatted(index, type.name());
        }
    }

    final class GraphRoots extends GroupableByType<Integer> {
        GraphRoots(StreamingGraphResource graph) {
            super(graph);
        }

        @Override
        protected Stream<Integer> keys() {
            return IntStream.range(0, graph.rootIndices().length).boxed();
        }

        @Override
        protected StreamingGroupData group(Integer key) {
            return graph.group(graph.rootUUIDs().get(key));
        }

        @Override
        protected int index(Integer key) {
            return graph.rootIndices()[key];
        }

        @Override
        public String toString() {
            return "Roots (" + graph.rootIndices().length + ")";
        }
    }

    @Override
    default GraphStructure getRoot() {
        return this;
    }

    @Override
    default List<? extends GraphStructure> getChildren(GraphStructure parent) {
        return switch (parent) {
            case Graph(var graph) -> List.of(
                new GraphGroups(graph),
                new GraphObjects(graph),
                new GraphRoots(graph)
            );
            case GraphGroups(var graph) -> graph.groups().stream()
                .map(group -> new Group(graph, group))
                .sorted()
                .toList();
            case GraphObjects(var graph) -> graph.types().stream()
                .gather(Gatherers.groupingBy(Function.identity(), IdentityHashMap::new, Collectors.counting()))
                .sorted(Comparator.comparing(x -> x.getKey().name()))
                .map(entry -> new GraphObjectSet(graph, entry.getKey(), Math.toIntExact(entry.getValue())))
                .toList();
            case GraphObjectSet(var graph, var info, var _) -> graph.groups().stream()
                .filter(group -> graph.types(group).anyMatch(type -> type == info))
                .sorted(Comparator.comparingInt(StreamingGroupData::groupID))
                .map(group -> {
                    var indices = IntStream.range(0, group.typeCount())
                        .filter(index -> graph.types().get(group.typeStart() + index) == info)
                        .toArray();
                    return new GraphObjectSetGroup(graph, group, indices);
                })
                .toList();
            case Group(var graph, var group) -> List.of(
                new GroupObjects(graph, group),
                new GroupRoots(graph, group),
                new GroupDependencies(graph, group),
                new GroupDependents(graph, group)
            );
            case GroupDependencies(var graph, var group) ->
                Arrays.stream(graph.subGroups(), group.subGroupStart(), group.subGroupStart() + group.subGroupCount())
                    .mapToObj(graph::group)
                    .map(Objects::requireNonNull)
                    .map(subGroup -> new Group(graph, subGroup))
                    .toList();
            case GroupDependents(var graph, var group) -> graph.incomingGroups(group).stream()
                .sorted(Comparator.comparingInt(StreamingGroupData::groupID))
                .map(inGroup -> new Group(graph, inGroup))
                .toList();

            case GroupableByType<?> groupableByType -> groupableByType.getChildren();
            case GroupedByType<?> groupedByType -> groupedByType.getChildren();

            case GroupObject _ -> List.of();
        };
    }

    @Override
    default boolean hasChildren(GraphStructure node) {
        return switch (node) {
            case GroupDependencies(var _, var group) -> group.subGroupCount() > 0;
            case GroupDependents(var graph, var group) -> !graph.incomingGroups(group).isEmpty();
            case GroupRoots roots -> roots.group.rootCount() > 0;
            case GroupObject _ -> false;
            default -> true;
        };
    }
}
