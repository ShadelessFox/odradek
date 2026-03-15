package sh.adelessfox.odradek.game.hfw.rtti;

import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.hashing.HashFunction;
import sh.adelessfox.odradek.io.BinaryWriter;
import sh.adelessfox.odradek.io.BoolFormat;
import sh.adelessfox.odradek.rtti.AtomTypeInfo;
import sh.adelessfox.odradek.rtti.ContainerTypeInfo;
import sh.adelessfox.odradek.rtti.EnumTypeInfo;
import sh.adelessfox.odradek.rtti.PointerTypeInfo;
import sh.adelessfox.odradek.rtti.io.AbstractTypeWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class HFWTypeWriter extends AbstractTypeWriter {
    @Override
    protected void writeAtom(Object object, AtomTypeInfo info, BinaryWriter writer) throws IOException {
        getAtomWriter(info).write(writer, object);
    }

    @Override
    protected void writeContainer(Object object, ContainerTypeInfo info, BinaryWriter writer) throws IOException {
        switch (info.containerType()) {
            case "Array" -> {
                int length = info.length(object);
                writer.writeInt(length);
                for (int i = 0; i < length; i++) {
                    write(info.get(object, i), info.itemType(), writer);
                }
            }
            default -> throw new IllegalArgumentException("Unknown container type: " + info.containerType());
        }
    }

    @Override
    protected void writeEnum(Object object, EnumTypeInfo info, BinaryWriter writer) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    protected void writePointer(Object object, PointerTypeInfo info, BinaryWriter writer) throws IOException {
        throw new NotImplementedException();
    }

    @SuppressWarnings("unchecked")
    private static AtomWriter<Object> getAtomWriter(AtomTypeInfo info) {
        var name = info.base().name();
        var writer = switch (name) {
            case "bool" -> AtomWriter.BOOL;
            // case "wchar", "tchar" -> AtomWriter.CHAR;
            case "uint8", "int8" -> AtomWriter.BYTE;
            case "uint16", "int16" -> AtomWriter.SHORT;
            case "uint", "int", "uint32", "int32", "ucs4" -> AtomWriter.INT;
            case "uint64", "int64", "uintptr" -> AtomWriter.LONG;
            // case "HalfFloat" -> AtomWriter.HALF;
            case "float" -> AtomWriter.FLOAT;
            case "double" -> AtomWriter.DOUBLE;
            case "String" -> AtomWriter.STRING;
            // case "WString" -> AtomWriter.WSTRING;
            default -> throw new IllegalArgumentException("Unknown atom type: " + info.name() + " (" + name + ")");
        };
        return (AtomWriter<Object>) (Object) writer;
    }

    private interface AtomWriter<T> {
        AtomWriter<Boolean> BOOL = (w, o) -> w.writeBool(o, BoolFormat.BYTE);
        AtomWriter<Byte> BYTE = BinaryWriter::writeByte;
        AtomWriter<Short> SHORT = BinaryWriter::writeShort;
        AtomWriter<Integer> INT = BinaryWriter::writeInt;
        AtomWriter<Long> LONG = BinaryWriter::writeLong;
        AtomWriter<Float> FLOAT = BinaryWriter::writeFloat;
        AtomWriter<Double> DOUBLE = BinaryWriter::writeDouble;

        AtomWriter<String> STRING = (w, o) -> {
            var data = o.getBytes(StandardCharsets.UTF_8);
            w.writeInt(data.length);
            w.writeInt(HashFunction.crc32c().hash(data).asInt());
            w.writeBytes(data);
        };

        void write(BinaryWriter writer, T object) throws IOException;
    }
}
