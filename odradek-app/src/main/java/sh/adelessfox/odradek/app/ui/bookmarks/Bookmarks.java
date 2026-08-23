package sh.adelessfox.odradek.app.ui.bookmarks;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.game.decima.ObjectId;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A repository of bookmarks.
 */
@Singleton
public final class Bookmarks {
    private final EventBus eventBus;
    private final Map<ObjectId, Bookmark> bookmarks = new LinkedHashMap<>();
    private final Map<FolderId, Folder> folders = new LinkedHashMap<>();
    private final Map<ObjectId, FolderId> bookmarkToParent = new LinkedHashMap<>();
    private final Map<FolderId, FolderId> folderToParent = new LinkedHashMap<>();

    private final FolderId root = FolderId.random();

    @Inject
    Bookmarks(EventBus eventBus) {
        this.eventBus = eventBus;
        this.folders.put(root, new Folder(root, "Root"));
    }

    /**
     * Creates a new bookmark to the repository if it doesn't exist already.
     * <p>
     * Whether a bookmark exists or not is determined by its {@link Bookmark#objectId()}.
     *
     * @param objectId an object id to add bookmark for
     * @param name     name of the bookmark
     * @return {@code true} if bookmark was added, {@code false} otherwise
     */
    public boolean create(ObjectId objectId, String name) {
        var bookmark = new Bookmark(objectId, name);
        if (bookmarks.putIfAbsent(objectId, bookmark) == null) {
            bookmarkToParent.put(objectId, root);
            eventBus.publish(new BookmarkEvent.BookmarkAdded(bookmark));
            return true;
        }
        return false;
    }

    /**
     * Returns a bookmark for the given object id if it's present in the repository.
     *
     * @param objectId object id to check
     * @return a bookmark if it exists for the given object id, {@link Optional#empty()} otherwise
     */
    public Optional<Bookmark> get(ObjectId objectId) {
        return Optional.ofNullable(bookmarks.get(objectId));
    }

    /**
     * Retrieves all bookmarks in the repository.
     *
     * @return all bookmarks in the repository
     */
    public List<Bookmark> getAll() {
        return List.copyOf(bookmarks.values());
    }

    public List<Bookmark> getAllInFolder(FolderId folderId) {
        return bookmarks.entrySet().stream()
            .filter(entry -> folderId.equals(bookmarkToParent.get(entry.getKey())))
            .map(Map.Entry::getValue)
            .toList();
    }

    /**
     * Updates a bookmark for the given object id in the repository.
     *
     * @param objectId object id to update the bookmark for
     * @param name     new name of the bookmark
     * @return {@code true} if the bookmark was updated, {@code false} otherwise
     */
    public boolean update(ObjectId objectId, String name) {
        var bookmark = bookmarks.computeIfPresent(objectId, (_, _) -> new Bookmark(objectId, name));
        if (bookmark != null) {
            eventBus.publish(new BookmarkEvent.BookmarkUpdated(bookmark));
            return true;
        }
        return false;
    }

    /**
     * Deletes a bookmark for the given object id in the repository.
     *
     * @param objectId object id to remove bookmark for
     */
    public void delete(ObjectId objectId) {
        var bookmark = bookmarks.remove(objectId);
        if (bookmark == null) {
            throw new IllegalArgumentException("Bookmark with objectId " + objectId + " does not exist");
        }
        bookmarkToParent.remove(objectId);
        eventBus.publish(new BookmarkEvent.BookmarkRemoved(bookmark));
    }

    public boolean createFolder(FolderId folderId, String name) {
        return createFolder(root, folderId, name);
    }

    public boolean createFolder(FolderId parentFolderId, FolderId folderId, String name) {
        var folder = new Folder(folderId, name);
        if (folders.putIfAbsent(folderId, folder) == null) {
            folderToParent.put(folderId, parentFolderId);
            eventBus.publish(new BookmarkEvent.FolderAdded(folder));
            return true;
        }
        return false;
    }

    public Optional<FolderId> getParent(FolderId folderId) {
        return Optional.ofNullable(folderToParent.get(folderId));
    }

    public Optional<FolderId> getParent(ObjectId objectId) {
        return Optional.ofNullable(bookmarkToParent.get(objectId));
    }

    public List<Folder> getAllFolders() {
        return List.copyOf(folders.values());
    }

    public List<Folder> getAllFoldersInFolder(FolderId folderId) {
        return folders.entrySet().stream()
            .filter(entry -> folderId.equals(folderToParent.get(entry.getKey())))
            .map(Map.Entry::getValue)
            .toList();
    }

    public boolean updateFolder(FolderId folderId, String name) {
        var folder = folders.computeIfPresent(folderId, (_, _) -> new Folder(folderId, name));
        if (folder != null) {
            eventBus.publish(new BookmarkEvent.FolderUpdated(folder));
            return true;
        }
        return false;
    }

    public void deleteFolder(FolderId folderId) {
        var removed = folders.remove(folderId);
        if (removed == null) {
            throw new IllegalArgumentException("Folder with folderId " + folderId + " does not exist");
        }
        folderToParent.remove(folderId);
        getAllInFolder(folderId).forEach(bookmark -> delete(bookmark.objectId()));
        getAllFoldersInFolder(folderId).forEach(folder -> deleteFolder(folder.id()));
        eventBus.publish(new BookmarkEvent.FolderRemoved(removed));
    }

    public void move(ObjectId objectId, FolderId folderId) {
        var bookmark = bookmarks.get(objectId);
        if (bookmark == null) {
            throw new IllegalArgumentException("Bookmark with objectId " + objectId + " does not exist");
        }
        var folder = folders.get(folderId);
        if (folder == null) {
            throw new IllegalArgumentException("Folder with folderId " + folderId + " does not exist");
        }
        var oldFolderId = bookmarkToParent.put(objectId, folderId);
        if (oldFolderId == null || !oldFolderId.equals(folderId)) {
            eventBus.publish(new BookmarkEvent.BookmarkMoved(bookmark, oldFolderId, folderId));
        }
    }

    public void move(FolderId folderId, FolderId newParentFolderId) {
        var folder = folders.get(folderId);
        if (folder == null) {
            throw new IllegalArgumentException("Folder with folderId " + folderId + " does not exist");
        }
        var newParentFolder = folders.get(newParentFolderId);
        if (newParentFolder == null) {
            throw new IllegalArgumentException("Folder with folderId " + newParentFolderId + " does not exist");
        }
        var oldParentFolder = folderToParent.put(folderId, newParentFolderId);
        eventBus.publish(new BookmarkEvent.FolderMoved(folder, oldParentFolder, newParentFolderId));
    }

    public FolderId rootFolderId() {
        return root;
    }
}
