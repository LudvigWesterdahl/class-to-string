package se.ludvigwesterdahl.lib.cts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

final class IdentifierTest {

    private static Stream<Arguments> Should_ThrowNpe_When_NewInstance_Provider() {
        return Stream.of(
                Arguments.of("missing name", Object.class, null),
                Arguments.of("missing type", null, "name"),
                Arguments.of("missing name and type", null, null)
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_ThrowNpe_When_NewInstance_Provider")
    void Should_ThrowNpe_When_NewInstance(@SuppressWarnings("unused") final String description,
                                          final Class<?> type,
                                          final String name) {
        assertThatCode(() -> Identifier.newInstance(type, name))
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void Should_ThrowException_When_NewInstance() {
        assertThatCode(() -> Identifier.newInstance(Object.class, "  "))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("name cannot be blank");
    }

    @Test
    void Should_ThrowNpe_When_NewInstanceWithOnlyType() {
        assertThatCode(() -> Identifier.newInstance(null))
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void Should_ReturnValues_When_NewInstanceAndUsingGetters() {
        final Class<?> expectedType = Object.class;
        final Optional<String> expectedName = Optional.of("name");
        final Identifier identifier = Identifier.newInstance(expectedType, expectedName.orElseThrow());

        final Class<?> actualType = identifier.getType();
        final Optional<String> actualName = identifier.getName();

        assertThat(actualType).isEqualTo(expectedType);
        assertThat(actualName).isEqualTo(expectedName);
    }

    @Test
    void Should_ReturnValues_When_NewInstanceWithOnlyTypeAndUsingGetters() {
        final Class<?> expectedType = Object.class;
        final Optional<String> expectedName = Optional.empty();
        final Identifier identifier = Identifier.newInstance(expectedType);

        final Class<?> actualType = identifier.getType();
        final Optional<String> actualName = identifier.getName();

        assertThat(actualType).isEqualTo(expectedType);
        assertThat(actualName).isEqualTo(expectedName);
    }

    private static Stream<Arguments> Should_Return_When_Matches_Provider() {
        return Stream.of(
                Arguments.of("same type and name",
                        Identifier.newInstance(Object.class, "name"),
                        Identifier.newInstance(Object.class, "name"),
                        true),
                Arguments.of("other is missing name",
                        Identifier.newInstance(Object.class, "name"),
                        Identifier.newInstance(Object.class),
                        true),
                Arguments.of("identifier is missing name",
                        Identifier.newInstance(Object.class),
                        Identifier.newInstance(Object.class, "name"),
                        true),
                Arguments.of("both are missing name",
                        Identifier.newInstance(Object.class),
                        Identifier.newInstance(Object.class),
                        true),
                Arguments.of("same name but different type",
                        Identifier.newInstance(Object.class, "name"),
                        Identifier.newInstance(String.class, "name"),
                        false),
                Arguments.of("identifier missing name and different type",
                        Identifier.newInstance(Object.class),
                        Identifier.newInstance(String.class, "name"),
                        false),
                Arguments.of("other is missing name and different type",
                        Identifier.newInstance(Object.class, "name"),
                        Identifier.newInstance(String.class),
                        false),
                Arguments.of("other is null",
                        Identifier.newInstance(Object.class, "name"),
                        null,
                        false)
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_Return_When_Matches_Provider")
    void Should_Return_When_Matches(@SuppressWarnings("unused") final String description,
                                    final Identifier identifier,
                                    final Identifier other,
                                    final boolean expected) {
        final boolean actual = identifier.matches(other);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void Should_ReturnEqualObjects_When_NewInstance() {
        final Identifier expected = Identifier.newInstance(Object.class, "name");

        final Identifier actual = Identifier.newInstance(Object.class, "name");

        assertThat(actual)
                .isEqualTo(expected)
                .hasSameHashCodeAs(expected)
                .hasToString(expected.toString());
    }

    @Test
    void Should_ReturnEqualObjects_When_StripName() {
        final Identifier expected = Identifier.newInstance(Object.class);

        final Identifier actual = Identifier.newInstance(Object.class, "name").stripName();

        assertThat(actual)
                .isEqualTo(expected)
                .hasSameHashCodeAs(expected)
                .hasToString(expected.toString());
    }

    private static Stream<Arguments> Should_Return_When_Equals_Provider() {
        final Identifier identifier = Identifier.newInstance(Object.class, "name");
        return Stream.of(
                Arguments.of("same object", identifier, identifier, true),
                Arguments.of("null", identifier, null, false),
                Arguments.of("different type", identifier, new Object(), false),
                Arguments.of("different", identifier, Identifier.newInstance(String.class), false),
                Arguments.of("different", identifier, Identifier.newInstance(Object.class, "hello"), false),
                Arguments.of("same", identifier, Identifier.newInstance(Object.class, "name"), true)
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_Return_When_Equals_Provider")
    void Should_Return_When_Equals(@SuppressWarnings("unused") final String description,
                                   final Identifier identifier,
                                   final Object other,
                                   final boolean expected) {
        final boolean actual = identifier.equals(other);

        assertThat(actual).isEqualTo(expected);
    }
}
