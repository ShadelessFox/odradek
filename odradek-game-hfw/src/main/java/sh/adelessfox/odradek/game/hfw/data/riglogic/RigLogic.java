package sh.adelessfox.odradek.game.hfw.data.riglogic;

import sh.adelessfox.odradek.game.hfw.data.riglogic.animatedmaps.AnimatedMaps;
import sh.adelessfox.odradek.game.hfw.data.riglogic.blendshapes.BlendShapes;
import sh.adelessfox.odradek.game.hfw.data.riglogic.controls.Controls;
import sh.adelessfox.odradek.game.hfw.data.riglogic.joints.Joints;
import sh.adelessfox.odradek.io.BinaryReader;

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
