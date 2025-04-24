package sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision.shape;

import sh.adelessfox.odradek.game.hfw.data.jolt.JoltUtils;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.util.List;

public class StaticCompoundShape extends CompoundShape {
    public record Node(
        short[] boundsMinX,
        short[] boundsMinY,
        short[] boundsMinZ,
        short[] boundsMaxX,
        short[] boundsMaxY,
        short[] boundsMaxZ,
        int[] nodeProperties
    ) {
        private static Node read(BinaryReader reader) throws IOException {
            var boundsMinX = reader.readShorts(4);
            var boundsMinY = reader.readShorts(4);
            var boundsMinZ = reader.readShorts(4);
            var boundsMaxX = reader.readShorts(4);
            var boundsMaxY = reader.readShorts(4);
            var boundsMaxZ = reader.readShorts(4);
            var nodeProperties = reader.readInts(4);

            return new Node(boundsMinX, boundsMinY, boundsMinZ, boundsMaxX, boundsMaxY, boundsMaxZ, nodeProperties);
        }
    }

    public List<Node> nodes;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        nodes = JoltUtils.readObjects(reader, Node::read);
    }
}
