package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.*;
import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.io.IOException;

public class TextureCallback implements ExtraBinaryDataCallback<Texture> {
    @Override
    public void deserialize(BinaryReader reader, TypeFactory factory, Texture object) throws IOException {
        read(reader, factory, object);
    }

    static TextureInfo read(BinaryReader reader, TypeFactory factory) throws IOException {
        var object = factory.newInstance(TextureInfo.class);
        read(reader, factory, object);
        return object;
    }

    private static void read(BinaryReader reader, TypeFactory factory, TextureInfo object) throws IOException {
        object.header(readHeader(reader, factory));
        object.data(readData(reader, factory));
    }

    private static TextureHeader readHeader(BinaryReader reader, TypeFactory factory) throws IOException {
        var header = factory.newInstance(TextureHeader.class);
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

    private static TextureData readData(BinaryReader reader, TypeFactory factory) throws IOException {
        var data = factory.newInstance(TextureData.class);
        data.totalSize(reader.readInt());
        data.embeddedSize(reader.readInt());
        data.streamedSize(reader.readInt());
        data.streamedMips(reader.readInt());
        data.embeddedData(reader.readBytes(data.totalSize() - 12));
        return data;
    }
}
