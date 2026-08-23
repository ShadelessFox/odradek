package sh.adelessfox.odradek.app.ui.tools.bookmarks;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.bookmarks.BookmarkEvent;
import sh.adelessfox.odradek.app.ui.bookmarks.Bookmarks;
import sh.adelessfox.odradek.app.ui.bookmarks.FolderId;
import sh.adelessfox.odradek.app.ui.editors.ObjectEditorInputLazy;
import sh.adelessfox.odradek.app.ui.settings.Settings;
import sh.adelessfox.odradek.app.ui.settings.SettingsEvent;
import sh.adelessfox.odradek.app.ui.tools.bookmarks.menu.BookmarkMenu;
import sh.adelessfox.odradek.event.EventBus;
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

    @Singleton
    public static final class Provider implements ToolPanel.Provider {
        private final Bookmarks repository;
        private final EventBus eventBus;

        @Inject
        Provider(Bookmarks repository, EventBus eventBus) {
            this.repository = repository;
            this.eventBus = eventBus;
        }

        @Override
        public ToolPanel create(ToolSite site) {
            return new BookmarkToolPanel(repository, eventBus);
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
            return Fugue.getIcon("blue-document-bookmark");
        }
    }

    private final Bookmarks repository;
    private final EventBus eventBus;
    private StructuredTree<BookmarkStructure> tree;

    private BookmarkToolPanel(Bookmarks repository, EventBus eventBus) {
        this.repository = repository;
        this.eventBus = eventBus;
    }

    @Override
    public JComponent createComponent() {
        tree = new StructuredTree<>(new BookmarkStructure.Folder(repository, repository.rootFolderId(), "Root"));
        tree.setShowsRootHandles(true);
        tree.setLabelProvider(new BookmarkLabelProvider());
        tree.setPlaceholderText("No bookmarks\n\nRight-click on an object to bookmark it");
        tree.addActionListener(TreeActionListener.treePathClickedAdapter(event -> {
            var component = event.getLastPathComponent();
            if (component instanceof BookmarkStructure.Bookmark bookmark) {
                var manager = Application.getInstance().editors();
                var input = new ObjectEditorInputLazy(bookmark.id());
                manager.openEditor(input);
            }
        }));
        Actions.installContextMenu(tree, BookmarkMenu.ID, tree);

        // Setup drag-n-drop
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON);
        tree.setTransferHandler(new BookmarkTransferHandler(tree));

        eventBus.subscribe(BookmarkEvent.class, _ -> {
            // TODO figure out a better way to refresh the tree without losing selection
            tree.setSelectionPath(null);
            tree.getModel().refresh();
        });
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

    private void loadSettings(Settings settings) {
        settings.bookmarks().ifPresent(bookmarks -> deserialize(repository.rootFolderId(), bookmarks));
    }

    private void deserialize(FolderId folderId, List<Settings.BookmarkState> children) {
        for (Settings.BookmarkState child : children) {
            switch (child) {
                case Settings.BookmarkState.Bookmark bookmark ->
                    repository.create(folderId, bookmark.objectId(), bookmark.name());
                case Settings.BookmarkState.Folder folder ->
                    deserialize(repository.createFolder(folderId, folder.name()), folder.children());
            }
        }
    }

    private void saveSettings(Settings settings) {
        var children = new ArrayList<Settings.BookmarkState>();
        serialize(repository.rootFolderId(), children);
        settings.bookmarks().set(children);
    }

    private void serialize(FolderId folderId, List<Settings.BookmarkState> output) {
        var folders = repository.getAllFoldersInFolder(folderId);
        for (var folder : folders) {
            var children = new ArrayList<Settings.BookmarkState>();
            serialize(folder.id(), children);
            output.add(new Settings.BookmarkState.Folder(folder.name(), children));
        }

        var bookmarks = repository.getAllInFolder(folderId);
        for (var bookmark : bookmarks) {
            output.add(new Settings.BookmarkState.Bookmark(bookmark.objectId(), bookmark.name()));
        }
    }
}
