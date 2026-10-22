package sh.adelessfox.odradek.ui.bookmarks.internal;

import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.ui.Focusable;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.bookmarks.BookmarkDataKeys;
import sh.adelessfox.odradek.ui.bookmarks.BookmarkEvent;
import sh.adelessfox.odradek.ui.bookmarks.BookmarkMenu;
import sh.adelessfox.odradek.ui.bookmarks.Bookmarks;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.components.tree.TreeActionListener;
import sh.adelessfox.odradek.ui.data.DataKeys;
import sh.adelessfox.odradek.ui.tools.ToolPanel;
import sh.adelessfox.odradek.ui.tools.ToolSite;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.util.List;

public class BookmarkToolPanel implements ToolPanel, Focusable {
    public static final String ID = "bookmarks";

    public static final class Provider implements ToolPanel.Provider {
        @Override
        public ToolPanel create(ToolSite site) {
            var context = site.getDataContext();
            return new BookmarkToolPanel(
                site,
                context.require(BookmarkDataKeys.BOOKMARKS),
                context.require(DataKeys.EVENT_BUS));
        }

        @Override
        public String id() {
            return BookmarkToolPanel.ID;
        }

        @Override
        public String name() {
            return "Bookmarks";
        }

        @Override
        public Icon icon() {
            return Fugue.getIcon("document-bookmark");
        }

        @Override
        public Placement placement() {
            return new Placement(Placement.Anchor.LEFT, false);
        }

        @Override
        public int order() {
            return 100;
        }
    }

    private final ToolSite site;
    private final Bookmarks bookmarks;
    private final EventBus eventBus;
    private StructuredTree<BookmarkStructure> tree;

    private BookmarkToolPanel(
        ToolSite site,
        Bookmarks bookmarks,
        EventBus eventBus
    ) {
        this.site = site;
        this.bookmarks = bookmarks;
        this.eventBus = eventBus;
    }

    @Override
    public JComponent createComponent() {
        tree = new StructuredTree<>(new BookmarkStructure.Folder(bookmarks, bookmarks.rootFolderId(), "Root"), site.getDataContext());
        tree.setShowsRootHandles(true);
        tree.setLabelProvider(new BookmarkLabelProvider(bookmarks));
        tree.setPlaceholderText("No bookmarks\n\nRight-click on an object to bookmark it");
        tree.addActionListener(TreeActionListener.treePathClickedAdapter(event -> {
            var component = event.getLastPathComponent();
            if (component instanceof BookmarkStructure.ObjectBookmark bookmark) {
                eventBus.publish(new BookmarkEvent.OpenRequested(bookmark.objectId()));
            }
        }));
        Actions.installContextMenu(tree, BookmarkMenu.ID, tree.getDataContext());

        // Setup drag-n-drop
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON);
        tree.setTransferHandler(new BookmarkTransferHandler(bookmarks, tree));

        eventBus.subscribe(BookmarkEvent.class, e -> SwingUtilities.invokeLater(() -> handleBookmarkEvent(e)));

        return new JScrollPane(tree);
    }

    @Override
    public boolean isFocused() {
        return tree.isFocusOwner();
    }

    @Override
    public void setFocus() {
        tree.requestFocusInWindow();
    }

    private void handleBookmarkEvent(BookmarkEvent event) {
        var folders = switch (event) {
            case BookmarkEvent.BookmarkAdded(_, var parent) -> List.of(parent);
            case BookmarkEvent.BookmarkUpdated(_, var parent) -> List.of(parent);
            case BookmarkEvent.BookmarkRemoved(_, var parent) -> List.of(parent);
            case BookmarkEvent.BookmarkMoved(_, var oldParent, var newParent) -> List.of(oldParent, newParent);
            case BookmarkEvent.FolderAdded(_, var parent) -> List.of(parent);
            case BookmarkEvent.FolderUpdated(_, var parent) -> List.of(parent);
            case BookmarkEvent.FolderRemoved(_, var parent) -> List.of(parent);
            case BookmarkEvent.FolderMoved(_, var oldParent, var newParent) -> List.of(oldParent, newParent);
            case BookmarkEvent.OpenRequested _ -> List.of();
        };

        if (folders.isEmpty()) {
            return;
        }

        tree.updatePreservingSelection(BookmarkStructure::equals, () -> {
            for (var folderId : folders) {
                tree.getModel()
                    .findLoadedPath(e -> e instanceof BookmarkStructure.Folder folder && folder.id().equals(folderId))
                    .ifPresent(tree.getModel()::refresh);
            }
        });
    }
}
