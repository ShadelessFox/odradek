package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.EPixelFormat;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ETexColorSpace;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.ETextureType;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.MurmurHashValue;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.Attr;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class TextureCallback implements ExtraBinaryDataCallback<TextureCallback.TextureData> {
    public interface HwTextureHeader {
        @Attr(name = "Type", type = "ETextureType", position = 0, offset = 0)
        ETextureType type();

        void type(ETextureType value);

        @Attr(name = "Width", type = "uint16", position = 1, offset = 2)
        short width();

        void width(short value);

        @Attr(name = "Height", type = "uint16", position = 2, offset = 4)
        short height();

        void height(short value);

        @Attr(name = "NumSurfaces", type = "uint16", position = 3, offset = 6)
        short numSurfaces();

        void numSurfaces(short value);

        @Attr(name = "NumMips", type = "uint8", position = 4, offset = 8)
        byte numMips();

        void numMips(byte value);

        @Attr(name = "PixelFormat", type = "EPixelFormat", position = 5, offset = 9)
        EPixelFormat pixelFormat();

        void pixelFormat(EPixelFormat value);

        @Attr(name = "Unk0A", type = "uint8", position = 6, offset = 10)
        byte unk0A();

        void unk0A(byte value);

        @Attr(name = "ColorSpace", type = "ETexColorSpace", position = 7, offset = 11)
        ETexColorSpace colorSpace();

        void colorSpace(ETexColorSpace value);

        @Attr(name = "Unk0C", type = "uint8", position = 8, offset = 12)
        byte unk0C();

        void unk0C(byte value);

        @Attr(name = "Unk0D", type = "uint8", position = 9, offset = 13)
        byte unk0D();

        void unk0D(byte value);

        @Attr(name = "Unk0E", type = "uint8", position = 10, offset = 14)
        byte unk0E();

        void unk0E(byte value);

        @Attr(name = "Unk0F", type = "uint8", position = 11, offset = 15)
        byte unk0F();

        void unk0F(byte value);

        @Attr(name = "Hash", type = "MurmurHashValue", position = 12, offset = 0)
        MurmurHashValue hash();

        void hash(MurmurHashValue value);

        static HwTextureHeader read(BinaryReader reader, TypeFactory factory) throws IOException {
            var header = factory.newInstance(HwTextureHeader.class);
            header.type(ETextureType.valueOf(reader.readShort()));
            header.width(reader.readShort());
            header.height(reader.readShort());
            header.numSurfaces(reader.readShort());
            header.numMips(reader.readByte());
            header.pixelFormat(EPixelFormat.valueOf(reader.readByte()));
            header.unk0A(reader.readByte());
            header.colorSpace(ETexColorSpace.valueOf(reader.readByte()));
            header.unk0C(reader.readByte());
            header.unk0D(reader.readByte());
            header.unk0E(reader.readByte());
            header.unk0F(reader.readByte());
            header.hash(HFWTypeReader.readCompound(MurmurHashValue.class, reader, factory));
            return header;
        }
    }

    public interface HwTextureData {
        @Attr(name = "TotalSize", type = "uint32", position = 0, offset = 0)
        int totalSize();

        void totalSize(int value);

        @Attr(name = "EmbeddedSize", type = "uint32", position = 1, offset = 0)
        int embeddedSize();

        void embeddedSize(int value);

        @Attr(name = "StreamedSize", type = "uint32", position = 2, offset = 0)
        int streamedSize();

        void streamedSize(int value);

        @Attr(name = "StreamedMips", type = "uint32", position = 3, offset = 0)
        int streamedMips();

        void streamedMips(int value);

        @Attr(name = "EmbeddedData", type = "Array<uint8>", position = 4, offset = 0)
        byte[] embeddedData();

        void embeddedData(byte[] value);

        static HwTextureData read(BinaryReader reader, TypeFactory factory) throws IOException {
            var data = factory.newInstance(HwTextureData.class);
            data.totalSize(reader.readInt());
            data.embeddedSize(reader.readInt());
            data.streamedSize(reader.readInt());
            data.streamedMips(reader.readInt());
            data.embeddedData(reader.readBytes(data.totalSize() - 12));
            return data;
        }
    }

    public interface TextureData {
        @Attr(name = "Header", type = "HwTextureHeader", position = 0, offset = 0)
        HwTextureHeader header();

        void header(HwTextureHeader value);

        @Attr(name = "Data", type = "HwTextureData", position = 1, offset = 0)
        HwTextureData data();

        void data(HwTextureData value);

        static TextureData read(BinaryReader reader, TypeFactory factory) throws IOException {
            var data = factory.newInstance(TextureData.class);
            new TextureCallback().deserialize(reader, factory, data);
            return data;
        }
    }

    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, TextureData object) throws IOException {
        object.header(HwTextureHeader.read(reader, factory));
        object.data(HwTextureData.read(reader, factory));
    }
}
