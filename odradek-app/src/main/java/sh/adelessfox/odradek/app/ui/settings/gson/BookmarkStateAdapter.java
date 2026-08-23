package sh.adelessfox.odradek.app.ui.settings.gson;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import sh.adelessfox.odradek.app.ui.settings.Settings;
import sh.adelessfox.odradek.game.decima.ObjectId;

import java.lang.reflect.Type;
import java.util.List;

public final class BookmarkStateAdapter
    implements JsonSerializer<Settings.BookmarkState>, JsonDeserializer<Settings.BookmarkState> {

    private static final TypeToken<?> ELEMENT_LIST = TypeToken.getParameterized(List.class, Settings.BookmarkState.class);

    @Override
    public Settings.BookmarkState deserialize(
        JsonElement json,
        Type typeOfT,
        JsonDeserializationContext context
    ) throws JsonParseException {
        var object = json.getAsJsonObject();
        var folder = object.has("children");
        if (folder) {
            var name = object.get("name").getAsString();
            var children = context.<List<Settings.BookmarkState>>deserialize(object.getAsJsonArray("children"), ELEMENT_LIST.getType());
            return new Settings.BookmarkState.Folder(name, children);
        } else {
            var objectId = context.<ObjectId>deserialize(object.get("objectId"), ObjectId.class);
            var name = object.get("name").getAsString();
            return new Settings.BookmarkState.Bookmark(objectId, name);
        }
    }

    @Override
    public JsonElement serialize(Settings.BookmarkState src, Type typeOfSrc, JsonSerializationContext context) {
        var object = new JsonObject();
        switch (src) {
            case Settings.BookmarkState.Folder folder -> {
                object.addProperty("name", folder.name());
                object.add("children", context.serialize(folder.children(), ELEMENT_LIST.getType()));
            }
            case Settings.BookmarkState.Bookmark bookmark -> {
                object.addProperty("name", bookmark.name());
                object.add("objectId", context.serialize(bookmark.objectId(), ObjectId.class));
            }
            default -> throw new JsonParseException("Unknown BookmarkState type: " + src.getClass().getName());
        }
        return object;
    }
}
