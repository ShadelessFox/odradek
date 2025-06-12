package sh.adelessfox.odradek.app;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingGroupData;
import sh.adelessfox.odradek.game.hfw.storage.StreamingGraphResource;
import sh.adelessfox.odradek.rtti.runtime.ClassTypeInfo;
import sh.adelessfox.odradek.ui.tree.TreeStructure;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public sealed interface GraphStructure extends TreeStructure<GraphStructure> {
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

    record GraphObjectSetGroup(
        StreamingGraphResource graph,
        StreamingGroupData group,
        int[] indices
    ) implements GraphStructure {
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

    record Group(
        StreamingGraphResource graph,
        StreamingGroupData group
    ) implements GraphStructure, Comparable<Group> {
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

    record GroupDependencies(
        StreamingGraphResource graph,
        StreamingGroupData group
    ) implements GraphStructure {
        @Override
        public String toString() {
            return "Dependencies (" + group.subGroupCount() + ")";
        }
    }

    record GroupDependents(
        StreamingGraphResource graph,
        StreamingGroupData group
    ) implements GraphStructure {
        @Override
        public String toString() {
            return "Dependents (" + graph.incomingGroups(group).size() + ")";
        }
    }

    record GroupRoots(
        StreamingGraphResource graph,
        StreamingGroupData group
    ) implements GraphStructure {
        @Override
        public String toString() {
            return "Roots (" + group.rootCount() + ")";
        }
    }

    record GroupObjects(
        StreamingGraphResource graph,
        StreamingGroupData group,
        Set<Option> options
    ) implements GraphStructure {
        public enum Option {
            GROUP_BY_TYPE,
            SORT_BY_COUNT
        }

        public GroupObjects(StreamingGraphResource graph, StreamingGroupData group) {
            this(graph, group, EnumSet.noneOf(Option.class));
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

    record GroupObjectSet(
        StreamingGraphResource graph,
        StreamingGroupData group,
        ClassTypeInfo info,
        int[] indices
    ) implements GraphStructure {
        @Override
        public boolean equals(Object object) {
            return object instanceof GroupObjectSet that
                && group.groupID() == that.group.groupID()
                && info.equals(that.info)
                && Arrays.equals(indices, that.indices);
        }

        @Override
        public int hashCode() {
            return Objects.hash(group, info, Arrays.hashCode(indices));
        }

        @Override
        public String toString() {
            return "%s (%d)".formatted(info, indices.length);
        }
    }

    record GroupObject(
        StreamingGraphResource graph,
        StreamingGroupData group,
        int index
    ) implements GraphStructure {
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

    @Override
    default GraphStructure getRoot() {
        return this;
    }

    @Override
    default List<? extends GraphStructure> getChildren(GraphStructure parent) {
        return switch (parent) {
            case Graph(var graph) -> List.of(
                new GraphGroups(graph),
                new GraphObjects(graph)
            );
            case GraphGroups(var graph) -> graph.groups().stream()
                .map(group -> new Group(graph, group))
                .sorted()
                .toList();
            case GraphObjects(var graph) -> {
                var types = graph.types().stream().collect(Collectors.groupingBy(
                    Function.identity(),
                    IdentityHashMap::new,
                    Collectors.counting()
                ));
                yield types.entrySet().stream()
                    .sorted(Comparator.comparing(x -> x.getKey().name()))
                    .map(entry -> new GraphObjectSet(graph, entry.getKey(), Math.toIntExact(entry.getValue())))
                    .toList();
            }
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
            case GraphObjectSetGroup(var graph, var group, var indices) -> IntStream.of(indices)
                .mapToObj(index -> new GroupObject(graph, group, index))
                .toList();
            case Group(var graph, var group) -> List.of(
                new GroupObjects(graph, group),
                new GroupRoots(graph, group),
                new GroupDependencies(graph, group),
                new GroupDependents(graph, group)
            );
            case GroupRoots(var graph, var group) -> IntStream.range(group.rootStart(), group.rootStart() + group.rootCount())
                .mapToObj(index -> {
                    var rootGroup = graph.group(graph.rootUUIDs().get(index));
                    var rootIndex = graph.rootIndices()[index];
                    Objects.requireNonNull(rootGroup);
                    return new GroupObject(graph, rootGroup, rootIndex);
                })
                .toList();
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
            case GroupObjects(var graph, var group, var options) -> {
                if (options.contains(GroupObjects.Option.GROUP_BY_TYPE)) {
                    var indices = IntStream.range(0, group.typeCount())
                        .boxed()
                        .collect(Collectors.groupingBy(
                            index -> graph.types().get(group.typeStart() + index),
                            IdentityHashMap::new,
                            Collectors.toList()
                        ));
                    var comparator = options.contains(GroupObjects.Option.SORT_BY_COUNT)
                        ? Comparator.comparingInt((Map.Entry<ClassTypeInfo, List<Integer>> e) -> e.getValue().size()).reversed()
                        : Comparator.comparing((Map.Entry<ClassTypeInfo, List<Integer>> e) -> e.getKey().name().name());
                    yield indices.entrySet().stream()
                        .sorted(comparator)
                        .map(entry -> new GroupObjectSet(graph,
                            group,
                            entry.getKey(),
                            entry.getValue().stream().mapToInt(x -> x).toArray()))
                        .toList();
                } else {
                    yield IntStream.range(0, group.typeCount())
                        .mapToObj(index -> new GroupObject(graph, group, index))
                        .toList();
                }
            }
            case GroupObjectSet(var graph, var group, var _, var indices) -> IntStream.of(indices)
                .mapToObj(index -> new GroupObject(graph, group, index))
                .toList();
            case GroupObject _ -> List.of();
        };
    }

    @Override
    default boolean hasChildren(GraphStructure node) {
        return switch (node) {
            case GroupDependencies(var _, var group) -> group.subGroupCount() > 0;
            case GroupDependents(var graph, var group) -> !graph.incomingGroups(group).isEmpty();
            case GroupRoots(var _, var group) -> group.rootCount() > 0;
            case GroupObject _ -> false;
            default -> true;
        };
    }
}
