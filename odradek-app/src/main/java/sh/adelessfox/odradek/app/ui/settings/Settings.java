package sh.adelessfox.odradek.app.ui.settings;

import sh.adelessfox.odradek.game.ObjectId;

import java.util.List;

public final class Settings {
    private final Setting<List<ObjectId>> objects = new Setting<>();

    Settings() {
    }

    public Setting<List<ObjectId>> objects() {
        return objects;
    }
}
