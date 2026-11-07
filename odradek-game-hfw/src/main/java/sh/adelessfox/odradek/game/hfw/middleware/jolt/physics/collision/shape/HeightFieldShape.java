package sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.collision.shape;

import sh.adelessfox.odradek.game.hfw.middleware.jolt.JoltUtils;
import sh.adelessfox.odradek.game.hfw.middleware.jolt.math.Vec3;
import sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.collision.PhysicsMaterial;
import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.collect.Shorts;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;
import java.util.List;

public class HeightFieldShape extends Shape {
    public record RangeBlock(Shorts min, Shorts max) {
        private static RangeBlock read(BinarySource source) throws IOException {
            var min = source.readShorts(4);
            var max = source.readShorts(4);

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
    public Bytes heightSamples;
    public Bytes activeEdges;
    public Bytes materialIndices;
    public int numBitsPerMaterialIndex;
    public PhysicsMaterial[] materials;

    @Override
    public void restoreBinaryState(BinarySource source) throws IOException {
        super.restoreBinaryState(source);

        offset = JoltUtils.readAlignedVector3(source);
        scale = JoltUtils.readAlignedVector3(source);
        sampleCount = source.readInt();
        blockSize = source.readInt();
        bitsPerSample = source.readByte();
        minSample = source.readShort();
        maxSample = source.readShort();
        rangeBlocks = JoltUtils.readObjects(source, RangeBlock::read);
        heightSamples = JoltUtils.readBytes(source);
        activeEdges = JoltUtils.readBytes(source);
        materialIndices = JoltUtils.readBytes(source);
        numBitsPerMaterialIndex = source.readInt();
    }

    @Override
    public void restoreMaterialState(PhysicsMaterial[] materials) {
        this.materials = materials;
    }
}
