package sh.adelessfox.odradek.app.ui.component.graph;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.ui.component.common.Presenter;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.components.ValidationPopup;
import sh.adelessfox.odradek.util.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Singleton
public class GraphPresenter implements Presenter<GraphView> {
    private final GraphView view;

    @Inject
    public GraphPresenter(
        GraphView view,
        EventBus eventBus
    ) {
        this.view = view;

        // TODO: Update the selection model as well
        eventBus.subscribe(GraphViewEvent.UpdateFilter.class, event -> {
            var treeModel = view.getTree().getModel();
            var filter = createFilter(event.query(), event.matchCase(), event.matchWholeWord());
            var validation = view.getFilterValidationPopup();

            switch (filter) {
                case Result.Ok(var predicate) -> {
                    validation.setVisible(false);
                    treeModel.setFilter(predicate.orElse(null));
                    treeModel.refresh();
                }
                case Result.Error(var message) -> {
                    validation.setMessage(message);
                    validation.setSeverity(ValidationPopup.Severity.ERROR);
                    validation.setVisible(true);
                }
            }
        });
    }

    @Override
    public GraphView getView() {
        return view;
    }

    private static Result<Optional<Predicate<GraphStructure>>, String> createFilter(String input, boolean matchCase, boolean matchWholeWord) {
        if (input.isBlank()) {
            return Result.ok(Optional.empty());
        }
        List<Predicate<GraphStructure>> predicates = new ArrayList<>();
        for (String part : input.split("\\s+")) {
            var filter = new Filter(part, matchCase, matchWholeWord);
            var other = createFilterPart(part, filter);
            if (other instanceof Result.Ok(var predicate)) {
                predicates.add(predicate);
            } else {
                return other.map(Optional::of);
            }
        }
        Predicate<GraphStructure> predicate = predicates.stream()
            .reduce(Predicate::or)
            .orElse(null);
        return Result.ok(Optional.ofNullable(predicate));
    }

    private static Result<Predicate<GraphStructure>, String> createFilterPart(String input, Filter filter) {
        int colon = input.indexOf(':');
        if (colon < 0) {
            return Result.ok(s -> switch (s) {
                case GraphStructure.Group(var graph, var group, _) -> graph.types(group).anyMatch(filter::matches);
                case GraphStructure.GraphObjectSet(_, var info, _) -> filter.matches(info);
                case GraphStructure.GroupObject object -> filter.matches(object.objectType());
                case GraphStructure.GroupedByType groupedByType -> filter.matches(groupedByType.info());
                default -> true;
            });
        }

        var key = input.substring(0, colon);
        var value = input.substring(colon + 1);
        return switch (key) {
            case "has" -> switch (value) {
                case "subgroups" -> Result.ok(s -> !(s instanceof GraphStructure.Group(_, var group, _)) || group.subGroupCount() > 0);
                case "roots" -> Result.ok(s -> !(s instanceof GraphStructure.Group(_, var group, _)) || group.rootCount() > 0);
                default -> Result.error("Unknown selector '%s' for 'has' filter".formatted(value));
            };
            case "group" -> {
                int groupId;
                try {
                    groupId = Integer.parseInt(value);
                } catch (NumberFormatException ignored) {
                    yield Result.error("Expected a number value for the 'group' filter, got '%s'".formatted(value));
                }
                yield Result.ok(s -> !(s instanceof GraphStructure.Group(_, var group, var filterable)) || !filterable || group.groupID() == groupId);
            }
            default -> Result.error("Unknown filter '%s'".formatted(input));
        };
    }

    private record Filter(String query, boolean matchCase, boolean matchWholeWord) {
        boolean matches(TypeInfo info) {
            return matches(info.name());
        }

        boolean matches(String input) {
            if (matchWholeWord && input.length() != query.length()) {
                return false;
            }
            if (matchCase) {
                if (matchWholeWord) {
                    return input.equals(query);
                } else {
                    return input.contains(query);
                }
            } else {
                if (matchWholeWord) {
                    return input.equalsIgnoreCase(query);
                } else {
                    return indexOfIgnoreCase(query, input) >= 0;
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
}
