package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class UITextureFramesCallback implements ExtraBinaryDataCallback<HFW.UITextureFrames> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, HFW.UITextureFrames object) throws IOException {
        read(reader, factory, object);
    }

    static HFW.UITextureFramesInfo read(BinaryReader reader, TypeFactory factory) throws IOException {
        var object = factory.newInstance(HFW.UITextureFramesInfo.class);
        read(reader, factory, object);
        return object;
    }

    private static void read(BinaryReader reader, TypeFactory factory, HFW.UITextureFramesInfo object) throws IOException {
        object.data(reader.readBytes(reader.readInt()));
        object.spans(reader.readLongs(reader.readInt()));
        object.width(reader.readInt());
        object.height(reader.readInt());
        object.pixelFormat(HFW.EPixelFormat.valueOf(reader.readInt()));
        object.frequency(HFW.EUpdateFrequency.valueOf((byte) reader.readInt()));
        object.size(reader.readInt());
        object.scale(HFWTypeReader.readCompound(HFW.FSize.class, reader, factory));
    }
}
