package sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.shape;

import sh.adelessfox.odradek.game.hfw.data.jolt.JoltUtils;
import sh.adelessfox.odradek.game.hfw.data.jolt.math.Vec3;
import sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.PhysicsMaterial;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.util.List;

public class HeightFieldShape extends Shape {
    public record RangeBlock(short[] min, short[] max) {
        private static RangeBlock read(BinaryReader reader) throws IOException {
            var min = reader.readShorts(4);
            var max = reader.readShorts(4);

            return new RangeBlock(min, max);
        }
    }

    public Vec3 offset;
    public Vec3 scale;
    public int sampleCount;
    public int blockSize;
    public byte bitsPerSample;
    public short minSample;
    public short maxSample;
    public List<RangeBlock> rangeBlocks;
    public byte[] heightSamples;
    public byte[] activeEdges;
    public byte[] materialIndices;
    public int numBitsPerMaterialIndex;
    public PhysicsMaterial[] materials;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        offset = JoltUtils.readAlignedVector3(reader);
        scale = JoltUtils.readAlignedVector3(reader);
        sampleCount = reader.readInt();
        blockSize = reader.readInt();
        bitsPerSample = reader.readByte();
        minSample = reader.readShort();
        maxSample = reader.readShort();
        rangeBlocks = JoltUtils.readObjects(reader, RangeBlock::read);
        heightSamples = JoltUtils.readBytes(reader);
        activeEdges = JoltUtils.readBytes(reader);
        materialIndices = JoltUtils.readBytes(reader);
        numBitsPerMaterialIndex = reader.readInt();
    }

    @Override
    public void restoreMaterialState(PhysicsMaterial[] materials) {
        this.materials = materials;
    }
}
