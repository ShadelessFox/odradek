package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.UITexture;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.io.BoolFormat;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class UITextureCallback implements ExtraBinaryDataCallback<UITexture> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, UITexture object) throws IOException {
        var animated = reader.readBoolean(BoolFormat.BYTE);
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
