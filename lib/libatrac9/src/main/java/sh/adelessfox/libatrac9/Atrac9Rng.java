package sh.adelessfox.libatrac9;

/// <summary>
/// An Xorshift RNG used by the ATRAC9 codec
/// </summary>
final class Atrac9Rng {
    private short _stateA;
    private short _stateB;
    private short _stateC;
    private short _stateD;

    Atrac9Rng(short seed) {
        int startValue = 0x4D93 * (seed ^ (seed >>> 14));

        _stateA = (short) (3 - startValue);
        _stateB = (short) (2 - startValue);
        _stateC = (short) (1 - startValue);
        _stateD = (short) (0 - startValue);
    }

    short Next() {
        short t = (short) (_stateD ^ (_stateD << 5));
        _stateD = _stateC;
        _stateC = _stateB;
        _stateB = _stateA;
        _stateA = (short) (t ^ _stateA ^ ((t ^ (_stateA >>> 5)) >>> 4));
        return _stateA;
    }
}
