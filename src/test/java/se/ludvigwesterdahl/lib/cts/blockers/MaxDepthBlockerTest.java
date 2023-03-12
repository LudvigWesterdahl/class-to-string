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
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateNode;

final class MaxDepthBlockerTest {

    @Test
    void Should_ThrowNpe_When_ConstructingBlocker() {
        assertThatCode(() -> MaxDepthBlocker.newInstance(null, 0))
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void Should_ThrowException_When_ConstructingBlocker() {
        final Identifier blockingPoint = Identifier.newInstance(Object.class);

        assertThatCode(() -> MaxDepthBlocker.newInstance(blockingPoint, -1))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("continueLevels cannot be negative");
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
                                  final int continueLevels) {
        final Identifier blockingPoint = blockingPointWithName
                ? Identifier.newInstance(String.class, "string")
                : Identifier.newInstance(String.class);
        final CtsFieldChain root = CtsFieldChain.newRootInstance(Object.class);
        final CtsFieldChain node1 = appendPrivateNode(root, String.class, "string");
        final CtsFieldChain node2 = appendPrivateNode(node1, Object.class, "object");
        final CtsFieldChain leaf1 = appendPrivateNode(node2, Object.class, "object");
        final Blocker blocker = MaxDepthBlocker.newInstance(blockingPoint, continueLevels);
        final boolean[] expected = new boolean[continueLevels + 1];
        expected[expected.length - 1] = true;

        blocker.enterNode(root);
        blocker.enterNode(node1);
        final boolean[] actual = new boolean[continueLevels + 1];
        for (int i = 0; i < continueLevels + 1; i++) {
            actual[i] = blocker.block(node2);
            blocker.enterNode(node2);
            blocker.consumeLeaf(leaf1);
        }

        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> Should_KeepTrackOfDepth_When_EnteringAndLeavingNodes_Provider() {
        return Stream.of(
                Arguments.of("blocking point with name", true),
                Arguments.of("blocking point without name", false)
        );
    }

    /**
     * This test will be using the following model.
     * <pre>
     * #01234567890123456789-01234567890123456789#
     * #                    R                    #
     * #                    |                    #
     * #                    P                    #
     * #                  /   \                  #
     * #               A0      A1                #
     * #              /       /   \              #
     * #            B0      B1     B2            #
     * #           /              /   \          #
     * #         C0             C1     C2        #
     * #        /              /   \      \      #
     * #      D0             D1     D2     D3    #
     * #01234567890123456789-01234567890123456789#
     * </pre>
     * Where max depth is 3, and as such, the D nodes should be blocked.
     * Processing is done depth first left to right.
     */
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_KeepTrackOfDepth_When_EnteringAndLeavingNodes_Provider")
    void Should_KeepTrackOfDepth_When_EnteringAndLeavingNodes(@SuppressWarnings("unused") final String description,
                                                              final boolean blockingPointWithName) {
        final Identifier blockingPoint = blockingPointWithName
                ? Identifier.newInstance(String.class, "p")
                : Identifier.newInstance(String.class);
        final boolean[] expected = {
                false, // R
                false, // P
                false, // A0
                false, // B0
                false, // C0
                true, // D0
                false, // A1
                false, // B1
                false, // B2
                false, // C1
                true, // D1
                true, // D2
                false, // C2
                true // D3
        };
        final CtsFieldChain root = CtsFieldChain.newRootInstance(Object.class);
        final CtsFieldChain nodeP = appendPrivateNode(root, String.class, "p");
        final CtsFieldChain nodeA0 = appendPrivateNode(nodeP, Object.class, "a0");
        final CtsFieldChain nodeA1 = appendPrivateNode(nodeP, Object.class, "a1");
        // This one is the same as the blocking point.
        final CtsFieldChain nodeB0 = appendPrivateNode(nodeA0, String.class, "p");
        final CtsFieldChain nodeB1 = appendPrivateNode(nodeA1, Object.class, "b1");
        final CtsFieldChain nodeB2 = appendPrivateNode(nodeA1, Object.class, "b2");
        final CtsFieldChain nodeC0 = appendPrivateNode(nodeB0, Object.class, "c0");
        // This one is the same as the blocking point.
        final CtsFieldChain nodeC1 = appendPrivateNode(nodeB2, String.class, "p");
        // This one is the same as the blocking point.
        final CtsFieldChain nodeC2 = appendPrivateNode(nodeB2, String.class, "p");
        final CtsFieldChain nodeD0 = appendPrivateNode(nodeC0, Object.class, "d0");
        final CtsFieldChain nodeD1 = appendPrivateNode(nodeC1, Object.class, "d0");
        // This one is the same as the blocking point.
        final CtsFieldChain nodeD2 = appendPrivateNode(nodeC1, String.class, "p");
        final CtsFieldChain nodeD3 = appendPrivateNode(nodeC2, Object.class, "d0");
        final Blocker blocker = MaxDepthBlocker.newInstance(blockingPoint, 3);

        final boolean[] actual = new boolean[expected.length];
        actual[0] = blocker.block(root);
        blocker.enterNode(root);
        actual[1] = blocker.block(nodeP);
        blocker.enterNode(nodeP);
        actual[2] = blocker.block(nodeA0);
        blocker.enterNode(nodeA0);
        actual[3] = blocker.block(nodeB0);
        blocker.enterNode(nodeB0);
        actual[4] = blocker.block(nodeC0);
        blocker.enterNode(nodeC0);
        actual[5] = blocker.block(nodeD0);
        blocker.enterNode(nodeD0);
        blocker.leaveNode(nodeD0);
        blocker.leaveNode(nodeC0);
        blocker.leaveNode(nodeB0);
        blocker.leaveNode(nodeA0);
        actual[6] = blocker.block(nodeA1);
        blocker.enterNode(nodeA1);
        actual[7] = blocker.block(nodeB1);
        blocker.enterNode(nodeB1);
        blocker.leaveNode(nodeB1);
        actual[8] = blocker.block(nodeB2);
        blocker.enterNode(nodeB2);
        actual[9] = blocker.block(nodeC1);
        blocker.enterNode(nodeC1);
        actual[10] = blocker.block(nodeD1);
        blocker.enterNode(nodeD1);
        blocker.leaveNode(nodeD1);
        actual[11] = blocker.block(nodeD2);
        blocker.enterNode(nodeD2);
        blocker.leaveNode(nodeD2);
        blocker.leaveNode(nodeC1);
        actual[12] = blocker.block(nodeC2);
        blocker.enterNode(nodeC2);
        actual[13] = blocker.block(nodeD3);
        blocker.enterNode(nodeD3);
        blocker.leaveNode(nodeD3);
        blocker.leaveNode(nodeC2);
        blocker.leaveNode(nodeB2);
        blocker.leaveNode(nodeA1);
        blocker.leaveNode(nodeP);
        blocker.leaveNode(root);

        assertThat(actual).isEqualTo(expected);
    }
}
