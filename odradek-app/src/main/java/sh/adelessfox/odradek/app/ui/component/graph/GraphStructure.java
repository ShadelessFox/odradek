package sh.adelessfox.odradek.app.ui.component.graph;

import sh.adelessfox.odradek.game.decima.DecimaGame;
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
    @SuppressWarnings("unused")
    abstract sealed class Groupable<T extends Groupable<T, ?>, O extends Enum<O>> {
        final EnumSet<O> options;
        final sh.adelessfox.odradek.game.decima.StreamingGraph graph;

        Groupable(sh.adelessfox.odradek.game.decima.StreamingGraph graph, EnumSet<O> options) {
            this.options = EnumSet.copyOf(options);
            this.graph = graph;
        }

        public Set<O> options() {
            return options;
        }
    }

    abstract sealed class GroupableByGroup extends Groupable<GroupableByGroup, GroupableByGroup.Option> {
        private static final Comparator<Map.Entry<sh.adelessfox.odradek.game.decima.StreamingGraph.Group, int[]>>
            DEFAULT_COMPARATOR = Comparator.comparingInt(e -> e.getKey().id()),
            COUNT_COMPARATOR = Comparator.comparingInt(e -> -e.getValue().length);

        public enum Option {
            GROUP_BY_GROUP,
            SORT_BY_COUNT
        }

        GroupableByGroup(sh.adelessfox.odradek.game.decima.StreamingGraph graph) {
            super(graph, EnumSet.of(Option.GROUP_BY_GROUP));
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

        GroupObject toGroupObject(sh.adelessfox.odradek.game.decima.StreamingGraph.Group group, int index, boolean includeGroupId) {
            return new GroupObject(graph, group, index, includeGroupId);
        }

        public Set<Option> options() {
            return options;
        }

        /** Get the groups that belong to this grouping. */
        protected abstract Stream<sh.adelessfox.odradek.game.decima.StreamingGraph.Group> groups();

        /** Get the indices of the objects in the given group. */
        protected abstract int[] indices(sh.adelessfox.odradek.game.decima.StreamingGraph.Group group);
    }

    abstract sealed class GroupableByType extends Groupable<GroupableByType, GroupableByType.Option> {
        public static final Comparator<Map.Entry<ClassTypeInfo, List<Integer>>>
            DEFAULT_COMPARATOR = Comparator.comparing(e -> e.getKey().name()),
            COUNT_COMPARATOR = Comparator.comparingInt(e -> -e.getValue().size());

        public enum Option {
            GROUP_BY_TYPE,
            SORT_BY_COUNT
        }

        GroupableByType(sh.adelessfox.odradek.game.decima.StreamingGraph graph) {
            super(graph, EnumSet.noneOf(Option.class));
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

        protected abstract sh.adelessfox.odradek.game.decima.StreamingGraph.Group group(int key);

        protected abstract int index(int key);

        private ClassTypeInfo type(int key) {
            return group(key).types().get(index(key));
        }
    }

    record GroupedByGroup(GroupableByGroup parent, sh.adelessfox.odradek.game.decima.StreamingGraph.Group group, int[] indices) implements GraphStructure {
        List<? extends GraphStructure> getGroupedChildren() {
            return IntStream.of(indices)
                .mapToObj(index -> parent.toGroupObject(group, index, false))
                .toList();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof GroupedByGroup that
                && parent.equals(that.parent)
                && group.id() == that.group.id();
        }

        @Override
        public int hashCode() {
            return Objects.hash(parent, group.id());
        }

        @Override
        public String toString() {
            return "Group %d (%d)".formatted(group.id(), indices.length);
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

    record Graph(sh.adelessfox.odradek.game.decima.StreamingGraph graph) implements GraphStructure {
        @Override
        public String toString() {
            return "Graph";
        }
    }

    record GraphGroups(sh.adelessfox.odradek.game.decima.StreamingGraph graph) implements GraphStructure {
        @Override
        public String toString() {
            return "Groups (" + graph.groups().size() + ")";
        }
    }

    record GraphObjects(sh.adelessfox.odradek.game.decima.StreamingGraph graph) implements GraphStructure {
        @Override
        public String toString() {
            return "Objects (" + graph.types().size() + ")";
        }
    }

    /*final class GraphRoots extends GroupableByType implements GraphStructure {
        GraphRoots(StreamingGraph graph) {
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
        protected StreamingGraph.Group group(int key) {
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
    }*/

    final class GraphObjectSet extends GroupableByGroup implements GraphStructure {
        private final ClassTypeInfo info;
        private final int count;

        GraphObjectSet(sh.adelessfox.odradek.game.decima.StreamingGraph graph, ClassTypeInfo info, int count) {
            super(graph);
            this.info = info;
            this.count = count;
        }

        public ClassTypeInfo info() {
            return info;
        }

        @Override
        protected Stream<sh.adelessfox.odradek.game.decima.StreamingGraph.Group> groups() {
            return graph.groups().stream()
                .filter(group -> group.types().stream().anyMatch(type -> type == info))
                .sorted(Comparator.comparingInt(sh.adelessfox.odradek.game.decima.StreamingGraph.Group::id))
                .map(sh.adelessfox.odradek.game.decima.StreamingGraph.Group.class::cast);
        }

        @Override
        protected int[] indices(sh.adelessfox.odradek.game.decima.StreamingGraph.Group group) {
            return IntStream.range(0, group.types().count())
                .filter(index -> group.types().get(index) == info)
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
        sh.adelessfox.odradek.game.decima.StreamingGraph graph,
        sh.adelessfox.odradek.game.decima.StreamingGraph.Group group,
        boolean filterable
    ) implements GraphStructure, Comparable<Group> {
        @Override
        public int compareTo(Group o) {
            return Integer.compare(group.id(), o.group.id());
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Group that && group.id() == that.group.id();
        }

        @Override
        public int hashCode() {
            return group.id();
        }

        @Override
        public String toString() {
            return "Group " + group.id();
        }
    }

    record GroupDependencies(sh.adelessfox.odradek.game.decima.StreamingGraph graph, sh.adelessfox.odradek.game.decima.StreamingGraph.Group group) implements GraphStructure {
        @Override
        public boolean equals(Object o) {
            return o instanceof GroupDependencies that && Objects.equals(group, that.group);
        }

        @Override
        public int hashCode() {
            return group.id();
        }

        @Override
        public String toString() {
            return "Dependencies (" + group.subGroups().count() + ")";
        }
    }

    /*record GroupDependents(StreamingGraph graph, StreamingGraph.Group group) implements GraphStructure {
        @Override
        public boolean equals(Object o) {
            return o instanceof GroupDependents that && Objects.equals(group, that.group);
        }

        @Override
        public int hashCode() {
            return group.id();
        }

        @Override
        public String toString() {
            return "Dependents (" + graph.incomingGroups(group).size() + ")";
        }
    }*/

    final class GroupObjects extends GroupableByType implements GraphStructure {
        private final sh.adelessfox.odradek.game.decima.StreamingGraph.Group group;

        public GroupObjects(sh.adelessfox.odradek.game.decima.StreamingGraph graph, sh.adelessfox.odradek.game.decima.StreamingGraph.Group group) {
            super(graph);
            this.group = group;
        }

        @Override
        protected IntStream keys() {
            return IntStream.range(0, group.types().count());
        }

        @Override
        protected sh.adelessfox.odradek.game.decima.StreamingGraph.Group group(int key) {
            return group;
        }

        @Override
        protected int index(int key) {
            return key;
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof GroupObjects that && group.id() == that.group.id();
        }

        @Override
        public int hashCode() {
            return group.id();
        }

        @Override
        public String toString() {
            return "Objects (" + group.types().count() + ")";
        }
    }

    final class GroupRoots extends GroupableByType implements GraphStructure {
        private final sh.adelessfox.odradek.game.decima.StreamingGraph.Group group;

        public GroupRoots(sh.adelessfox.odradek.game.decima.StreamingGraph graph, sh.adelessfox.odradek.game.decima.StreamingGraph.Group group) {
            super(graph);
            this.group = group;
        }

        @Override
        protected IntStream keys() {
            return IntStream.range(0, group.roots().size());
        }

        @Override
        protected sh.adelessfox.odradek.game.decima.StreamingGraph.Group group(int key) {
            return group;
        }

        @Override
        protected int index(int key) {
            return group.roots().get(key);
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof GroupRoots that && group.id() == that.group.id();
        }

        @Override
        public int hashCode() {
            return group.id();
        }

        @Override
        public String toString() {
            return "Roots (" + group.roots().size() + ")";
        }
    }

    record GroupObject(
        sh.adelessfox.odradek.game.decima.StreamingGraph graph,
        sh.adelessfox.odradek.game.decima.StreamingGraph.Group group,
        int indexAndIncludeGroupId
    ) implements GraphStructure, sh.adelessfox.odradek.game.decima.ObjectSupplier, sh.adelessfox.odradek.game.decima.ObjectIdHolder {
        public GroupObject(sh.adelessfox.odradek.game.decima.StreamingGraph graph, sh.adelessfox.odradek.game.decima.StreamingGraph.Group group, int index, boolean includeGroupId) {
            Objects.checkIndex(index, group.types().count());
            this(graph, group, index | (includeGroupId ? 0x80000000 : 0));
        }

        int index() {
            return indexAndIncludeGroupId & 0x7FFFFFFF;
        }

        boolean includeGroupId() {
            return indexAndIncludeGroupId < 0;
        }

        @Override
        public TypedObject readObject(DecimaGame game) throws IOException {
            return game.readObject(group.id(), index());
        }

        @Override
        public ClassTypeInfo objectType() {
            return group.types().get(index());
        }

        @Override
        public sh.adelessfox.odradek.game.decima.ObjectId objectId() {
            return new sh.adelessfox.odradek.game.decima.ObjectId(group.id(), index());
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof GroupObject that
                && group.id() == that.group.id()
                && indexAndIncludeGroupId == that.indexAndIncludeGroupId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(group.id(), indexAndIncludeGroupId);
        }

        @Override
        public String toString() {
            if (includeGroupId()) {
                return "[%d:%d] %s".formatted(group.id(), index(), objectType());
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
                new GraphObjects(graph)/*,
                new GraphRoots(graph)*/
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
                new GroupDependencies(graph, group)/*,
                new GroupDependents(graph, group)*/
            );
            case GroupDependencies(var graph, var group) -> group.subGroups().stream()
                    .map(subGroup -> new Group(graph, subGroup, false))
                    .toList();
            /*case GroupDependents(var graph, var group) -> graph.incomingGroups(group).stream()
                .sorted(Comparator.comparingInt(StreamingGraph.Group::id))
                .map(inGroup -> new Group(graph, inGroup, false))
                .toList();*/

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
            case GroupDependencies(var _, var group) -> group.subGroups().count() > 0;
            /*case GroupDependents(var graph, var group) -> !graph.incomingGroups(group).isEmpty();*/
            case GroupRoots roots -> !roots.group.roots().isEmpty();
            case GroupObject _ -> false;
            default -> true;
        };
    }
}
