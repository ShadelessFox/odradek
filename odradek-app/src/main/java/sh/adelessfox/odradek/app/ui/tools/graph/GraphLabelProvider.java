package sh.adelessfox.odradek.app.ui.tools.graph;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.bookmarks.Bookmark;
import sh.adelessfox.odradek.game.decima.ObjectIdHolder;
import sh.adelessfox.odradek.ui.components.StyledFragment;
import sh.adelessfox.odradek.ui.components.StyledText;
import sh.adelessfox.odradek.ui.components.tree.StyledTreeLabelProvider;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.util.Optional;

final class GraphLabelProvider implements StyledTreeLabelProvider<GraphStructure> {
    @Override
    public Optional<StyledText> getStyledText(GraphStructure element) {
        var builder = StyledText.builder()
            .add(element.toString());
        if (element instanceof GraphStructure.GroupObject object) {
            findBookmark(object).ifPresent(bookmark -> builder.add(" " + bookmark.name(), StyledFragment.GRAYED));
        }
        return builder.build();
    }

    @Override
    public Optional<Icon> getIcon(GraphStructure element) {
        return Optional.ofNullable(switch (element) {
            case GraphStructure.Graph _ -> null;
            case GraphStructure.GraphGroups _, GraphStructure.GraphObjects _ -> Fugue.getIcon("folders-stack");
            case GraphStructure.Group _ -> Fugue.getIcon("folders");
            case GraphStructure.GroupDependencies _ -> Fugue.getIcon("folder-export");
            case GraphStructure.GroupDependents _ -> Fugue.getIcon("folder-import");
            case GraphStructure.GroupObject object -> {
                boolean bookmarked = findBookmark(object).isPresent();
                yield bookmarked
                    ? Fugue.getIcon("blue-document-bookmark")
                    : Fugue.getIcon("blue-document");
            }
            case GraphStructure.GraphRoots _,
                 GraphStructure.GroupRoots _ -> Fugue.getIcon("folder-stamp");
            case GraphStructure.GroupableByType _,
                 GraphStructure.GroupedByType _,
                 GraphStructure.GroupableByGroup _,
                 GraphStructure.GroupedByGroup _ -> Fugue.getIcon("folder-open-document");
        });
    }

    private static Optional<Bookmark> findBookmark(ObjectIdHolder holder) {
        return Application.getInstance().bookmarks().get(holder.objectId());
    }
}
