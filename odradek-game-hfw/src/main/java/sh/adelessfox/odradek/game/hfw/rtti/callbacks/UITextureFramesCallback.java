package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class UITextureFramesCallback implements ExtraBinaryDataCallback<HFW.UITextureFrames> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, HFW.UITextureFrames object) throws IOException {
        read(source, factory, object);
    }

    static HFW.UITextureFramesInfo read(BinarySource source, TypeFactory factory) throws IOException {
        var object = factory.newInstance(HFW.UITextureFramesInfo.class);
        read(source, factory, object);
        return object;
    }

    private static void read(BinarySource source, TypeFactory factory, HFW.UITextureFramesInfo object) throws IOException {
        object.data(source.readBytes(source.readInt()));
        object.spans(source.readLongs(source.readInt()));
        object.width(source.readInt());
        object.height(source.readInt());
        object.pixelFormat(HFW.EPixelFormat.valueOf(source.readInt()));
        object.frequency(HFW.EUpdateFrequency.valueOf((byte) source.readInt()));
        object.size(source.readInt());
        object.scale(HFWTypeReader.readCompound(HFW.FSize.class, source, factory));
    }
}
