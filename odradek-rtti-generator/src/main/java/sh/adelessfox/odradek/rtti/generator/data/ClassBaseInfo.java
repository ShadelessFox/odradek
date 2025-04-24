package sh.adelessfox.odradek.rtti.generator.data;

public record ClassBaseInfo(
    TypeInfoRef typeRef,
    int offset
) {
    public ClassTypeInfo type() {
        return (ClassTypeInfo) typeRef.value();
    }
}
