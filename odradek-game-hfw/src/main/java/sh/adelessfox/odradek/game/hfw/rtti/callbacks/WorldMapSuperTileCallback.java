package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.TextureInfo;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.WorldMapSuperTile;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;
import java.util.ArrayList;

public class WorldMapSuperTileCallback implements ExtraBinaryDataCallback<WorldMapSuperTile> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, WorldMapSuperTile object) throws IOException {
        var smallTextureSize = reader.readInt();
        var largeTextureSize = reader.readInt();
        var mask = reader.readInt();

        var smallTextures = new ArrayList<TextureInfo>(4);
        var largeTextures = new ArrayList<TextureInfo>(4);

        if (smallTextureSize > 0) {
            for (int i = 0; i < 4; i++) {
                if ((mask & (1 << i)) != 0) {
                    smallTextures.add(TextureCallback.read(reader, factory));
                }
            }
        }

        if (largeTextureSize > 0) {
            for (int i = 0; i < 4; i++) {
                if ((mask & (1 << i)) != 0) {
                    largeTextures.add(TextureCallback.read(reader, factory));
                }
            }
        }

        object.smallTextures(smallTextures);
        object.largeTextures(largeTextures);
        object.mask(mask);
    }
}
