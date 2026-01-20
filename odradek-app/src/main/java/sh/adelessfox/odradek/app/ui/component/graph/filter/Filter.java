package sh.adelessfox.odradek.app.ui.component.graph.filter;

import sh.adelessfox.odradek.app.ui.component.graph.GraphStructure;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.util.Result;

import java.util.Set;

public sealed interface Filter {
    static Result<Filter, FilterError> parse(String input) {
        return FilterParser.parse(input);
    }

    boolean test(GraphStructure structure, Set<FilterOption> options);

    record GroupId(int id) implements Filter {
        @Override
        public boolean test(GraphStructure structure, Set<FilterOption> options) {
            return switch (structure) {
                case GraphStructure.Group group -> group.filterable() && group.group().groupID() == id;
                default -> true;
            };
        }

        @Override
        public String toString() {
            return "group:" + id;
        }
    }

    record GroupHasSubgroups() implements Filter {
        @Override
        public boolean test(GraphStructure structure, Set<FilterOption> options) {
            return switch (structure) {
                case GraphStructure.Group group -> group.group().subGroupCount() > 0;
                default -> true;
            };
        }

        @Override
        public String toString() {
            return "has:subgroups";
        }
    }

    record GroupHasRoots() implements Filter {
        @Override
        public boolean test(GraphStructure structure, Set<FilterOption> options) {
            return switch (structure) {
                case GraphStructure.Group group -> group.group().rootCount() > 0;
                default -> true;
            };
        }

        @Override
        public String toString() {
            return "has:roots";
        }
    }

    record GroupType(String name) implements Filter {
        @Override
        public boolean test(GraphStructure structure, Set<FilterOption> options) {
            return switch (structure) {
                case GraphStructure.Group(var graph, var group, _) ->
                    graph.types(group).anyMatch(info -> matches(info, options));
                case GraphStructure.GraphObjectSet(_, var info, _) -> matches(info, options);
                case GraphStructure.GroupObject object -> matches(object.objectType(), options);
                case GraphStructure.GroupedByType groupedByType -> matches(groupedByType.info(), options);
                default -> true;
            };
        }

        @Override
        public String toString() {
            return "type:" + name;
        }

        private boolean matches(TypeInfo info, Set<FilterOption> options) {
            return matches(info.name(), options);
        }

        private boolean matches(String input, Set<FilterOption> options) {
            boolean wholeWord = options.contains(FilterOption.WHOLE_WORD);
            boolean caseSensitive = options.contains(FilterOption.CASE_SENSITIVE);

            if (wholeWord && input.length() != name.length()) {
                return false;
            }
            if (caseSensitive) {
                if (wholeWord) {
                    return input.equals(name);
                } else {
                    return input.contains(name);
                }
            } else {
                if (wholeWord) {
                    return input.equalsIgnoreCase(name);
                } else {
                    return indexOfIgnoreCase(name, input) >= 0;
                }
            }
        }

        private static int indexOfIgnoreCase(String key, String haystack) {
            if (haystack.length() < key.length()) {
                return -1;
            }
            for (int i = haystack.length() - key.length(); i >= 0; i--) {
                if (haystack.regionMatches(true, i, key, 0, key.length())) {
                    return i;
                }
            }
            return -1;
        }
    }

    record And(Filter left, Filter right) implements Filter {
        @Override
        public boolean test(GraphStructure structure, Set<FilterOption> options) {
            return left.test(structure, options) && right.test(structure, options);
        }

        @Override
        public String toString() {
            return "(" + left + " AND " + right + ")";
        }
    }

    record Or(Filter left, Filter right) implements Filter {
        @Override
        public boolean test(GraphStructure structure, Set<FilterOption> options) {
            return left.test(structure, options) || right.test(structure, options);
        }

        @Override
        public String toString() {
            return "(" + left + " OR " + right + ")";
        }
    }

    record Not(Filter filter) implements Filter {
        @Override
        public boolean test(GraphStructure structure, Set<FilterOption> options) {
            return !filter.test(structure, options);
        }

        @Override
        public String toString() {
            return "!" + filter;
        }
    }
}
