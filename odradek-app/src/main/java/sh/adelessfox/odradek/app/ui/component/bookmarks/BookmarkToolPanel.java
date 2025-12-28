package sh.adelessfox.odradek.app.ui.component.bookmarks;

import jakarta.inject.Inject;
import sh.adelessfox.odradek.app.ui.Application;
import sh.adelessfox.odradek.app.ui.bookmarks.Bookmark;
import sh.adelessfox.odradek.app.ui.bookmarks.BookmarkEvent;
import sh.adelessfox.odradek.app.ui.bookmarks.BookmarkRepository;
import sh.adelessfox.odradek.app.ui.component.bookmarks.menu.BookmarkMenu;
import sh.adelessfox.odradek.app.ui.editors.ObjectEditorInputLazy;
import sh.adelessfox.odradek.app.ui.settings.Settings;
import sh.adelessfox.odradek.app.ui.settings.SettingsEvent;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.ui.actions.Actions;
import sh.adelessfox.odradek.ui.components.tool.ToolPanel;
import sh.adelessfox.odradek.ui.components.tree.StructuredTree;

import javax.swing.*;

public class BookmarkToolPanel implements ToolPanel {
    private final BookmarkRepository repository;
    private final EventBus eventBus;

    private StructuredTree<BookmarkStructure> tree;

    @Inject
    public BookmarkToolPanel(BookmarkRepository repository, EventBus eventBus) {
        this.repository = repository;
        this.eventBus = eventBus;

        eventBus.subscribe(SettingsEvent.class, event -> {
            switch (event) {
                case SettingsEvent.AfterLoad(var settings) -> loadSettings(settings);
                case SettingsEvent.BeforeSave(var settings) -> saveSettings(settings);
            }
        });
    }

    @Override
    public JComponent createComponent() {
        tree = new StructuredTree<>(new BookmarkStructure.Root(repository));
        tree.setShowsRootHandles(true);
        tree.setLabelProvider(new BookmarkLabelProvider());
        tree.addActionListener(event -> {
            if (event.getLastPathComponent() instanceof BookmarkStructure.Bookmark bookmark) {
                var manager = Application.getInstance().editors();
                var input = new ObjectEditorInputLazy(bookmark.id.groupId(), bookmark.id.objectIndex());
                manager.openEditor(input);
            }
        });
        Actions.installContextMenu(tree, BookmarkMenu.ID, tree);

        eventBus.subscribe(BookmarkEvent.class, _ -> tree.getModel().update());

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
                repository.create(bookmark.objectId(), bookmark.name());
            }
        });
    }

    private void saveSettings(Settings settings) {
        settings.bookmarks().set(repository.getAll());
    }
}
