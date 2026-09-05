package sh.adelessfox.odradek.app.ui.settings.gson;

import com.google.gson.*;
import sh.adelessfox.odradek.app.ui.bookmarks.BookmarkKey;
import sh.adelessfox.odradek.app.ui.settings.ApplicationSettings.BookmarkState;
import sh.adelessfox.odradek.game.decima.ObjectId;

import java.lang.reflect.Type;

public final class BookmarkStateAdapter implements JsonSerializer<BookmarkState>, JsonDeserializer<BookmarkState> {
    @Override
    public BookmarkState deserialize(
        JsonElement json,
        Type typeOfT,
        JsonDeserializationContext context
    ) throws JsonParseException {
        var object = json.getAsJsonObject();
        if (object.has("children")) {
            return context.<BookmarkState.Folder>deserialize(json, BookmarkState.Folder.class);
        } else if (object.has("groupId")) {
            var groupId = object.get("groupId").getAsInt();
            var name = object.get("name").getAsString();
            return new BookmarkState.Bookmark(new BookmarkKey.OfGroup(groupId), name);
        } else {
            var objectId = context.<ObjectId>deserialize(object.get("objectId"), ObjectId.class);
            var name = object.get("name").getAsString();
            return new BookmarkState.Bookmark(new BookmarkKey.OfObject(objectId), name);
        }
    }

    @Override
    public JsonElement serialize(BookmarkState src, Type typeOfSrc, JsonSerializationContext context) {
        return switch (src) {
            case BookmarkState.Folder folder -> context.serialize(folder, BookmarkState.Folder.class);
            case BookmarkState.Bookmark b when b.key() instanceof BookmarkKey.OfGroup(int groupId) -> {
                var object = new JsonObject();
                object.addProperty("name", b.name());
                object.addProperty("groupId", groupId);
                yield object;
            }
            case BookmarkState.Bookmark bookmark when bookmark.key() instanceof BookmarkKey.OfObject(var objectId) -> {
                var object = new JsonObject();
                object.addProperty("name", bookmark.name());
                object.add("objectId", context.serialize(objectId, ObjectId.class));
                yield object;
            }
            default -> throw new JsonParseException("Unknown BookmarkState type: " + src.getClass().getName());
        };
    }
}
