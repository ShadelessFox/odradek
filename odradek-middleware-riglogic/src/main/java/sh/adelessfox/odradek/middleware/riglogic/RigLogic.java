package sh.adelessfox.odradek.middleware.riglogic;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.middleware.riglogic.animatedmaps.AnimatedMaps;
import sh.adelessfox.odradek.middleware.riglogic.blendshapes.BlendShapes;
import sh.adelessfox.odradek.middleware.riglogic.controls.Controls;
import sh.adelessfox.odradek.middleware.riglogic.joints.Joints;

import java.io.IOException;

public record RigLogic(
    Configuration config,
    RigMetrics metrics,
    Controls controls,
    Joints joints,
    BlendShapes blendShapes,
    AnimatedMaps animatedMaps
) {
    public static RigLogic read(BinaryReader reader) throws IOException {
        return new RigLogic(
            Configuration.read(reader),
            RigMetrics.read(reader),
            Controls.read(reader),
            Joints.read(reader),
            BlendShapes.read(reader),
            AnimatedMaps.read(reader)
        );
    }
}
