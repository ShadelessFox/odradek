package sh.adelessfox.odradek.app.ui.bookmarks;

import org.junit.jupiter.api.Test;
import sh.adelessfox.odradek.event.DefaultEventBus;
import sh.adelessfox.odradek.game.decima.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookmarksTest {
    @Test
    void eventsIdentifyAffectedFolders() {
        var eventBus = new DefaultEventBus();
        var repository = new Bookmarks(eventBus);
        var events = new ArrayList<BookmarkEvent>();
        eventBus.subscribe(BookmarkEvent.class, events::add);

        var root = repository.rootFolderId();
        var target = repository.createFolder(root, "Target");
        var source = repository.createFolder(root, "Source");

        var objectId = new ObjectId(1, 2);
        repository.create(source, objectId, "Bookmark");
        repository.update(objectId, "Updated bookmark");
        repository.move(objectId, target);
        repository.delete(objectId);
        repository.updateFolder(source, "Updated source");
        repository.moveFolder(source, target);
        repository.deleteFolder(source);

        assertEquals(List.of(
            new BookmarkEvent.FolderAdded(new Folder(target, "Target"), root),
            new BookmarkEvent.FolderAdded(new Folder(source, "Source"), root),
            new BookmarkEvent.BookmarkAdded(new Bookmark(objectId, "Bookmark"), source),
            new BookmarkEvent.BookmarkUpdated(new Bookmark(objectId, "Updated bookmark"), source),
            new BookmarkEvent.BookmarkMoved(new Bookmark(objectId, "Updated bookmark"), source, target),
            new BookmarkEvent.BookmarkRemoved(new Bookmark(objectId, "Updated bookmark"), target),
            new BookmarkEvent.FolderUpdated(new Folder(source, "Updated source"), root),
            new BookmarkEvent.FolderMoved(new Folder(source, "Updated source"), root, target),
            new BookmarkEvent.FolderRemoved(new Folder(source, "Updated source"), target)
        ), events);
    }
}
