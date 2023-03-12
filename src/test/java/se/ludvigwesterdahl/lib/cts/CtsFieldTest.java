package se.ludvigwesterdahl.lib.cts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class CtsFieldTest {

    @Test
    void Should_ReturnEqualObject_When_NewNode() {
        final CtsField expected = CtsField.newNode(Identifier.newInstance(Object.class), 123);

        final CtsField actual = CtsField.newNode(Identifier.newInstance(Object.class), 123);

        assertThat(actual)
                .isEqualTo(expected)
                .hasSameHashCodeAs(expected)
                .hasToString(expected.toString());
    }

    @Test
    void Should_ReturnEqualObject_When_NewLeaf() {
        final CtsField expected = CtsField.newLeaf(Identifier.newInstance(Object.class), 123);

        final CtsField actual = CtsField.newLeaf(Identifier.newInstance(Object.class), 123);

        assertThat(actual)
                .isEqualTo(expected)
                .hasSameHashCodeAs(expected)
                .hasToString(expected.toString());
    }

    @Test
    void Should_ReturnDifferentObject_When_NewNodeAndNewLeaf() {
        final CtsField expected = CtsField.newNode(Identifier.newInstance(Object.class), 123);

        final CtsField actual = CtsField.newLeaf(Identifier.newInstance(Object.class), 123);

        assertThat(actual).isNotEqualTo(expected);
    }

    @Test
    void Should_ReturnValues_When_NewNodeAndUsingGetters() {
        final Identifier expectedIdentifier = Identifier.newInstance(String.class, "string");
        final int expectedModifiers = Modifier.PRIVATE | Modifier.FINAL;
        final boolean expectedNode = true;
        final CtsField field = CtsField.newNode(expectedIdentifier, expectedModifiers);

        final Identifier actualIdentifier = field.getIdentifier();
        final int actualModifiers = field.getModifiers();
        final boolean actualNode = field.isNode();

        assertThat(actualIdentifier).isEqualTo(expectedIdentifier);
        assertThat(actualModifiers).isEqualTo(expectedModifiers);
        assertThat(expectedNode).isEqualTo(actualNode);
    }

    @Test
    void Should_ReturnValues_When_NewLeafAndUsingGetters() {
        final Identifier expectedIdentifier = Identifier.newInstance(String.class, "string");
        final int expectedModifiers = Modifier.PRIVATE | Modifier.FINAL;
        final boolean expectedNode = false;
        final CtsField field = CtsField.newLeaf(expectedIdentifier, expectedModifiers);

        final Identifier actualIdentifier = field.getIdentifier();
        final int actualModifiers = field.getModifiers();
        final boolean actualNode = field.isNode();

        assertThat(actualIdentifier).isEqualTo(expectedIdentifier);
        assertThat(actualModifiers).isEqualTo(expectedModifiers);
        assertThat(expectedNode).isEqualTo(actualNode);
    }

    private static Stream<Arguments> Should_Return_When_Equals_Provider() {
        final Identifier identifier = Identifier.newInstance(Object.class, "name");
        final CtsField node = CtsField.newNode(identifier, 0);
        final CtsField leaf = CtsField.newLeaf(identifier, 0);
        return Stream.of(
                Arguments.of("same object", node, node, true),
                Arguments.of("same object", leaf, leaf, true),
                Arguments.of("null", node, null, false),
                Arguments.of("null", leaf, null, false),
                Arguments.of("different type", node, new Object(), false),
                Arguments.of("different type", leaf, new Object(), false),
                Arguments.of("different", node, leaf, false),
                Arguments.of("different", leaf, node, false),
                Arguments.of("same", node, CtsField.newNode(Identifier.newInstance(Object.class, "name"), 0), true),
                Arguments.of("same", leaf, CtsField.newLeaf(Identifier.newInstance(Object.class, "name"), 0), true)
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_Return_When_Equals_Provider")
    void Should_Return_When_Equals(@SuppressWarnings("unused") final String description,
                                   final CtsField ctsField,
                                   final Object other,
                                   final boolean expected) {
        final boolean actual = ctsField.equals(other);

        assertThat(actual).isEqualTo(expected);
    }
}
