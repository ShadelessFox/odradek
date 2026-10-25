package sh.adelessfox.odradek.game.ds2.rtti.extensions;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;

// TODO: Introduce a mechanism for providing getters/setters for actual properties
public interface DrawFlagsExtension {
    default DS2.EShadowCastMode castShadows() {
        var flags = (DS2.DrawFlags) this;
        return DS2.EShadowCastMode.valueOf(flags.general().data() & 3);
    }

    default DS2.EShadowCull shadowCullMode() {
        var flags = (DS2.DrawFlags) this;
        return DS2.EShadowCull.valueOf((flags.general().data() >>> 2) & 3);
    }

    default DS2.EDrawPartType renderType() {
        var flags = (DS2.DrawFlags) this;
        return DS2.EDrawPartType.valueOf((flags.general().data() >>> 4) & 3);
    }

    default DS2.EViewLayer viewLayer() {
        var flags = (DS2.DrawFlags) this;
        return DS2.EViewLayer.valueOf((flags.general().data() >>> 6) & 3);
    }

    default float shadowBiasMultiplier() {
        var flags = (DS2.DrawFlags) this;
        return Float.float16ToFloat((short) (flags.general().data() >>> 8));
    }

    default DS2.EShadowBiasMode shadowBiasMode() {
        var flags = (DS2.DrawFlags) this;
        return DS2.EShadowBiasMode.valueOf((flags.general().data() >>> 24) & 1);
    }

    default boolean disableOcclusionCulling() {
        var flags = (DS2.DrawFlags) this;
        return ((flags.general().data() >>> 25) & 1) != 0;
    }

    default boolean disableDepthOnlyPass() {
        var flags = (DS2.DrawFlags) this;
        return ((flags.general().data() >>> 26) & 1) != 0;
    }

    default boolean castShadowsInSunCascadeCompartment0() {
        var flags = (DS2.DrawFlags) this;
        return ((flags.general().data() >>> 27) & 1) != 0;
    }

    default boolean castShadowsInSunCascade0() {
        var flags = (DS2.DrawFlags) this;
        return ((flags.general().data() >>> 28) & 1) != 0;
    }

    default boolean castShadowsInSunCascade1() {
        var flags = (DS2.DrawFlags) this;
        return ((flags.general().data() >>> 29) & 1) != 0;
    }

    default boolean castShadowsInSunCascade2() {
        var flags = (DS2.DrawFlags) this;
        return ((flags.general().data() >>> 30) & 1) != 0;
    }

    default boolean castShadowsInLocalLights() {
        var flags = (DS2.DrawFlags) this;
        return ((flags.general().data() >>> 31) & 1) != 0;
    }
}
