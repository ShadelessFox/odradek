/**
 * This module contains a minimal implementation of the JoltPhysics data structures.
 *
 * @see <a href="https://github.com/jrouwe/JoltPhysics">Jolt Physics</a>
 */
module odradek.middleware.jolt {
    requires odradek.core;

    exports sh.adelessfox.odradek.middleware.jolt.core;
    exports sh.adelessfox.odradek.middleware.jolt.geometry;
    exports sh.adelessfox.odradek.middleware.jolt.math;
    exports sh.adelessfox.odradek.middleware.jolt.physics.body;
    exports sh.adelessfox.odradek.middleware.jolt.physics.collision.shape;
    exports sh.adelessfox.odradek.middleware.jolt.physics.collision;
    exports sh.adelessfox.odradek.middleware.jolt.physics.constraints;
    exports sh.adelessfox.odradek.middleware.jolt.physics.ragdoll;
    exports sh.adelessfox.odradek.middleware.jolt.skeleton;
    exports sh.adelessfox.odradek.middleware.jolt;
}
