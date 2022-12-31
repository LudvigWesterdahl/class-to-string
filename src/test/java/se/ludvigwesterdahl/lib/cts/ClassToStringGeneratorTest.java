package se.ludvigwesterdahl.lib.cts;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.ludvigwesterdahl.lib.cts.strategy.FlatGenerationStrategy;
import se.ludvigwesterdahl.lib.cts.strategy.GenerationStrategy;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class ClassToStringGeneratorTest {

    @SuppressWarnings("unused")
    private static final class Simple {

        private String field1;
        private Inner inner;

        private static final class Inner {

            private String field1Inner;
            private String field2Inner;
        }
    }

    private static Stream<Arguments> Should_ProduceString_When_DefaultConfigurationIsUsed_Provider() {
        return Stream.of(
                Arguments.of(
                        "simple class structure",
                        ClassToStringGenerator.from(Simple.class)
                                .addNode(Identifier.newInstance(Simple.Inner.class)),
                        "field1,inner/field1Inner,inner/field2Inner"),
                Arguments.of(
                        "simple class structure with renaming",
                        ClassToStringGenerator.from(Simple.class)
                                .addNode(Identifier.newInstance(Simple.Inner.class))
                                .rename(Identifier.newInstance(Simple.Inner.class, "inner"),
                                        Identifier.newInstance(Simple.Inner.class, "newInner")),
                        "field1,newInner/field1Inner,newInner/field2Inner"),
                Arguments.of(
                        "simple class structure with embeddings",
                        ClassToStringGenerator.from(Simple.class)
                                .addNode(Identifier.newInstance(Simple.Inner.class))
                                .embed(Identifier.newInstance(Simple.Inner.class)),
                        "field1,field1Inner,field2Inner")
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_ProduceString_When_DefaultConfigurationIsUsed_Provider")
    void Should_ProduceString_When_FlatGenerationStrategyIsUsed(@SuppressWarnings("unused") final String description,
                                                                final ClassToStringGenerator generator,
                                                                final String expected) {
        final GenerationStrategy flatGenerationStrategy = new FlatGenerationStrategy.Builder()
                .withPathSeparator(",")
                .withLevelMarker("/")
                .build();
        generator.addObserver(flatGenerationStrategy);

        final String actual = generator.generate()
                .get(0)
                .generate();

        assertThat(actual).isEqualTo(expected);
    }
}
