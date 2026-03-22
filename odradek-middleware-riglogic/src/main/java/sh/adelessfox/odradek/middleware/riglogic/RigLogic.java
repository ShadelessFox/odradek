package sh.adelessfox.odradek.middleware.riglogic;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.middleware.riglogic.animatedmaps.AnimatedMaps;
import sh.adelessfox.odradek.middleware.riglogic.blendshapes.BlendShapes;
import sh.adelessfox.odradek.middleware.riglogic.controls.Controls;
import sh.adelessfox.odradek.middleware.riglogic.joints.Joints;

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

        var config = Configuration.read(reader);
        var metrics = RigMetrics.read(reader);
        var controls = Controls.read(reader);
        var joints = Joints.read(config, reader);
        var blendShapes = config.loadBlendShapes() ? BlendShapes.read(reader) : null;
        var animatedMaps = config.loadAnimatedMaps() ? AnimatedMaps.read(reader) : null;

        reader.order(ByteOrder.LITTLE_ENDIAN);

        return new RigLogic(config, metrics, controls, joints, blendShapes, animatedMaps);
    }
}
