package sh.adelessfox.odradek.game.ds2.rtti.data.ref;

import sh.adelessfox.odradek.game.ds2.rtti.DS2;

@SuppressWarnings("unused")
public record UUIDRef<T>(DS2.GGUUID uuid) {
    @Override
    public String toString() {
        return "<uuid ref to " + uuid.toDisplayString() + ">";
    }
}
