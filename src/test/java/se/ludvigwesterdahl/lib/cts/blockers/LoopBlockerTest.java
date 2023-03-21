package se.ludvigwesterdahl.lib.cts.blockers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.ludvigwesterdahl.lib.cts.Blocker;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;
import se.ludvigwesterdahl.lib.cts.Identifier;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateNode;

final class LoopBlockerTest {

    @Test
    void Should_ThrowNpe_When_ConstructingBlocker() {
        assertThatCode(() -> LoopBlocker.loop(null, 0))
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void Should_ThrowException_When_ConstructingBlocker() {
        final Identifier blockingPoint = Identifier.newInstance(Object.class);

        assertThatCode(() -> LoopBlocker.loop(blockingPoint, -1))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("times cannot be negative");
    }

    private static Stream<Arguments> Should_DoNothing_When_LeaveNode_Provider() {
        return Stream.of(
                Arguments.of(0),
                Arguments.of(1),
                Arguments.of(2),
                Arguments.of(10),
                Arguments.of(2000)
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_DoNothing_When_LeaveNode_Provider")
    void Should_DoNothing_When_LeaveNode(final int times) {
        final CtsFieldChain chain = mock(CtsFieldChain.class);
        final Blocker blocker = LoopBlocker.loop(Identifier.newInstance(Object.class), times);

        blocker.leaveNode(chain);

        verifyNoInteractions(chain);
    }

    private static Stream<Arguments> Should_Return_When_Block_Provider() {
        return Stream.of(
                Arguments.of("0 times", true, 0),
                Arguments.of("0 times without name", false, 0),
                Arguments.of("1 times", true, 1),
                Arguments.of("1 times without name", false, 1),
                Arguments.of("2 times", true, 2),
                Arguments.of("2 times without name", false, 2),
                Arguments.of("10 times", true, 10),
                Arguments.of("10 times without name", false, 10),
                Arguments.of("2000 times", true, 2000),
                Arguments.of("2000 times without name", false, 2000)
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_Return_When_Block_Provider")
    void Should_Return_When_Block(@SuppressWarnings("unused") final String description,
                                  final boolean blockingPointWithName,
                                  final int times) {
        final Identifier blockingPoint = blockingPointWithName
                ? Identifier.newInstance(String.class, "string")
                : Identifier.newInstance(String.class);
        final CtsFieldChain root = CtsFieldChain.newRootInstance(Object.class);
        final CtsFieldChain node1 = appendPrivateNode(root, String.class, "string");
        final CtsFieldChain node2 = appendPrivateNode(node1, Object.class, "object");
        final CtsFieldChain leaf1 = appendPrivateNode(node2, Object.class, "object");
        final Blocker blocker = LoopBlocker.loop(blockingPoint, times);
        final boolean[] expected = new boolean[times + 1];
        expected[expected.length - 1] = true;

        blocker.enterNode(root);
        final boolean[] actual = new boolean[times + 1];
        for (int i = 0; i < times + 1; i++) {
            actual[i] = blocker.block(node1);
            blocker.enterNode(node1);
            blocker.enterNode(node2);
            blocker.consumeLeaf(leaf1);
        }

        assertThat(actual).isEqualTo(expected);
    }
}
