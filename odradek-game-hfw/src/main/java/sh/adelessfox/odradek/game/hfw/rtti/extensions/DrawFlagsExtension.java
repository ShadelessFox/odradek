package sh.adelessfox.odradek.game.hfw.rtti.extensions;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;
import sh.adelessfox.odradek.game.hfw.rtti.HFW.*;

// TODO: Introduce a mechanism for providing getters/setters for actual properties
public interface DrawFlagsExtension {
    default EShadowCastMode castShadows() {
        var flags = (HFW.DrawFlags) this;
        return EShadowCastMode.valueOf(flags.general().data() & 3);
    }

    default EShadowCull shadowCullMode() {
        var flags = (HFW.DrawFlags) this;
        return EShadowCull.valueOf((flags.general().data() >>> 2) & 3);
    }

    default EDrawPartType renderType() {
        var flags = (HFW.DrawFlags) this;
        return EDrawPartType.valueOf((flags.general().data() >>> 4) & 3);
    }

    default EViewLayer viewLayer() {
        var flags = (HFW.DrawFlags) this;
        return EViewLayer.valueOf((flags.general().data() >>> 6) & 3);
    }

    default float shadowBiasMultiplier() {
        var flags = (HFW.DrawFlags) this;
        return Float.float16ToFloat((short) (flags.general().data() >>> 8));
    }

    default EShadowBiasMode shadowBiasMode() {
        var flags = (HFW.DrawFlags) this;
        return EShadowBiasMode.valueOf((flags.general().data() >>> 24) & 1);
    }

    default boolean disableOcclusionCulling() {
        var flags = (HFW.DrawFlags) this;
        return ((flags.general().data() >>> 25) & 1) != 0;
    }

    default boolean disableDepthOnlyPass() {
        var flags = (HFW.DrawFlags) this;
        return ((flags.general().data() >>> 26) & 1) != 0;
    }

    default boolean castShadowsInSunCascade0() {
        var flags = (HFW.DrawFlags) this;
        return ((flags.general().data() >>> 28) & 1) != 0;
    }

    default boolean castShadowsInSunCascade1() {
        var flags = (HFW.DrawFlags) this;
        return ((flags.general().data() >>> 29) & 1) != 0;
    }
}
