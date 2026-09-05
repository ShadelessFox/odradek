package sh.adelessfox.odradek.app.ui.settings.gson;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import sh.adelessfox.odradek.app.ui.settings.ApplicationSettings.EditorState;
import sh.adelessfox.odradek.game.decima.ObjectId;
import sh.adelessfox.odradek.ui.editors.stack.EditorStackContainer;

import java.lang.reflect.Type;
import java.util.List;

public final class EditorStateAdapter implements JsonSerializer<EditorState>, JsonDeserializer<EditorState> {
    private static final TypeToken<?> OBJECT_ID_LIST = TypeToken.getParameterized(List.class, ObjectId.class);

    @Override
    public EditorState deserialize(
        JsonElement json,
        Type typeOfT,
        JsonDeserializationContext context
    ) throws JsonParseException {
        var object = json.getAsJsonObject();
        var type = object.get("type").getAsString();
        return switch (type) {
            case "leaf" -> {
                var objects = context.<List<ObjectId>>deserialize(object.get("objects"), OBJECT_ID_LIST.getType());
                var selection = object.get("selection").getAsInt();
                yield new EditorState.Leaf(objects, selection);
            }
            case "split" -> {
                var left = context.<EditorState>deserialize(object.get("left"), EditorState.class);
                var right = context.<EditorState>deserialize(object.get("right"), EditorState.class);
                var orientation = EditorStackContainer.Orientation.valueOf(object.get("orientation").getAsString());
                var proportion = object.get("proportion").getAsDouble();
                yield new EditorState.Split(left, right, orientation, proportion);
            }
            default -> throw new JsonParseException("Unknown EditorState type: " + type);
        };
    }

    @Override
    public JsonElement serialize(EditorState src, Type typeOfSrc, JsonSerializationContext context) {
        var object = new JsonObject();
        switch (src) {
            case EditorState.Leaf leaf -> {
                object.addProperty("type", "leaf");
                object.addProperty("selection", leaf.selection());
                object.add("objects", context.serialize(leaf.objects(), OBJECT_ID_LIST.getType()));
            }
            case EditorState.Split split -> {
                object.addProperty("type", "split");
                object.addProperty("orientation", split.orientation().name());
                object.addProperty("proportion", split.proportion());
                object.add("left", context.serialize(split.left(), EditorState.class));
                object.add("right", context.serialize(split.right(), EditorState.class));
            }
            default -> throw new JsonParseException("Unknown EditorState type: " + src.getClass().getName());
        }
        return object;
    }
}
