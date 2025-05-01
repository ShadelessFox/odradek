package sh.adelessfox.odradek.game.kz4.rtti;

import sh.adelessfox.odradek.rtti.factory.TypeId;

public record Killzone4TypeId(String typeName) implements TypeId {
    public static TypeId of(String typeName) {
        return new Killzone4TypeId(typeName);
    }
}
