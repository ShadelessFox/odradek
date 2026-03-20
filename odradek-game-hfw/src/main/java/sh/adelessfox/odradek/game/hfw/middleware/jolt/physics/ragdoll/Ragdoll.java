package sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.ragdoll;

import sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.body.BodyCreationSettings;
import sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.constraints.TwoBodyConstraintSettings;

public class Ragdoll {
    public static class Part extends BodyCreationSettings {
        public TwoBodyConstraintSettings toParent;
    }
}
