package sh.adelessfox.odradek.app.ui.settings.gson;

import com.google.gson.*;
import sh.adelessfox.odradek.app.ui.settings.Settings.BookmarkState;

import java.lang.reflect.Type;

public final class BookmarkStateAdapter implements JsonSerializer<BookmarkState>, JsonDeserializer<BookmarkState> {
    @Override
    public BookmarkState deserialize(
        JsonElement json,
        Type typeOfT,
        JsonDeserializationContext context
    ) throws JsonParseException {
        if (json.getAsJsonObject().has("children")) {
            return context.<BookmarkState.Folder>deserialize(json, BookmarkState.Folder.class);
        } else {
            return context.<BookmarkState.Bookmark>deserialize(json, BookmarkState.Bookmark.class);
        }
    }

    @Override
    public JsonElement serialize(BookmarkState src, Type typeOfSrc, JsonSerializationContext context) {
        return switch (src) {
            case BookmarkState.Folder folder -> context.serialize(folder, BookmarkState.Folder.class);
            case BookmarkState.Bookmark bookmark -> context.serialize(bookmark, BookmarkState.Bookmark.class);
        };
    }
}
