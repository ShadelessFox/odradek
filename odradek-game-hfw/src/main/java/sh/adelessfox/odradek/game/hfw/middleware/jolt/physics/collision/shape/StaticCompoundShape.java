package sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.collision.shape;

import sh.adelessfox.odradek.game.hfw.middleware.jolt.JoltUtils;
import wtf.reversed.toolbox.collect.Ints;
import wtf.reversed.toolbox.collect.Shorts;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;
import java.util.List;

public class StaticCompoundShape extends CompoundShape {
    public record Node(
        Shorts boundsMinX,
        Shorts boundsMinY,
        Shorts boundsMinZ,
        Shorts boundsMaxX,
        Shorts boundsMaxY,
        Shorts boundsMaxZ,
        Ints nodeProperties
    ) {
        private static Node read(BinarySource source) throws IOException {
            var boundsMinX = source.readShorts(4);
            var boundsMinY = source.readShorts(4);
            var boundsMinZ = source.readShorts(4);
            var boundsMaxX = source.readShorts(4);
            var boundsMaxY = source.readShorts(4);
            var boundsMaxZ = source.readShorts(4);
            var nodeProperties = source.readInts(4);

            return new Node(boundsMinX, boundsMinY, boundsMinZ, boundsMaxX, boundsMaxY, boundsMaxZ, nodeProperties);
        }
    }

    public List<Node> nodes;

    @Override
    public void restoreBinaryState(BinarySource source) throws IOException {
        super.restoreBinaryState(source);

        nodes = JoltUtils.readObjects(source, Node::read);
    }
}
