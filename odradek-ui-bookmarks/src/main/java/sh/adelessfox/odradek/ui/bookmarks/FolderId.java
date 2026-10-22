package sh.adelessfox.odradek.ui.bookmarks;

import java.util.UUID;

public record FolderId(UUID value) {
    public static FolderId random() {
        return new FolderId(UUID.randomUUID());
    }
}
