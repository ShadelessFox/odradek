package sh.adelessfox.odradek.app.component.graph;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.adelessfox.odradek.app.GraphStructure;
import sh.adelessfox.odradek.app.mvvm.Presenter;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.rtti.runtime.TypeInfo;

import java.util.function.Predicate;

public class GraphPresenter implements Presenter<GraphView> {
    private static final Logger log = LoggerFactory.getLogger(GraphPresenter.class);

    private final GraphView view;

    @Inject
    public GraphPresenter(
        GraphView view,
        EventBus eventBus
    ) {
        this.view = view;

        eventBus.subscribe(GraphViewEvent.UpdateFilter.class, event -> {
            var treeModel = view.getTree().getModel();
            treeModel.setFilter(createFilter(event.query(), event.matchCase(), event.matchWholeWord()));
            treeModel.update();
            // TODO: Update the selection model as well
        });
    }

    @Override
    public GraphView getView() {
        return view;
    }

    public void setBusy(boolean busy) {
        view.getTree().setEnabled(!busy);
    }

    private static Predicate<GraphStructure> createFilter(String input, boolean matchCase, boolean matchWholeWord) {
        if (input.isBlank()) {
            return null;
        }
        Predicate<GraphStructure> predicate = _ -> false;
        for (String part : input.split("\\s+")) {
            var filter = new Filter(part, matchCase, matchWholeWord);
            predicate = predicate.or(createFilterPart(part, filter));
        }
        return predicate;
    }

    private static Predicate<GraphStructure> createFilterPart(String input, Filter filter) {
        int colon = input.indexOf(':');
        if (colon < 0) {
            return s -> switch (s) {
                case GraphStructure.Group(var graph, var group) -> graph.types(group).anyMatch(filter::matches);
                case GraphStructure.GraphObjectSet(_, var info, _) -> filter.matches(info);
                case GraphStructure.GroupObject object -> filter.matches(object.type());
                case GraphStructure.GroupObjectSet(_, _, var info, _) -> filter.matches(info);
                default -> true;
            };
        }

        var key = input.substring(0, colon);
        var value = input.substring(colon + 1);
        return switch (key) {
            case "has" -> switch (value) {
                case "subgroups" -> s -> !(s instanceof GraphStructure.Group(_, var group)) || group.subGroupCount() > 0;
                case "roots" -> s -> !(s instanceof GraphStructure.Group(_, var group)) || group.rootCount() > 0;
                default -> {
                    log.warn("Unknown selector '{}' for 'has' filter", value);
                    yield _ -> false;
                }
            };
            case "group" -> {
                int groupId;
                try {
                    groupId = Integer.parseInt(value);
                } catch (NumberFormatException ignored) {
                    log.debug("Expected a number value for the 'group' filter, got {}", value);
                    yield _ -> false;
                }
                yield s -> !(s instanceof GraphStructure.Group(_, var group)) || group.groupID() == groupId;
            }
            default -> {
                log.warn("Unknown filter '{}'", input);
                yield _ -> false;
            }
        };
    }

    private record Filter(String query, boolean matchCase, boolean matchWholeWord) {
        boolean matches(TypeInfo info) {
            return matches(info.toString());
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
                int index = indexOfIgnoreCase(query, input);
                return index >= 0 && (!matchWholeWord || index == 0);
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
