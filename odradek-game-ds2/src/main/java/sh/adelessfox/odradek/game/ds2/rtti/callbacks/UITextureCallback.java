package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public final class UITextureCallback implements ExtraBinaryDataCallback<DS2.UITexture> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, DS2.UITexture object) throws IOException {
        var animated = reader.readByteBoolean();
        object.animated(animated);

        var smallTextureSize = reader.readInt();
        var largeTextureSize = reader.readInt();

        if (smallTextureSize > 0) {
            if (animated) {
                object.smallTextureFrames(UITextureFramesCallback.read(reader, factory));
            } else {
                object.smallTexture(TextureCallback.read(reader, factory));
            }
        }

        if (largeTextureSize > 0) {
            if (animated) {
                object.largeTextureFrames(UITextureFramesCallback.read(reader, factory));
            } else {
                object.largeTexture(TextureCallback.read(reader, factory));
            }
        }
    }

}
