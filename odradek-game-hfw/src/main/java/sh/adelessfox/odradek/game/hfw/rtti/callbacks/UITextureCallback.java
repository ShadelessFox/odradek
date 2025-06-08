package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.Attr;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class UITextureCallback implements ExtraBinaryDataCallback<UITextureCallback.UITextureData> {
    public interface UITextureData {
        @Attr(name = "Animated", type = "bool", position = 0)
        boolean animated();

        void animated(boolean value);

        @Attr(name = "SmallTexture", type = "TextureData", position = 0)
        TextureCallback.TextureData smallTexture();

        void smallTexture(TextureCallback.TextureData value);

        @Attr(name = "LargeTexture", type = "TextureData", position = 1)
        TextureCallback.TextureData largeTexture();

        void largeTexture(TextureCallback.TextureData value);

        @Attr(name = "SmallTextureFrames", type = "UITextureFramesData", position = 2)
        UITextureFramesCallback.UITextureFramesData smallTextureFrames();

        void smallTextureFrames(UITextureFramesCallback.UITextureFramesData value);

        @Attr(name = "LargeTextureFrames", type = "UITextureFramesData", position = 3)
        UITextureFramesCallback.UITextureFramesData largeTextureFrames();

        void largeTextureFrames(UITextureFramesCallback.UITextureFramesData value);
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, UITextureData object) throws IOException {
        var animated = reader.readByteBoolean();
        object.animated(animated);

        var smallTextureSize = reader.readInt();
        var largeTextureSize = reader.readInt();

        if (smallTextureSize > 0) {
            if (animated) {
                object.smallTextureFrames(UITextureFramesCallback.UITextureFramesData.read(reader, factory));
            } else {
                object.smallTexture(TextureCallback.TextureData.read(reader, factory));
            }
        }

        if (largeTextureSize > 0) {
            if (animated) {
                object.largeTextureFrames(UITextureFramesCallback.UITextureFramesData.read(reader, factory));
            } else {
                object.largeTexture(TextureCallback.TextureData.read(reader, factory));
            }
        }
    }
}
