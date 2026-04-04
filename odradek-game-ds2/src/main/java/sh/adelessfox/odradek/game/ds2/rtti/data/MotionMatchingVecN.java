package sh.adelessfox.odradek.game.ds2.rtti.data;

import java.util.Arrays;

public record MotionMatchingVecN(float[] data) {
    public MotionMatchingVecN {
        if (data.length != 72) {
            throw new IllegalArgumentException("MotionMatchingVecN must have exactly 72 floats, got " + data.length);
        }
        data = Arrays.copyOf(data, 72);
    }
}
