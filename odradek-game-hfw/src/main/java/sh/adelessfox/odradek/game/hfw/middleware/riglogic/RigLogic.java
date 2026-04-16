package sh.adelessfox.odradek.game.hfw.middleware.riglogic;

import sh.adelessfox.odradek.game.hfw.middleware.riglogic.animatedmaps.AnimatedMaps;
import sh.adelessfox.odradek.game.hfw.middleware.riglogic.blendshapes.BlendShapes;
import sh.adelessfox.odradek.game.hfw.middleware.riglogic.controls.Controls;
import sh.adelessfox.odradek.game.hfw.middleware.riglogic.joints.Joints;
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
            var joints = Joints.read(reader);
            var blendShapes = BlendShapes.read(reader);
            var animatedMaps = AnimatedMaps.read(reader);

            return new RigLogic(config, metrics, controls, joints, blendShapes, animatedMaps);
        } finally {
            reader.order(ByteOrder.LITTLE_ENDIAN);
        }
    }
}
