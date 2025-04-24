package sh.adelessfox.odradek.game.hfw.rtti.data;

import sh.adelessfox.odradek.rtti.Attr;

public interface StreamingDataSourceExtension {
    @Attr(name = "Locator", type = "uint64", position = 0, offset = 0)
    long locator();

    void locator(long locator);
}
