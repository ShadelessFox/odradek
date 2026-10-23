package sh.adelessfox.odradek.app.ui.tools.bookmarks.data;

import java.util.UUID;

public record FolderId(UUID value) {
    public static FolderId random() {
        return new FolderId(UUID.randomUUID());
    }
}
