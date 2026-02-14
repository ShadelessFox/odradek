package sh.adelessfox.odradek.game.untildawn.rtti;

import sh.adelessfox.odradek.rtti.factory.TypeId;

public record UntilDawnTypeId(String typeName) implements TypeId {
    public static TypeId of(String typeName) {
        return new UntilDawnTypeId(typeName);
    }
}
