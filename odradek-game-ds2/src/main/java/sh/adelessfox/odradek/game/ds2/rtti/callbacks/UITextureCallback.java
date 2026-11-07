package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;
import wtf.reversed.toolbox.io.BoolFormat;

import java.io.IOException;

public final class UITextureCallback implements ExtraBinaryDataCallback<DS2.UITexture> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, DS2.UITexture object) throws IOException {
        var animated = source.readBool(BoolFormat.BYTE);
        object.animated(animated);

        var smallTextureSize = source.readInt();
        var largeTextureSize = source.readInt();

        if (smallTextureSize > 0) {
            if (animated) {
                object.smallTextureFrames(UITextureFramesCallback.read(source, factory));
            } else {
                object.smallTexture(TextureCallback.read(source, factory));
            }
        }

        if (largeTextureSize > 0) {
            if (animated) {
                object.largeTextureFrames(UITextureFramesCallback.read(source, factory));
            } else {
                object.largeTexture(TextureCallback.read(source, factory));
            }
        }
    }

}
