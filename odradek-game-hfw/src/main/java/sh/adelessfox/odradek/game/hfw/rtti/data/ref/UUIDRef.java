package sh.adelessfox.odradek.game.hfw.rtti.data.ref;

import sh.adelessfox.odradek.game.hfw.rtti.HorizonForbiddenWest.GGUUID;

@SuppressWarnings("unused")
public record UUIDRef<T>(GGUUID uuid) {
    @Override
    public String toString() {
        return "<uuid ref to " + uuid.toDisplayString() + ">";
    }
}
