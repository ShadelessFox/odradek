package sh.adelessfox.odradek.game.ds2.middleware.riglogic;

import sh.adelessfox.odradek.game.ds2.middleware.riglogic.animatedmaps.AnimatedMaps;
import sh.adelessfox.odradek.game.ds2.middleware.riglogic.blendshapes.BlendShapes;
import sh.adelessfox.odradek.game.ds2.middleware.riglogic.controls.Controls;
import sh.adelessfox.odradek.game.ds2.middleware.riglogic.joints.Joints;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.nio.ByteOrder;

public record RigLogic(
    Configuration config,
    RigMetrics metrics,
    Controls controls,
    Joints joints,
    BlendShapes blendShapes,
    AnimatedMaps animatedMaps
) {
    public static RigLogic read(BinaryReader reader) throws IOException {
        reader.order(ByteOrder.BIG_ENDIAN);
        try {
            var config = Configuration.read(reader);
            var metrics = RigMetrics.read(reader);
            var controls = Controls.read(reader);
            var joints = Joints.read(config, reader);
            var blendShapes = config.loadBlendShapes() ? BlendShapes.read(reader) : null;
            var animatedMaps = config.loadAnimatedMaps() ? AnimatedMaps.read(reader) : null;

            return new RigLogic(config, metrics, controls, joints, blendShapes, animatedMaps);
        } finally {
            reader.order(ByteOrder.LITTLE_ENDIAN);
        }
    }
}
