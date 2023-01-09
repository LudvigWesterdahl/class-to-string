package se.ludvigwesterdahl.lib.cts;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.ludvigwesterdahl.lib.fixture.CtsNotification;
import se.ludvigwesterdahl.lib.fixture.CtsTestCase;
import se.ludvigwesterdahl.lib.fixture.CtsTestCaseGroup;
import se.ludvigwesterdahl.lib.fixture.SimpleStructureWithCode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class ClassToStringGeneratorTest {

    private static final List<CtsTestCaseGroup> TEST_CASE_GROUPS = List.of(
            new SimpleStructureWithCode()
    );

    private static Stream<Arguments> toExpectedGenerateArguments(final CtsTestCaseGroup group) {
        return group.testCases()
                .stream()
                .filter(CtsTestCase::hasExpectedGenerate)
                .map(c -> Arguments.of(
                        group.getClass().getSimpleName(),
                        c.getClass().getSimpleName(),
                        c));
    }

    private static Stream<Arguments> Should_ProduceString_When_Generate_Provider() {
        return TEST_CASE_GROUPS.stream()
                .flatMap(ClassToStringGeneratorTest::toExpectedGenerateArguments);
    }

    @ParameterizedTest(name = "{index}: {0} - {1}")
    @MethodSource("Should_ProduceString_When_Generate_Provider")
    void Should_ProduceString_When_Generate(@SuppressWarnings("unused") final String groupName,
                                            @SuppressWarnings("unused") final String testName,
                                            final CtsTestCase testCase) {
        final String expected = testCase.expectedGenerate();
        final ClassToStringGenerator generator = testCase.generator();

        final String actual = generator.iterate()
                .get(0)
                .generate();

        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> toExpectedNotificationsArguments(final CtsTestCaseGroup group) {
        return group.testCases()
                .stream()
                .filter(CtsTestCase::hasExpectedNotifications)
                .map(c -> Arguments.of(
                        group.getClass().getSimpleName(),
                        c.getClass().getSimpleName(),
                        c));
    }

    private static Stream<Arguments> Should_NotifyObserver_When_Generate_Provider() {
        return TEST_CASE_GROUPS.stream()
                .flatMap(ClassToStringGeneratorTest::toExpectedNotificationsArguments);
    }

    @ParameterizedTest(name = "{index}: {0} - {1}")
    @MethodSource("Should_NotifyObserver_When_Generate_Provider")
    void Should_NotifyObserver_When_Generate(@SuppressWarnings("unused") final String groupName,
                                             @SuppressWarnings("unused") final String testName,
                                             final CtsTestCase testCase) {
        final List<CtsNotification> expected = testCase.expectedNotifications();
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
        final ClassToStringGenerator generator = testCase.generator();
        generator.addObserver(actualObserver);

        generator.iterate();

        assertThat(actual).isEqualTo(expected);
    }
}
