package sh.adelessfox.odradek.game.ds2.middleware.riglogic;

import sh.adelessfox.odradek.game.ds2.middleware.riglogic.animatedmaps.AnimatedMaps;
import sh.adelessfox.odradek.game.ds2.middleware.riglogic.blendshapes.BlendShapes;
import sh.adelessfox.odradek.game.ds2.middleware.riglogic.controls.Controls;
import sh.adelessfox.odradek.game.ds2.middleware.riglogic.joints.Joints;
import wtf.reversed.toolbox.io.BinarySource;

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
    public static RigLogic read(BinarySource source) throws IOException {
        source.order(ByteOrder.BIG_ENDIAN);
        try {
            var config = Configuration.read(source);
            var metrics = RigMetrics.read(source);
            var controls = Controls.read(source);
            var joints = Joints.read(config, source);
            var blendShapes = config.loadBlendShapes() ? BlendShapes.read(source) : null;
            var animatedMaps = config.loadAnimatedMaps() ? AnimatedMaps.read(source) : null;

            return new RigLogic(config, metrics, controls, joints, blendShapes, animatedMaps);
        } finally {
            source.order(ByteOrder.LITTLE_ENDIAN);
        }
    }
}
