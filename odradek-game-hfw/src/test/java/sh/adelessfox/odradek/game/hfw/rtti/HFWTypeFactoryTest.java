package sh.adelessfox.odradek.game.hfw.rtti;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.factory.TypeFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Disabled("JUnit ignores JPSM and loads everything in an unnamed module, so the type factory fails to initialize")
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
        assertDoesNotThrow(() -> factory.newInstance(info));
    }

    @ParameterizedTest
    @MethodSource("getTypes")
    void typePassesEqualityChecks(String name) {
        var info = factory.get(name).asClass();
        var instance = factory.newInstance(info);

        EqualsVerifier.forClass(instance.getClass())
            .suppress(Warning.NONFINAL_FIELDS)
            .verify();
    }

    private static Stream<String> getTypes() {
        return factory.getAll().stream()
            .filter(ClassTypeInfo.class::isInstance)
            .map(TypeInfo::name);
    }
}
