package sh.adelessfox.odradek.app.ui.settings.gson;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import sh.adelessfox.odradek.app.ui.bookmarks.BookmarkKey;
import sh.adelessfox.odradek.app.ui.settings.ApplicationSettings;
import sh.adelessfox.odradek.game.decima.ObjectId;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookmarkSettingsStateAdapterTest {
    private final com.google.gson.Gson gson;

    BookmarkSettingsStateAdapterTest() {
        var builder = new GsonBuilder();
        new ApplicationGsonAdapterProvider().configure(builder);
        gson = builder.create();
    }

    @Test
    void preservesTheExistingBookmarkJsonShape() {
        var state = List.of(
            new ApplicationSettings.BookmarkState.Folder("Folder", List.of(
                new ApplicationSettings.BookmarkState.Bookmark(new BookmarkKey.OfGroup(7), "Group"))),
            new ApplicationSettings.BookmarkState.Bookmark(
                new BookmarkKey.OfObject(new ObjectId(1, 2)),
                "Object"));

        var json = gson.toJsonTree(state, TypeToken.getParameterized(List.class, ApplicationSettings.BookmarkState.class).getType());

        assertEquals(JsonParser.parseString("""
            [
              {"name":"Folder","children":[{"name":"Group","groupId":7}]},
              {"name":"Object","objectId":"1:2"}
            ]
            """), json);
        assertEquals(state, gson.fromJson(json, TypeToken.getParameterized(List.class,
            ApplicationSettings.BookmarkState.class).getType()));
    }
}
