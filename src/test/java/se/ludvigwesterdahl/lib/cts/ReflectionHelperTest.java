package se.ludvigwesterdahl.lib.cts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

final class ReflectionHelperTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface AnnotationExample {

        String first() default "NAME 1";

        String second() default "NAME 2";
    }

    @SuppressWarnings("unused") // The fields are not directly accessed.
    public static final class ClassExample {

        @AnnotationExample
        private int firstField;

        @AnnotationExample(first = "NOT DEFAULT 1")
        private int secondField;

        @AnnotationExample(second = "NOT DEFAULT 2")
        private int thirdField;

        @AnnotationExample(first = "NOT DEFAULT 1", second = "NOT DEFAULT 2")
        private int fourthField;
    }

    @Test
    void Should_ThrowException_When_CallingConstructor() throws Exception {
        final Constructor<ReflectionHelper> constructor = ReflectionHelper.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatCode(constructor::newInstance)
                .hasRootCauseExactlyInstanceOf(AssertionError.class)
                .hasRootCauseMessage("this private constructor is suppressed");
    }

    private static Stream<Arguments> Should_Return_When_HasDefaultValues_Provider() {
        return Stream.of(
                Arguments.of("firstField", true),
                Arguments.of("secondField", false),
                Arguments.of("thirdField", false),
                Arguments.of("fourthField", false)
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_Return_When_HasDefaultValues_Provider")
    void Should_Return_When_HasDefaultValues(final String fieldName,
                                             final boolean expected) throws Exception {
        final Field field = ClassExample.class.getDeclaredField(fieldName);
        final AnnotationExample instance = field.getAnnotation(AnnotationExample.class);

        final boolean actual = ReflectionHelper.hasDefaultValues(AnnotationExample.class, instance);

        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> Should_ReturnValue_When_GetAnnotationValue_Provider() {
        return Stream.of(
                Arguments.of("firstField - first", "firstField", "first", null),
                Arguments.of("firstField - second", "firstField", "second", null),
                Arguments.of("secondField - first", "secondField", "first", "NOT DEFAULT 1"),
                Arguments.of("secondField - second", "secondField", "second", null),
                Arguments.of("thirdField - first", "thirdField", "first", null),
                Arguments.of("thirdField - second", "thirdField", "second", "NOT DEFAULT 2"),
                Arguments.of("fourthField - first", "fourthField", "first", "NOT DEFAULT 1"),
                Arguments.of("fourthField - second", "fourthField", "second", "NOT DEFAULT 2")
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_ReturnValue_When_GetAnnotationValue_Provider")
    void Should_ReturnValue_When_GetAnnotationValue(@SuppressWarnings("unused") final String description,
                                                    final String fieldName,
                                                    final String annotationName,
                                                    final String expected) throws Exception {
        final Field field = ClassExample.class.getDeclaredField(fieldName);
        final AnnotationExample instance = field.getAnnotation(AnnotationExample.class);

        final String actual = ReflectionHelper.getAnnotationValue(
                AnnotationExample.class,
                instance,
                String.class,
                annotationName,
                null);

        assertThat(actual).isEqualTo(expected);
    }
}
