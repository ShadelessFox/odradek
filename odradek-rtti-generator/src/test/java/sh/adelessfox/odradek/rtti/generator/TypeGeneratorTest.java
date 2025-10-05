// package sh.adelessfox.odradek.rtti.generator;
//
// import org.junit.jupiter.api.Test;
// import sh.adelessfox.odradek.rtti.generator.data.AtomTypeInfo;
// import sh.adelessfox.odradek.rtti.generator.data.ContainerTypeInfo;
// import sh.adelessfox.odradek.rtti.generator.data.PointerTypeInfo;
//
// import java.util.Optional;
//
// import static org.junit.jupiter.api.Assertions.assertEquals;
//
// class TypeGeneratorTest {
//     private static final AtomTypeInfo uint8 = new AtomTypeInfo("uint8", Optional.empty());
//     private static final ContainerTypeInfo array_uint8 = new ContainerTypeInfo("Array<uint8>", uint8.ref());
//     private static final PointerTypeInfo cptr_uint8 = new PointerTypeInfo("cptr<uint8>", uint8.ref());
//
//     @Test
//     void generateAtomType() {
//         var generated = new TypeGenerator().generate(uint8);
//         assertEquals(Optional.empty(), generated, "Atom types don't have compiled presentation");
//     }
//
//     @Test
//     void generateContainerType() {
//         var generated = new TypeGenerator().generate(array_uint8);
//         assertEquals(Optional.empty(), generated, "Container types don't have compiled presentation");
//     }
//
//     @Test
//     void generatePointerType() {
//         var generated = new TypeGenerator().generate(cptr_uint8);
//         assertEquals(Optional.empty(), generated, "Pointer types don't have compiled presentation");
//     }
// }
