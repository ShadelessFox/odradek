package sh.adelessfox.odradek.game.hfw.rtti;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;
import wtf.reversed.toolbox.collect.*;

import java.util.BitSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class HFWTypeFactoryTest {
    private static TypeFactory factory;

    @BeforeAll
    static void beforeAll() {
        factory = new HFWTypeFactory();
    }

    @ParameterizedTest
    @MethodSource("getTypes")
    void typeCanBeResolvedFromName(String name) {
        assertDoesNotThrow(() -> factory.get(name));
    }

    @ParameterizedTest
    @MethodSource("getTypes")
    void typeCanBeInstantiated(String name) {
        var info = factory.get(name).asClass();
        assertDoesNotThrow(info::newInstance);
    }

    @ParameterizedTest
    @MethodSource("getTypes")
    void typePassesEqualityChecks(String name) {
        var info = factory.get(name).asClass();
        var instance = info.newInstance();

        EqualsVerifier.forClass(instance.getClass())
            .suppress(Warning.NONFINAL_FIELDS)
            .withPrefabValues(Bytes.class, Bytes.wrap(new byte[]{1}), Bytes.wrap(new byte[]{2}))
            .withPrefabValues(Shorts.class, Shorts.copyOf(new short[]{1}), Shorts.copyOf(new short[]{2}))
            .withPrefabValues(Ints.class, Ints.copyOf(new int[]{1}), Ints.copyOf(new int[]{2}))
            .withPrefabValues(Longs.class, Longs.copyOf(new long[]{1}), Longs.copyOf(new long[]{2}))
            .withPrefabValues(Floats.class, Floats.copyOf(new float[]{1}), Floats.copyOf(new float[]{2}))
            .withPrefabValues(Doubles.class, Doubles.copyOf(new double[]{1}), Doubles.copyOf(new double[]{2}))
            .withPrefabValues(BitSet.class, BitSet.valueOf(new long[]{1}), BitSet.valueOf(new long[]{2}))
            .verify();
    }

    private static Stream<String> getTypes() {
        return factory.getAll().stream()
            .filter(ClassTypeInfo.class::isInstance)
            .map(TypeInfo::name);
    }
}
