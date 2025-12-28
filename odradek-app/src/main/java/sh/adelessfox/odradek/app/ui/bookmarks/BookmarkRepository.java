package sh.adelessfox.odradek.app.ui.bookmarks;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import sh.adelessfox.odradek.event.EventBus;
import sh.adelessfox.odradek.game.ObjectId;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public final class BookmarkRepository {
    private final EventBus eventBus;
    private final Map<ObjectId, Bookmark> bookmarks = new LinkedHashMap<>();

    @Inject
    public BookmarkRepository(EventBus eventBus) {
        this.eventBus = eventBus;
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
     * @return {@code true} if the associated bookmark was removed, {@code false} otherwise
     */
    public boolean delete(ObjectId objectId) {
        var bookmark = bookmarks.remove(objectId);
        if (bookmark != null) {
            eventBus.publish(new BookmarkEvent.BookmarkRemoved(bookmark));
            return true;
        }
        return false;
    }
}
