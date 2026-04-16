package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.game.ds2.rtti.DS2TypeReader;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public final class TextureCallback implements ExtraBinaryDataCallback<DS2.Texture> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, DS2.Texture object) throws IOException {
        read(reader, factory, object);
    }

    static DS2.TextureInfo read(BinaryReader reader, TypeFactory factory) throws IOException {
        var object = factory.newInstance(DS2.TextureInfo.class);
        read(reader, factory, object);
        return object;
    }

    private static void read(BinaryReader reader, TypeFactory factory, DS2.TextureInfo object) throws IOException {
        object.header(readHeader(reader, factory));
        object.data(readData(reader, factory));
    }

    private static DS2.TextureHeader readHeader(BinaryReader reader, TypeFactory factory) throws IOException {
        var header = factory.newInstance(DS2.TextureHeader.class);
        header.type(DS2.ETextureType.valueOf(reader.readShort()));
        header.width(reader.readShort());
        header.height(reader.readShort());
        header.numSurfaces(reader.readShort());
        header.numMips(reader.readByte());
        header.pixelFormat(DS2.EPixelFormat.valueOf(reader.readByte()));
        header.unk0A(reader.readByte());
        header.colorSpace(DS2.ETexColorSpace.valueOf(reader.readByte()));
        header.unk0C(reader.readByte());
        header.unk0D(reader.readByte());
        header.unk0E(reader.readByte());
        header.unk0F(reader.readByte());
        header.hash(DS2TypeReader.readCompound(DS2.MurmurHashValue.class, reader, factory));
        return header;
    }

    private static DS2.TextureData readData(BinaryReader reader, TypeFactory factory) throws IOException {
        var data = factory.newInstance(DS2.TextureData.class);
        data.totalSize(reader.readInt());
        data.embeddedSize(reader.readInt());
        data.streamedSize(reader.readInt());
        data.streamedMips(reader.readInt());
        data.embeddedData(reader.readBytes(data.totalSize() - 12));
        return data;
    }
}
