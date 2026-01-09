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
        int startValue = 0x4D93 * (seed & 0xffff ^ (seed & 0xffff) >>> 14);

        stateA = (short) (3 - startValue);
        stateB = (short) (2 - startValue);
        stateC = (short) (1 - startValue);
        stateD = (short) /**/-startValue;
    }

    short Next() {
        int t = (short) (stateD & 0xffff ^ (stateD & 0xffff) << 5) & 0xffff;
        stateD = stateC;
        stateC = stateB;
        stateB = stateA;
        stateA = (short) (stateA & 0xffff ^ ((stateA & 0xffff) >>> 5 ^ t) >>> 4 ^ t);
        return stateA;
    }
}
