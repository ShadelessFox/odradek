package sh.adelessfox.odradek.app;

import sh.adelessfox.odradek.app.ui.tree.TreeStructure;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingGroupData;
import sh.adelessfox.odradek.game.hfw.storage.StreamingGraphResource;
import sh.adelessfox.odradek.rtti.runtime.ClassTypeInfo;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

record GraphStructure(
    StreamingGraphResource graph
) implements TreeStructure<GraphStructure.Element> {
    public sealed interface Element {
        record Root(StreamingGraphResource graph) implements Element {
            @Override
            public boolean equals(Object object) {
                return this == object;
            }

            @Override
            public int hashCode() {
                return System.identityHashCode(this);
            }

            @Override
            public String toString() {
                return "Graph";
            }
        }

        record Group(
            StreamingGraphResource graph,
            StreamingGroupData group
        ) implements Element, Comparable<Group> {
            @Override
            public String toString() {
                return "Group " + group.groupID();
            }

            @Override
            public int compareTo(Group o) {
                return Integer.compare(group.groupID(), o.group().groupID());
            }
        }

        record GroupDependencyGroups(
            StreamingGraphResource graph,
            StreamingGroupData group
        ) implements Element {
            @Override
            public String toString() {
                return "Dependencies (" + group.subGroupCount() + ")";
            }
        }

        record GroupDependentGroups(
            StreamingGraphResource graph,
            StreamingGroupData group
        ) implements Element {
            @Override
            public String toString() {
                return "Dependents (" + graph.incomingGroups(group).size() + ")";
            }
        }

        record GroupRoots(
            StreamingGraphResource graph,
            StreamingGroupData group
        ) implements Element {
            @Override
            public String toString() {
                return "Roots (" + group.rootCount() + ")";
            }
        }

        record GroupObjects(
            StreamingGraphResource graph,
            StreamingGroupData group,
            Set<Options> options
        ) implements Element {
            public enum Options {
                GROUP_BY_TYPE,
                SORT_BY_COUNT
            }

            public GroupObjects(StreamingGraphResource graph, StreamingGroupData group) {
                this(graph, group, EnumSet.noneOf(Options.class));
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
        ) implements Element {
            @Override
            public String toString() {
                return "%s (%d)".formatted(info.name(), indices.length);
            }
        }

        record Compound(
            StreamingGraphResource graph,
            StreamingGroupData group,
            int index
        ) implements Element {
            @Override
            public String toString() {
                ClassTypeInfo type = graph.types().get(group.typeStart() + index);
                return "[%d] %s".formatted(index, type.name());
            }
        }
    }

    @Override
    public Element getRoot() {
        return new Element.Root(graph);
    }

    @Override
    public List<? extends Element> getChildren(Element parent) {
        return switch (parent) {
            case Element.Root(var graph) -> graph.groups().stream()
                .map(group -> new Element.Group(graph, group))
                .sorted()
                .toList();
            case Element.Group(var graph, var group) -> List.of(
                new Element.GroupObjects(graph, group),
                new Element.GroupRoots(graph, group),
                new Element.GroupDependencyGroups(graph, group),
                new Element.GroupDependentGroups(graph, group)
            );
            case Element.GroupRoots(var graph, var group) ->
                IntStream.range(group.rootStart(), group.rootStart() + group.rootCount())
                    .mapToObj(index -> {
                        var rootGroup = graph.group(graph.rootUUIDs().get(index));
                        var rootIndex = graph.rootIndices()[index];
                        Objects.requireNonNull(rootGroup);
                        return new Element.Compound(graph, rootGroup, rootIndex);
                    })
                    .toList();
            case Element.GroupDependencyGroups(var graph, var group) -> Arrays.stream(graph.subGroups(),
                    group.subGroupStart(),
                    group.subGroupStart() + group.subGroupCount())
                .mapToObj(graph::group)
                .map(Objects::requireNonNull)
                .map(subGroup -> new Element.Group(graph, subGroup))
                .toList();
            case Element.GroupDependentGroups(var graph, var group) -> graph.incomingGroups(group).stream()
                .sorted(Comparator.comparingInt(StreamingGroupData::groupID))
                .map(inGroup -> new Element.Group(graph, inGroup))
                .toList();
            case Element.GroupObjects(var graph, var group, var options) -> {
                if (options.contains(Element.GroupObjects.Options.GROUP_BY_TYPE)) {
                    var indices = IntStream.range(0, group.typeCount())
                        .boxed()
                        .collect(Collectors.groupingBy(index -> graph.types().get(group.typeStart() + index)));
                    yield indices.entrySet().stream()
                        .sorted(options.contains(Element.GroupObjects.Options.SORT_BY_COUNT)
                            ? Comparator.comparingInt((Map.Entry<ClassTypeInfo, List<Integer>> e) -> e.getValue().size()).reversed()
                            : Comparator.comparing((Map.Entry<ClassTypeInfo, List<Integer>> e) -> e.getKey().name().name()))
                        .map(entry -> new Element.GroupObjectSet(graph, group, entry.getKey(), entry.getValue().stream().mapToInt(x -> x).toArray()))
                        .toList();
                } else {
                    yield IntStream.range(0, group.typeCount())
                        .mapToObj(index -> new Element.Compound(graph, group, index))
                        .toList();
                }
            }
            case Element.GroupObjectSet(var graph, var group, var _, var indices) -> IntStream.of(indices)
                .mapToObj(index -> new Element.Compound(graph, group, index))
                .toList();
            case Element.Compound _ -> List.of();
        };
    }

    @Override
    public boolean hasChildren(Element node) {
        return switch (node) {
            case Element.Root(var graph) -> !graph.groups().isEmpty();
            case Element.Group _ -> true;
            case Element.GroupObjects(var _, var group, var _) -> group.numObjects() > 0;
            case Element.GroupObjectSet(var _, var _, var _, var indices) -> indices.length > 0;
            case Element.GroupDependencyGroups(var _, var group) -> group.subGroupCount() > 0;
            case Element.GroupDependentGroups(var _, var group) -> !graph.incomingGroups(group).isEmpty();
            case Element.GroupRoots(var _, var group) -> group.rootCount() > 0;
            case Element.Compound _ -> false;
        };
    }

    @Override
    public String toString() {
        return "GraphStructure[]";
    }
}
