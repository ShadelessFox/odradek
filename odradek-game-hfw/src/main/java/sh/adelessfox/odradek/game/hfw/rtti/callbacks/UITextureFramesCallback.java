package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.*;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class UITextureFramesCallback implements ExtraBinaryDataCallback<UITextureFrames> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, UITextureFrames object) throws IOException {
        read(reader, factory, object);
    }

    static UITextureFramesInfo read(BinaryReader reader, TypeFactory factory) throws IOException {
        var object = factory.newInstance(UITextureFramesInfo.class);
        read(reader, factory, object);
        return object;
    }

    private static void read(BinaryReader reader, TypeFactory factory, UITextureFramesInfo object) throws IOException {
        object.data(reader.readBytes(reader.readInt()));
        object.spans(reader.readLongs(reader.readInt()));
        object.width(reader.readInt());
        object.height(reader.readInt());
        object.pixelFormat(EPixelFormat.valueOf(reader.readInt()));
        object.frequency(EUpdateFrequency.valueOf((byte) reader.readInt()));
        object.size(reader.readInt());
        object.scale(HFWTypeReader.readCompound(FSize.class, reader, factory));
    }
}
