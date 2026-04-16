package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.game.ds2.rtti.DS2TypeReader;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public final class UITextureFramesCallback implements ExtraBinaryDataCallback<DS2.UITextureFrames> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, DS2.UITextureFrames object) throws IOException {
        read(reader, factory, object);
    }

    static DS2.UITextureFramesInfo read(BinaryReader reader, TypeFactory factory) throws IOException {
        var object = factory.newInstance(DS2.UITextureFramesInfo.class);
        read(reader, factory, object);
        return object;
    }

    private static void read(BinaryReader reader, TypeFactory factory, DS2.UITextureFramesInfo object) throws IOException {
        object.data(reader.readBytes(reader.readInt()));
        object.spans(reader.readLongs(reader.readInt()));
        object.width(reader.readInt());
        object.height(reader.readInt());
        object.pixelFormat(DS2.EPixelFormat.valueOf(reader.readInt()));
        object.frequency(DS2.EUpdateFrequency.valueOf((byte) reader.readInt()));
        object.size(reader.readInt());
        object.scale(DS2TypeReader.readCompound(DS2.FSize.class, reader, factory));
    }
}
