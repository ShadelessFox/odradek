package sh.adelessfox.odradek.game.ds2.rtti.callbacks;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;
import sh.adelessfox.odradek.game.ds2.rtti.DS2TypeReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public final class TextureCallback implements ExtraBinaryDataCallback<DS2.Texture> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, DS2.Texture object) throws IOException {
        read(source, factory, object);
    }

    static DS2.TextureInfo read(BinarySource source, TypeFactory factory) throws IOException {
        var object = factory.newInstance(DS2.TextureInfo.class);
        read(source, factory, object);
        return object;
    }

    private static void read(BinarySource source, TypeFactory factory, DS2.TextureInfo object) throws IOException {
        object.header(readHeader(source, factory));
        object.data(readData(source, factory));
    }

    private static DS2.TextureHeader readHeader(BinarySource source, TypeFactory factory) throws IOException {
        var header = factory.newInstance(DS2.TextureHeader.class);
        header.type(DS2.ETextureType.valueOf(source.readShort()));
        header.width(source.readShort());
        header.height(source.readShort());
        header.numSurfaces(source.readShort());
        header.numMips(source.readByte());
        header.pixelFormat(DS2.EPixelFormat.valueOf(source.readByte()));
        header.unk0A(source.readByte());
        header.colorSpace(DS2.ETexColorSpace.valueOf(source.readByte()));
        header.unk0C(source.readByte());
        header.unk0D(source.readByte());
        header.unk0E(source.readByte());
        header.unk0F(source.readByte());
        header.hash(DS2TypeReader.readCompound(DS2.MurmurHashValue.class, source, factory));
        return header;
    }

    private static DS2.TextureData readData(BinarySource source, TypeFactory factory) throws IOException {
        var data = factory.newInstance(DS2.TextureData.class);
        data.totalSize(source.readInt());
        data.embeddedSize(source.readInt());
        data.streamedSize(source.readInt());
        data.streamedMips(source.readInt());
        data.embeddedData(source.readBytes(data.totalSize() - 12));
        return data;
    }
}
