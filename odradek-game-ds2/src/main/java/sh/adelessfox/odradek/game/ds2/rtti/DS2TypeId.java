package sh.adelessfox.odradek.game.ds2.rtti;

import sh.adelessfox.odradek.rtti.factory.TypeId;

public record DS2TypeId(long hash) implements TypeId {
    public static TypeId of(long hash) {
        return new DS2TypeId(hash);
    }
}
