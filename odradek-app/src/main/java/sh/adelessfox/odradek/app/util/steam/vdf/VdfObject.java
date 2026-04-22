package sh.adelessfox.odradek.app.util.steam.vdf;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class VdfObject implements VdfElement {
    private final Map<String, VdfElement> members = new LinkedHashMap<>();

    public boolean has(String key) {
        return members.containsKey(key);
    }

    public VdfElement get(String key) {
        return members.get(key);
    }

    public VdfElement add(String key, VdfElement value) {
        return members.put(key, value);
    }

    public VdfElement remove(String key) {
        return members.remove(key);
    }

    public Set<Map.Entry<String, VdfElement>> entrySet() {
        return members.entrySet();
    }

    public Set<String> keySet() {
        return members.keySet();
    }

    public int size() {
        return members.size();
    }
}
