package sh.adelessfox.atrac9j;

/**
 * An Xorshift RNG used by the ATRAC9 codec
 */
final class Atrac9Rng {
    private short stateA;
    private short stateB;
    private short stateC;
    private short stateD;

    Atrac9Rng(short seed) {
        int startValue = 0x4D93 * (seed ^ (seed >>> 14));

        stateA = (short) (3 - startValue);
        stateB = (short) (2 - startValue);
        stateC = (short) (1 - startValue);
        stateD = (short) (-startValue);
    }

    short Next() {
        short t = (short) (stateD ^ (stateD << 5));
        stateD = stateC;
        stateC = stateB;
        stateB = stateA;
        stateA = (short) (t ^ stateA ^ ((t ^ (stateA >>> 5)) >>> 4));
        return stateA;
    }
}
