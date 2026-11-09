package sh.adelessfox.odradek.game.hfw.rtti.callbacks;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.HFWTypeReader;
import sh.adelessfox.odradek.rtti.data.ExtraBinaryDataCallback;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class TextureCallback implements ExtraBinaryDataCallback<HFW.Texture> {
    @Override
    public void deserialize(BinarySource source, TypeFactory factory, HFW.Texture object) throws IOException {
        read(source, factory, object);
    }

    static HFW.TextureInfo read(BinarySource source, TypeFactory factory) throws IOException {
        var object = factory.newInstance(HFW.TextureInfo.class);
        read(source, factory, object);
        return object;
    }

    private static void read(BinarySource source, TypeFactory factory, HFW.TextureInfo object) throws IOException {
        object.header(readHeader(source, factory));
        object.data(readData(source, factory));
    }

    private static HFW.TextureHeader readHeader(BinarySource source, TypeFactory factory) throws IOException {
        var header = factory.newInstance(HFW.TextureHeader.class);
        header.type(HFW.ETextureType.valueOf(source.readShort()));
        header.width(source.readShort());
        header.height(source.readShort());
        header.numSurfaces(source.readShort());
        header.numMips(source.readByte());
        header.pixelFormat(HFW.EPixelFormat.valueOf(source.readByte()));
        header.texCoordType(HFW.ETexCoordType.valueOf(source.readByte()));
        header.colorSpace(HFW.ETexColorSpace.valueOf(source.readByte()));
        header.flags(source.readByte());
        header.creationFlags(source.readByte());
        header.streamingMipCount(source.readByte());
        header.highMipSkipCount(source.readByte());
        header.hash(HFWTypeReader.readCompound(HFW.MurmurHashValue.class, source, factory));
        return header;
    }

    private static HFW.TextureData readData(BinarySource source, TypeFactory factory) throws IOException {
        var data = factory.newInstance(HFW.TextureData.class);
        data.totalSize(source.readInt());
        data.embeddedSize(source.readInt());
        data.streamedSize(source.readInt());
        data.streamedMips(source.readInt());
        data.embeddedData(source.readBytes(data.totalSize() - 12));
        return data;
    }
}
