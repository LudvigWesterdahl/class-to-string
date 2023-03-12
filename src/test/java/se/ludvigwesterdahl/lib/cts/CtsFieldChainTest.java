package se.ludvigwesterdahl.lib.cts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

final class CtsFieldChainTest {

    @Test
    void Should_ReturnEqualObject_When_NewRootInstance() {
        final CtsFieldChain expected = CtsFieldChain.newRootInstance(String.class);

        final CtsFieldChain actual = CtsFieldChain.newRootInstance(String.class);

        assertThat(actual)
                .isEqualTo(expected)
                .hasSameHashCodeAs(expected)
                .hasToString(expected.toString());
    }

    @Test
    void Should_ThrowNpe_When_NewRootInstance() {
        assertThatCode(() -> CtsFieldChain.newRootInstance(null))
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void Should_ReturnRootField_When_Head() {
        final CtsFieldChain fieldChain = CtsFieldChain.newRootInstance(String.class);
        final CtsField expected = CtsField.newNode(Identifier.newInstance(String.class), 0);

        final CtsField actual = fieldChain.head();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void Should_ReturnField_When_Head() {
        final CtsFieldChain fieldChain = CtsFieldChain.newRootInstance(String.class)
                .chainAll(List.of(CtsField.newLeaf(Identifier.newInstance(Object.class, "leaf"), Modifier.PRIVATE)))
                .get(0);
        final CtsField expected = CtsField.newLeaf(Identifier.newInstance(Object.class, "leaf"), Modifier.PRIVATE);

        final CtsField actual = fieldChain.head();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void Should_ReturnTrue_When_IsRoot() {
        final CtsFieldChain fieldChain = CtsFieldChain.newRootInstance(String.class);

        final boolean actual = fieldChain.isRoot();

        assertThat(actual).isTrue();
    }

    @Test
    void Should_ReturnFalse_When_IsRoot() {
        final CtsFieldChain fieldChain = CtsFieldChain.newRootInstance(String.class)
                .chainAll(List.of(CtsField.newLeaf(Identifier.newInstance(Object.class, "leaf"), 0)))
                .get(0);

        final boolean actual = fieldChain.isRoot();

        assertThat(actual).isFalse();
    }

    @Test
    void Should_ReturnFields_When_AllFields() {
        final CtsFieldChain fieldChain = CtsFieldChain.newRootInstance(String.class)
                .chainAll(List.of(CtsField.newNode(Identifier.newInstance(Stream.class, "node"), Modifier.PRIVATE)))
                .get(0)
                .chainAll(List.of(CtsField.newLeaf(Identifier.newInstance(String.class, "leaf"), Modifier.PRIVATE)))
                .get(0);
        final List<CtsField> expected = List.of(
                CtsField.newNode(Identifier.newInstance(String.class), 0),
                CtsField.newNode(Identifier.newInstance(Stream.class, "node"), Modifier.PRIVATE),
                CtsField.newLeaf(Identifier.newInstance(String.class, "leaf"), Modifier.PRIVATE)
        );

        final List<CtsField> actual = fieldChain.allFields();

        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> Should_ThrowException_When_ChainAllWithoutName_Provider() {
        return Stream.of(
                Arguments.of("leaf", CtsField.newLeaf(Identifier.newInstance(Object.class), 123)),
                Arguments.of("node", CtsField.newNode(Identifier.newInstance(Object.class), 123))
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_ThrowException_When_ChainAllWithoutName_Provider")
    void Should_ThrowException_When_ChainAllWithoutName(@SuppressWarnings("unused") final String description,
                                                        final CtsField field) {
        final CtsFieldChain fieldChain = CtsFieldChain.newRootInstance(String.class)
                .chainAll(List.of(CtsField.newNode(Identifier.newInstance(Object.class, "node"), 0)))
                .get(0);
        final List<CtsField> heads = List.of(field);

        assertThatCode(() -> fieldChain.chainAll(heads))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("cannot append a head without a name");
    }

    private static Stream<Arguments> Should_ThrowException_When_ChainAllOnALeafChain_Provider() {
        return Stream.of(
                Arguments.of("leaf", CtsField.newLeaf(Identifier.newInstance(Object.class, "NAME"), 123)),
                Arguments.of("node", CtsField.newNode(Identifier.newInstance(Object.class, "NAME"), 123))
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_ThrowException_When_ChainAllOnALeafChain_Provider")
    void Should_ThrowException_When_ChainAllOnALeafChain(@SuppressWarnings("unused") final String description,
                                                         final CtsField field) {
        final CtsFieldChain fieldChain = CtsFieldChain.newRootInstance(String.class)
                .chainAll(List.of(CtsField.newLeaf(Identifier.newInstance(Object.class, "leaf"), 0)))
                .get(0);
        final List<CtsField> heads = List.of(field);

        assertThatCode(() -> fieldChain.chainAll(heads))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("cannot append to this chain");
    }

    private static Stream<Arguments> Should_Return_When_Equals_Provider() {
        final CtsFieldChain ctsFieldChain = CtsFieldChain.newRootInstance(Object.class)
                .chainAll(List.of(CtsField.newNode(Identifier.newInstance(Object.class, "name"), 0)))
                .get(0);

        return Stream.of(
                Arguments.of("same object", ctsFieldChain, ctsFieldChain, true),
                Arguments.of("null", ctsFieldChain, null, false),
                Arguments.of("different type", ctsFieldChain, new Object(), false),
                Arguments.of("different", ctsFieldChain, CtsFieldChain.newRootInstance(Object.class), false),
                Arguments.of("same", ctsFieldChain, CtsFieldChain.newRootInstance(Object.class)
                        .chainAll(List.of(CtsField.newNode(Identifier.newInstance(Object.class, "name"), 0)))
                        .get(0), true)
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_Return_When_Equals_Provider")
    void Should_Return_When_Equals(@SuppressWarnings("unused") final String description,
                                   final CtsFieldChain ctsFieldChain,
                                   final Object other,
                                   final boolean expected) {
        final boolean actual = ctsFieldChain.equals(other);

        assertThat(actual).isEqualTo(expected);
    }
}
