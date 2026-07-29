package sh.adelessfox.odradek.app.ui.tools.bookmarks;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.bookmarks.Bookmark;
import sh.adelessfox.odradek.app.ui.bookmarks.BookmarkEvent;
import sh.adelessfox.odradek.app.ui.bookmarks.Bookmarks;
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
        tree = new StructuredTree<>(new BookmarkStructure.Root(repository));
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
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

        eventBus.subscribe(BookmarkEvent.class, _ -> tree.getModel().refresh());
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
        settings.bookmarks().ifPresent(bookmarks -> {
            for (Bookmark bookmark : bookmarks) {
                this.repository.create(bookmark.objectId(), bookmark.name());
            }
        });
    }

    private void saveSettings(Settings settings) {
        settings.bookmarks().set(repository.getAll());
    }
}
