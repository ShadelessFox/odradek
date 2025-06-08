package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EPixelFormat;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EUpdateFrequency;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.FSize;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.Attr;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class UITextureFramesCallback implements ExtraBinaryDataCallback<UITextureFramesCallback.UITextureFramesData> {
    public interface UITextureFramesData {
        /** LZ4-compressed frame data; split into chunks defined by {@link #spans()}. */
        @Attr(name = "Data", type = "Array<uint8>", position = 0)
        byte[] data();

        void data(byte[] value);

        /** {@code (u32 length; u32 offset)} pairs that define frame chunks in {@link #data()}. */
        @Attr(name = "Spans", type = "Array<uint64>", position = 1)
        long[] spans();

        void spans(long[] value);

        @Attr(name = "Width", type = "uint32", position = 2)
        int width();

        void width(int value);

        @Attr(name = "Height", type = "uint32", position = 3)
        int height();

        void height(int value);

        @Attr(name = "PixelFormat", type = "EPixelFormat", position = 4)
        EPixelFormat pixelFormat();

        void pixelFormat(EPixelFormat value);

        @Attr(name = "Frequency", type = "EUpdateFrequency", position = 5)
        EUpdateFrequency frequency();

        void frequency(EUpdateFrequency value);

        /** Allocation size of a single frame; dimensions are aligned for compressed textures */
        @Attr(name = "Size", type = "uint32", position = 6)
        int size();

        void size(int value);

        @Attr(name = "Scale", type = "FSize", position = 7)
        FSize scale();

        void scale(FSize value);

        static UITextureFramesData read(BinaryReader reader, TypeFactory factory) throws IOException {
            var data = factory.newInstance(UITextureFramesData.class);
            new UITextureFramesCallback().deserialize(reader, factory, data);
            return data;
        }
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, UITextureFramesData object) throws IOException {
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
