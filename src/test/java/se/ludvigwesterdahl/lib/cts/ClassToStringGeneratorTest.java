package se.ludvigwesterdahl.lib.cts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.ludvigwesterdahl.lib.fixture.ctstestcases.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

final class ClassToStringGeneratorTest {

    private static final List<CtsTestCaseGroup> TEST_CASE_GROUPS = List.of(
            new SimpleStructureWithCode(),
            new EmbedBeforeBlocker(),
            new ListGenericRename(),
            new ListGenericRenameEmbedWithAnnotation(),
            new FixEmbedAnnotationCycle(),
            new RemoveRenameAnnotation(),
            new AddRemoveNameWithCode(),
            new SimpleListSameErasure(),
            new TypicalSelectExpandQuery()
    );

    private static Stream<Arguments> toArguments(final Predicate<CtsTestCase> predicate) {
        return TEST_CASE_GROUPS.stream()
                .flatMap(group -> group.testCases()
                        .stream()
                        .filter(predicate)
                        .map(testCase -> Arguments.of(
                                group.getClass().getSimpleName(),
                                testCase.getClass().getSimpleName(),
                                testCase)));
    }

    private static Stream<Arguments> Should_ProduceString_When_Generate_Provider() {
        return toArguments(CtsTestCase::hasExpectedGenerate);
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

    private static Stream<Arguments> Should_NotifyObserver_When_Generate_Provider() {
        return toArguments(CtsTestCase::hasExpectedNotifications);
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

    @SuppressWarnings("unused")
    private static final class Circular {

        private String field;
        private Circular circular;
    }

    @Test
    void Should_ThrowException_When_CircularEmbeddingDetected() {
        final ClassToStringGenerator generator = ClassToStringGenerator.from(Circular.class)
                .addEmbedding(Circular.class, Identifier.newInstance(Circular.class));

        assertThatCode(generator::iterate)
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("illegal loop detected");
    }

    @Test
    void Should_ThrowNpe_When_FromWithNull() {
        assertThatCode(() -> ClassToStringGenerator.from(null))
                .isExactlyInstanceOf(NullPointerException.class);
    }

    private static Stream<Arguments> Should_ThrowNpe_When_RenameGivenNull_Provider() {
        return Stream.of(
                Arguments.of("missing to", Identifier.newInstance(String.class, "string"), null),
                Arguments.of("missing from", null, Identifier.newInstance(String.class, "string")),
                Arguments.of("missing both", null, null)
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_ThrowNpe_When_RenameGivenNull_Provider")
    void Should_ThrowNpe_When_RenameGivenNull(@SuppressWarnings("unused") final String description,
                                              final Identifier from,
                                              final Identifier to) {
        final ClassToStringGenerator generator = ClassToStringGenerator.from(Object.class);

        assertThatCode(() -> generator.addName(from, to))
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void Should_ThrowException_When_RenameAndToMissingName() {
        final ClassToStringGenerator generator = ClassToStringGenerator.from(Object.class);
        final Identifier from = Identifier.newInstance(String.class);
        final Identifier to = Identifier.newInstance(Object.class);

        assertThatCode(() -> generator.addName(null, from, to))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("to is missing a name");
    }
}
