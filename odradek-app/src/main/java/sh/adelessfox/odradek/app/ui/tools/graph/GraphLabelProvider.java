package sh.adelessfox.odradek.app.ui.tools.graph;

import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.bookmarks.Bookmark;
import sh.adelessfox.odradek.app.ui.bookmarks.BookmarkKey;
import sh.adelessfox.odradek.app.ui.bookmarks.Bookmarkable;
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
        if (element instanceof Bookmarkable bookmarkable) {
            findBookmark(bookmarkable.bookmarkKey())
                .ifPresent(bookmark -> builder.add(" " + bookmark.name(), StyledFragment.GRAYED));
        }
        return builder.build();
    }

    @Override
    public Optional<Icon> getIcon(GraphStructure element) {
        return Optional.ofNullable(switch (element) {
            case GraphStructure.Graph _ -> null;
            case GraphStructure.GraphGroups _, GraphStructure.GraphObjects _ -> Fugue.getIcon("folders");
            case GraphStructure.Group group -> {
                var key = new BookmarkKey.OfGroup(group.group().id());
                var bookmarked = findBookmark(key).isPresent();
                yield bookmarked
                    ? Fugue.getIcon("folder-bookmark")
                    : Fugue.getIcon("folder");
            }
            case GraphStructure.GroupDependencies _ -> Fugue.getIcon("folder-export");
            case GraphStructure.GroupDependents _ -> Fugue.getIcon("folder-import");
            case GraphStructure.GroupObject object -> {
                var key = new BookmarkKey.OfObject(object.objectId());
                var bookmarked = findBookmark(key).isPresent();
                yield bookmarked
                    ? Fugue.getIcon("document-bookmark")
                    : Fugue.getIcon("document");
            }
            case GraphStructure.GraphRoots _,
                 GraphStructure.GroupRoots _ -> Fugue.getIcon("folder-stamp");
            case GraphStructure.GroupableByType _,
                 GraphStructure.GroupedByType _,
                 GraphStructure.GroupableByGroup _,
                 GraphStructure.GroupedByGroup _ -> Fugue.getIcon("folder-open-document");
        });
    }

    private static Optional<Bookmark> findBookmark(BookmarkKey key) {
        return Application.getInstance().bookmarks().get(key);
    }
}
