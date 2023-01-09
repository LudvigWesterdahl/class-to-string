package se.ludvigwesterdahl.lib.cts;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.ludvigwesterdahl.lib.fixture.CtsGenerationFixtureGroup;
import se.ludvigwesterdahl.lib.fixture.CtsNotification;
import se.ludvigwesterdahl.lib.fixture.CtsNotificationFixtureGroup;
import se.ludvigwesterdahl.lib.fixture.generation.SimpleStructureWithCodeGeneration;
import se.ludvigwesterdahl.lib.fixture.notification.SimpleStructureWithCodeNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class ClassToStringGeneratorTest {

    private static final List<CtsGenerationFixtureGroup> GENERATION_FIXTURES = List.of(
            new SimpleStructureWithCodeGeneration()
    );

    private static final List<CtsNotificationFixtureGroup> NOTIFICATION_FIXTURES = List.of(
            new SimpleStructureWithCodeNotification()
    );


    private static Stream<Arguments> fromGenerationFixtureGroup(final CtsGenerationFixtureGroup group) {
        return group.generationFixtures()
                .stream()
                .map(c -> Arguments.of(
                        group.getClass().getSimpleName(),
                        c.getClass().getSimpleName(),
                        c.generator(),
                        c.expected()));
    }

    private static Stream<Arguments> Should_ProduceString_When_Generate_Provider() {
        return GENERATION_FIXTURES.stream()
                .flatMap(ClassToStringGeneratorTest::fromGenerationFixtureGroup);
    }

    @ParameterizedTest(name = "{index}: {0} - {1}")
    @MethodSource("Should_ProduceString_When_Generate_Provider")
    void Should_ProduceString_When_Generate(@SuppressWarnings("unused") final String groupName,
                                            @SuppressWarnings("unused") final String fixtureName,
                                            final ClassToStringGenerator generator,
                                            final String expected) {
        final String actual = generator.iterate()
                .get(0)
                .generate();

        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> fromNotificationFixtureGroup(final CtsNotificationFixtureGroup group) {
        return group.notificationFixtures()
                .stream()
                .map(c -> Arguments.of(
                        group.getClass().getSimpleName(),
                        c.getClass().getSimpleName(),
                        c.generator(),
                        c.expected()));
    }

    private static Stream<Arguments> Should_NotifyObserver_When_Generate_Provider() {
        return NOTIFICATION_FIXTURES.stream()
                .flatMap(ClassToStringGeneratorTest::fromNotificationFixtureGroup);
    }

    @ParameterizedTest(name = "{index}: {0} - {1}")
    @MethodSource("Should_NotifyObserver_When_Generate_Provider")
    void Should_NotifyObserver_When_Generate(@SuppressWarnings("unused") final String groupName,
                                             @SuppressWarnings("unused") final String fixtureName,
                                             final ClassToStringGenerator generator,
                                             final List<CtsNotification> expected) {
        final List<CtsNotification> actual = new ArrayList<>();
        final Observer actualObserver = new Observer() {
            @Override
            public void enterNode(final CtsFieldChain nodeFieldChain) {
                actual.add(CtsNotification.notification(CtsNotification.Type.ENTER_NODE, nodeFieldChain));
            }

            @Override
            public void consumeLeaf(final CtsFieldChain leafFieldChain) {
                actual.add(CtsNotification.notification(CtsNotification.Type.CONSUME_LEAF, leafFieldChain));
            }

            @Override
            public void leaveNode(final CtsFieldChain nodeFieldChain) {
                actual.add(CtsNotification.notification(CtsNotification.Type.LEAVE_NODE, nodeFieldChain));
            }
        };
        generator.addObserver(actualObserver);

        generator.iterate();

        assertThat(actual).isEqualTo(expected);
    }
}
