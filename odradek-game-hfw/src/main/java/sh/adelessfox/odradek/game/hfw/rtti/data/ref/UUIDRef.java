package sh.adelessfox.odradek.game.hfw.rtti.data.ref;

import sh.adelessfox.odradek.game.hfw.rtti.HFW;

@SuppressWarnings("unused")
public record UUIDRef<T>(HFW.GGUUID uuid) {
    @Override
    public String toString() {
        return "<uuid ref to " + uuid.toDisplayString() + ">";
    }
}
