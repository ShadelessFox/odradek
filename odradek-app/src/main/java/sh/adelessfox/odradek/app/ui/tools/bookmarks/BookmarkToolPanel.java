package sh.adelessfox.odradek.app.ui.tools.bookmarks;

import sh.adelessfox.odradek.app.ui.ApplicationDataKeys;
import sh.adelessfox.odradek.app.ui.bookmarks.BookmarkEvent;
import sh.adelessfox.odradek.app.ui.bookmarks.Bookmarks;
import sh.adelessfox.odradek.app.ui.bookmarks.FolderId;
import sh.adelessfox.odradek.app.ui.settings.ApplicationSettings;
import sh.adelessfox.odradek.app.ui.tools.bookmarks.menu.BookmarkMenu;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.settings.Settings;
import sh.adelessfox.odradek.settings.SettingsEvent;
import sh.adelessfox.odradek.ui.Focusable;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;
import sh.adelessfox.odradek.ui.components.tree.TreeActionListener;
import sh.adelessfox.odradek.ui.tools.ToolPanel;
import sh.adelessfox.odradek.ui.tools.ToolSite;
import sh.adelessfox.odradek.ui.util.Fugue;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class BookmarkToolPanel implements ToolPanel, Focusable {
    public static final String ID = "bookmarks";

    public static final class Provider implements ToolPanel.Provider {
        @Override
        public ToolPanel create(ToolSite site) {
            var context = site.getDataContext();
            return new BookmarkToolPanel(
                site,
                context.require(ApplicationDataKeys.BOOKMARKS),
                context.require(ApplicationDataKeys.EVENT_BUS));
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
        eventBus.subscribe(SettingsEvent.class, event -> {
            switch (event) {
                case SettingsEvent.AfterLoad(var settings) -> loadSettings(settings);
                case SettingsEvent.BeforeSave(var settings) -> saveSettings(settings);
            }
        });

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

    private void loadSettings(Settings settings) {
        deserialize(bookmarks.rootFolderId(), settings.get(ApplicationSettings.BOOKMARKS).value());
    }

    private void deserialize(FolderId folderId, List<ApplicationSettings.BookmarkState> children) {
        for (var child : children) {
            switch (child) {
                case ApplicationSettings.BookmarkState.Bookmark bookmark ->
                    bookmarks.create(folderId, bookmark.key(), bookmark.name());
                case ApplicationSettings.BookmarkState.Folder folder ->
                    deserialize(bookmarks.createFolder(folderId, folder.name()), folder.children());
            }
        }
    }

    private void saveSettings(Settings settings) {
        var children = new ArrayList<ApplicationSettings.BookmarkState>();
        serialize(bookmarks.rootFolderId(), children);
        settings.get(ApplicationSettings.BOOKMARKS).set(children);
    }

    private void serialize(FolderId folderId, List<ApplicationSettings.BookmarkState> output) {
        var folders = bookmarks.getAllFoldersInFolder(folderId);
        for (var folder : folders) {
            var children = new ArrayList<ApplicationSettings.BookmarkState>();
            serialize(folder.id(), children);
            output.add(new ApplicationSettings.BookmarkState.Folder(folder.name(), children));
        }

        var bookmarks = this.bookmarks.getAllInFolder(folderId);
        for (var bookmark : bookmarks) {
            output.add(new ApplicationSettings.BookmarkState.Bookmark(bookmark.key(), bookmark.name()));
        }
    }
}
