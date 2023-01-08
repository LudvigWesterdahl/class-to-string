package se.ludvigwesterdahl.lib.cts;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.ludvigwesterdahl.lib.fixture.CtsFixtureGroup;
import se.ludvigwesterdahl.lib.fixture.SimpleStructureCodeCtsFixtures;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class ClassToStringGeneratorTest {

    private static final List<CtsFixtureGroup> FIXTURES = List.of(
            new SimpleStructureCodeCtsFixtures()
    );

    private static Stream<Arguments> fromGroup(final CtsFixtureGroup group) {
        return group.ctsFixtures()
                .stream()
                .map(c -> Arguments.of(
                        group.getClass().getSimpleName(),
                        c.getClass().getSimpleName(),
                        c.generator(),
                        c.expected()));
    }

    private static Stream<Arguments> Should_ProduceString_When_DefaultConfigurationIsUsed_Provider() {
        return FIXTURES.stream()
                .flatMap(ClassToStringGeneratorTest::fromGroup);
    }

    @ParameterizedTest(name = "{index}: {0} - {1}")
    @MethodSource("Should_ProduceString_When_DefaultConfigurationIsUsed_Provider")
    void Should_ProduceString_When_Generate(@SuppressWarnings("unused") final String groupName,
                                            @SuppressWarnings("unused") final String fixtureName,
                                            final ClassToStringGenerator generator,
                                            final String expected) {
        final String actual = generator.iterate()
                .get(0)
                .generate();

        assertThat(actual).isEqualTo(expected);
    }
}
