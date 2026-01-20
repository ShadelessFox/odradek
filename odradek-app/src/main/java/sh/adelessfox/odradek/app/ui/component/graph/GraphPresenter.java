package sh.adelessfox.odradek.app.ui.component.graph;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.ui.component.common.Presenter;
import sh.adelessfox.odradek.app.ui.component.graph.filter.Filter;
import sh.adelessfox.odradek.app.ui.component.graph.filter.FilterOption;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.ui.components.ValidationPopup;
import sh.adelessfox.odradek.util.Result;

import java.util.EnumSet;
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
        var options = EnumSet.noneOf(FilterOption.class);
        if (matchCase) {
            options.add(FilterOption.CASE_SENSITIVE);
        }
        if (matchWholeWord) {
            options.add(FilterOption.WHOLE_WORD);
        }
        return Filter.parse(input, options)
            .map(filter -> Optional.<Predicate<GraphStructure>>of(filter::test))
            .mapError(error -> "%s at %d".formatted(error.message(), error.offset()));
    }
}
