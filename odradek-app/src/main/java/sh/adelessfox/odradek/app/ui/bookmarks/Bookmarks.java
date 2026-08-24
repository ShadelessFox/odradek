package sh.adelessfox.odradek.app.ui.bookmarks;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.game.decima.ObjectId;

import java.util.*;

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
     * @param parentFolderId id of the folder to add the bookmark to
     * @param objectId       an object id to add bookmark for
     * @param name           name of the bookmark
     */
    public synchronized void create(FolderId parentFolderId, ObjectId objectId, String name) {
        if (!folders.containsKey(parentFolderId)) {
            throw new IllegalArgumentException("Folder with folderId " + parentFolderId + " does not exist");
        }
        if (bookmarks.containsKey(objectId)) {
            throw new IllegalArgumentException("Bookmark with objectId " + objectId + " already exists");
        }
        var bookmark = new Bookmark(objectId, name);
        bookmarks.put(objectId, bookmark);
        bookmarkToParent.put(objectId, parentFolderId);
        eventBus.publish(new BookmarkEvent.BookmarkAdded(bookmark, parentFolderId));
    }

    /**
     * Returns a bookmark for the given object id if it's present in the repository.
     *
     * @param objectId object id to check
     * @return a bookmark if it exists for the given object id, {@link Optional#empty()} otherwise
     */
    public synchronized Optional<Bookmark> get(ObjectId objectId) {
        return Optional.ofNullable(bookmarks.get(objectId));
    }

    /**
     * Returns all bookmarks contained in the given folder.
     *
     * @param folderId id of the folder to get bookmarks from
     * @return a list of bookmarks contained in the given folder
     */
    public synchronized List<Bookmark> getAllInFolder(FolderId folderId) {
        return bookmarks.entrySet().stream()
            .filter(entry -> folderId.equals(bookmarkToParent.get(entry.getKey())))
            .map(Map.Entry::getValue)
            .toList();
    }

    /**
     * Returns the parent folder id of the given bookmark.
     *
     * @param objectId id of the bookmark to get the parent for
     * @return the id of the parent folder
     */
    public synchronized FolderId getParent(ObjectId objectId) {
        return Objects.requireNonNull(bookmarkToParent.get(objectId));
    }

    /**
     * Updates a bookmark for the given object id in the repository.
     *
     * @param objectId object id to update the bookmark for
     * @param name     new name of the bookmark
     */
    public synchronized void update(ObjectId objectId, String name) {
        var bookmark = bookmarks.computeIfPresent(objectId, (_, _) -> new Bookmark(objectId, name));
        if (bookmark == null) {
            throw new IllegalArgumentException("Bookmark with objectId " + objectId + " does not exist");
        }
        eventBus.publish(new BookmarkEvent.BookmarkUpdated(bookmark, getParent(objectId)));
    }

    /**
     * Moves a bookmark to a different folder in the repository.
     *
     * @param objectId id of the bookmark to move
     * @param folderId id of the folder to move the bookmark to
     */
    public synchronized void move(ObjectId objectId, FolderId folderId) {
        var bookmark = bookmarks.get(objectId);
        if (bookmark == null) {
            throw new IllegalArgumentException("Bookmark with objectId " + objectId + " does not exist");
        }
        var folder = folders.get(folderId);
        if (folder == null) {
            throw new IllegalArgumentException("Folder with folderId " + folderId + " does not exist");
        }
        var oldFolderId = Objects.requireNonNull(bookmarkToParent.put(objectId, folderId));
        if (!oldFolderId.equals(folderId)) {
            eventBus.publish(new BookmarkEvent.BookmarkMoved(bookmark, oldFolderId, folderId));
        }
    }

    /**
     * Deletes a bookmark for the given object id in the repository.
     *
     * @param objectId object id to remove bookmark for
     */
    public synchronized void delete(ObjectId objectId) {
        var bookmark = bookmarks.remove(objectId);
        if (bookmark == null) {
            throw new IllegalArgumentException("Bookmark with objectId " + objectId + " does not exist");
        }
        var parent = Objects.requireNonNull(bookmarkToParent.remove(objectId));
        eventBus.publish(new BookmarkEvent.BookmarkRemoved(bookmark, parent));
    }

    /**
     * Creates a new folder in the repository.
     *
     * @param parentFolderId id of the parent folder to add the new folder to
     * @param name           name of the new folder
     * @return id of the newly created folder
     */
    public synchronized FolderId createFolder(FolderId parentFolderId, String name) {
        if (!folders.containsKey(parentFolderId)) {
            throw new IllegalArgumentException("Folder with folderId " + parentFolderId + " does not exist");
        }
        var id = FolderId.random();
        var folder = new Folder(id, name);
        folders.put(id, folder);
        folderToParent.put(id, parentFolderId);
        eventBus.publish(new BookmarkEvent.FolderAdded(folder, parentFolderId));
        return id;
    }

    /**
     * Returns a folder for the given folder id if it's present in the repository.
     *
     * @param folderId folder id to check
     * @return a folder if it exists for the given folder id, {@link Optional#empty()} otherwise
     */
    public synchronized Optional<Folder> getFolder(FolderId folderId) {
        return Optional.ofNullable(folders.get(folderId));
    }

    /**
     * Returns all folders contained in the given folder.
     *
     * @param folderId id of the folder to get folders from
     * @return a list of folders contained in the given folder
     */
    public synchronized List<Folder> getAllFoldersInFolder(FolderId folderId) {
        return folders.entrySet().stream()
            .filter(entry -> folderId.equals(folderToParent.get(entry.getKey())))
            .map(Map.Entry::getValue)
            .toList();
    }

    /**
     * Returns the parent folder id of the given folder.
     *
     * @param folderId id of the folder to get the parent for
     * @return the id of the parent folder, {@link Optional#empty()} if the folder is the root folder
     */
    public synchronized Optional<FolderId> getParent(FolderId folderId) {
        return Optional.ofNullable(folderToParent.get(folderId));
    }

    /**
     * Updates a folder for the given folder id in the repository.
     *
     * @param folderId folder id to update the folder for
     * @param name     new name of the folder
     */
    public synchronized void updateFolder(FolderId folderId, String name) {
        if (folderId.equals(rootFolderId())) {
            throw new IllegalArgumentException("Cannot update the root folder");
        }
        var folder = folders.computeIfPresent(folderId, (_, _) -> new Folder(folderId, name));
        if (folder == null) {
            throw new IllegalArgumentException("Folder with folderId " + folderId + " does not exist");
        }
        eventBus.publish(new BookmarkEvent.FolderUpdated(folder, getParent(folderId).orElseThrow()));
    }

    /**
     * Moves a folder to a different parent folder in the repository.
     *
     * @param folderId          id of the folder to move
     * @param newParentFolderId id of the new parent folder to move the folder to
     */
    public synchronized void moveFolder(FolderId folderId, FolderId newParentFolderId) {
        if (folderId.equals(rootFolderId())) {
            throw new IllegalArgumentException("Cannot move the root folder");
        }
        var folder = folders.get(folderId);
        if (folder == null) {
            throw new IllegalArgumentException("Folder with folderId " + folderId + " does not exist");
        }
        var newParentFolder = folders.get(newParentFolderId);
        if (newParentFolder == null) {
            throw new IllegalArgumentException("Folder with folderId " + newParentFolderId + " does not exist");
        }
        if (isDescendant(folderId, newParentFolderId)) {
            throw new IllegalArgumentException("Cannot move folder " + folderId + " into its descendant " + newParentFolderId);
        }
        var oldParentFolder = Objects.requireNonNull(folderToParent.put(folderId, newParentFolderId));
        eventBus.publish(new BookmarkEvent.FolderMoved(folder, oldParentFolder, newParentFolderId));
    }

    /**
     * Deletes a folder for the given folder id in the repository.
     * <p>
     * This method also deletes all bookmarks and folders contained in the folder recursively.
     *
     * @param folderId folder id to remove
     */
    public synchronized void deleteFolder(FolderId folderId) {
        if (folderId.equals(rootFolderId())) {
            throw new IllegalArgumentException("Cannot delete the root folder");
        }
        var removed = folders.remove(folderId);
        if (removed == null) {
            throw new IllegalArgumentException("Folder with folderId " + folderId + " does not exist");
        }
        var parent = Objects.requireNonNull(folderToParent.remove(folderId));
        getAllInFolder(folderId).forEach(bookmark -> delete(bookmark.objectId()));
        getAllFoldersInFolder(folderId).forEach(folder -> deleteFolder(folder.id()));
        eventBus.publish(new BookmarkEvent.FolderRemoved(removed, parent));
    }

    /**
     * Checks if a folder is a descendant of another folder.
     *
     * @param folderId            id of the folder to check
     * @param potentialDescendant id of the potential descendant folder
     * @return {@code true} if the folder is a descendant of the other folder, {@code false} otherwise
     */
    public synchronized boolean isDescendant(FolderId folderId, FolderId potentialDescendant) {
        var current = potentialDescendant;
        while (true) {
            if (current.equals(folderId)) {
                return true;
            }
            var parent = getParent(current);
            if (parent.isEmpty()) {
                return false;
            }
            current = parent.get();
        }
    }

    /**
     * Returns the root folder id of the repository.
     * <p>
     * The id of the root folder is guaranteed to never change during the lifetime of the repository.
     *
     * @return the id of the root folder
     */
    public FolderId rootFolderId() {
        return root;
    }
}
