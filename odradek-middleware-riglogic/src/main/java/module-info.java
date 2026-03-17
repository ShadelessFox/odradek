/**
 * This module contains a minimal implementation of the RigLogic data structures.
 *
 * @see <a href="https://docs.unrealengine.com/4.27/en-US/API/Plugins/RigLogicLib">Unreal Engine RigLogicLib</a>
 */
module odradek.middleware.riglogic {
    requires odradek.core;

    exports sh.adelessfox.odradek.middleware.riglogic.animatedmaps;
    exports sh.adelessfox.odradek.middleware.riglogic.blendshapes;
    exports sh.adelessfox.odradek.middleware.riglogic.conditionaltable;
    exports sh.adelessfox.odradek.middleware.riglogic.controls;
    exports sh.adelessfox.odradek.middleware.riglogic.joints.bpcm;
    exports sh.adelessfox.odradek.middleware.riglogic.joints;
    exports sh.adelessfox.odradek.middleware.riglogic.psdmatrix;
    exports sh.adelessfox.odradek.middleware.riglogic;
}
