package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.game.ds2.rtti.DS2TypeReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public final class UITextureFramesCallback implements ExtraBinaryDataCallback<DS2.UITextureFrames> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, DS2.UITextureFrames object) throws IOException {
        read(source, factory, object);
    }

    static DS2.UITextureFramesInfo read(BinarySource source, TypeFactory factory) throws IOException {
        var object = factory.newInstance(DS2.UITextureFramesInfo.class);
        read(source, factory, object);
        return object;
    }

    private static void read(BinarySource source, TypeFactory factory, DS2.UITextureFramesInfo object) throws IOException {
        object.data(source.readBytes(source.readInt()));
        object.spans(source.readLongs(source.readInt()));
        object.width(source.readInt());
        object.height(source.readInt());
        object.pixelFormat(DS2.EPixelFormat.valueOf(source.readInt()));
        object.frequency(DS2.EUpdateFrequency.valueOf((byte) source.readInt()));
        object.size(source.readInt());
        object.scale(DS2TypeReader.readCompound(DS2.FSize.class, source, factory));
    }
}
