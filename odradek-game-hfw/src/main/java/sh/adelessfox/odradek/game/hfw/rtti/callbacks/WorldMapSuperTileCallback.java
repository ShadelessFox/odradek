package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;
import java.util.ArrayList;

public class WorldMapSuperTileCallback implements ExtraBinaryDataCallback<HFW.WorldMapSuperTile> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, HFW.WorldMapSuperTile object) throws IOException {
        var smallTextureSize = source.readInt();
        var largeTextureSize = source.readInt();
        var mask = source.readInt();

        var smallTextures = new ArrayList<HFW.TextureInfo>(4);
        var largeTextures = new ArrayList<HFW.TextureInfo>(4);

        if (smallTextureSize > 0) {
            for (int i = 0; i < 4; i++) {
                if ((mask & (1 << i)) != 0) {
                    smallTextures.add(TextureCallback.read(source, factory));
                }
            }
        }

        if (largeTextureSize > 0) {
            for (int i = 0; i < 4; i++) {
                if ((mask & (1 << i)) != 0) {
                    largeTextures.add(TextureCallback.read(source, factory));
                }
            }
        }

        object.smallTextures(smallTextures);
        object.largeTextures(largeTextures);
        object.mask(mask);
    }
}
