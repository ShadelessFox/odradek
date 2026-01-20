package sh.adelessfox.odradek.app.ui.component.graph.filter;

import sh.adelessfox.odradek.app.ui.component.graph.GraphStructure;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.StreamingGroupData;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.util.Result;

import java.util.Set;
import java.util.function.ToIntFunction;

public sealed interface Filter {
    static Result<Filter, FilterError> parse(String input, Set<FilterOption> options) {
        return new FilterParser(options).parse(input);
    }

    boolean test(GraphStructure structure);

    record GroupId(int id) implements Filter {
        @Override
        public boolean test(GraphStructure structure) {
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

    record GroupHas(What what) implements Filter {
        enum What {
            SUBGROUPS("subgroups", StreamingGroupData::subGroupCount),
            ROOTS("roots", StreamingGroupData::rootCount);

            private final String name;
            private final ToIntFunction<StreamingGroupData> supplier;

            What(String name, ToIntFunction<StreamingGroupData> supplier) {
                this.name = name;
                this.supplier = supplier;
            }
        }

        @Override
        public boolean test(GraphStructure structure) {
            return switch (structure) {
                case GraphStructure.Group group -> what.supplier.applyAsInt(group.group()) > 0;
                default -> true;
            };
        }

        @Override
        public String toString() {
            return "has:" + what.name;
        }
    }

    record Type(String name, boolean caseSensitive, boolean wholeWord) implements Filter {
        @Override
        public boolean test(GraphStructure structure) {
            return switch (structure) {
                case GraphStructure.Group(var graph, var group, _) -> graph.types(group).anyMatch(this::matches);
                case GraphStructure.GraphObjectSet(_, var info, _) -> matches(info);
                case GraphStructure.GroupObject object -> matches(object.objectType());
                case GraphStructure.GroupedByType groupedByType -> matches(groupedByType.info());
                default -> true;
            };
        }

        @Override
        public String toString() {
            return switch ((caseSensitive ? 1 : 0) | (wholeWord ? 2 : 0)) {
                case 1 -> "type[c]:" + name;
                case 2 -> "type[w]:" + name;
                case 3 -> "type[cw]:" + name;
                default -> "type:" + name;
            };
        }

        private boolean matches(TypeInfo info) {
            return matches(info.name());
        }

        private boolean matches(String input) {
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
        public boolean test(GraphStructure structure) {
            return left.test(structure) && right.test(structure);
        }

        @Override
        public String toString() {
            return "(" + left + " AND " + right + ")";
        }
    }

    record Or(Filter left, Filter right) implements Filter {
        @Override
        public boolean test(GraphStructure structure) {
            return left.test(structure) || right.test(structure);
        }

        @Override
        public String toString() {
            return "(" + left + " OR " + right + ")";
        }
    }

    record Not(Filter filter) implements Filter {
        @Override
        public boolean test(GraphStructure structure) {
            return !filter.test(structure);
        }

        @Override
        public String toString() {
            return "!" + filter;
        }
    }
}
