package sh.adelessfox.odradek.game.ds2.rtti.data;

import wtf.reversed.toolbox.collect.Floats;

public record MotionMatchingVecN(Floats data) {
    public MotionMatchingVecN {
        if (data.length() != 72) {
            throw new IllegalArgumentException("MotionMatchingVecN must have exactly 72 floats, got " + data.length());
        }
    }
}
