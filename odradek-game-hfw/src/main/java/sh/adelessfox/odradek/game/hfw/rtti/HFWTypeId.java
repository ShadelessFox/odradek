package sh.adelessfox.odradek.game.hfw.rtti;

import sh.adelessfox.odradek.rtti.factory.TypeId;

public record HFWTypeId(long hash) implements TypeId {
    public static HFWTypeId of(long hash) {
        return new HFWTypeId(hash);
    }
}
