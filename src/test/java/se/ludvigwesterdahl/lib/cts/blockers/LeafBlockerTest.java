package se.ludvigwesterdahl.lib.cts.blockers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.ludvigwesterdahl.lib.cts.Blocker;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateLeaf;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateNode;

final class LeafBlockerTest {

    @Test
    void Should_ReturnSameInstance_When_GetInstance() {
        final Blocker expected = LeafBlocker.getInstance();

        final Blocker actual = LeafBlocker.getInstance();

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void Should_DoNothing_When_EnterNode() {
        final CtsFieldChain chain = mock(CtsFieldChain.class);
        final Blocker blocker = LeafBlocker.getInstance();

        blocker.enterNode(chain);

        verifyNoInteractions(chain);
    }

    @Test
    void Should_DoNothing_When_ConsumeLeaf() {
        final CtsFieldChain chain = mock(CtsFieldChain.class);
        final Blocker blocker = LeafBlocker.getInstance();

        blocker.consumeLeaf(chain);

        verifyNoInteractions(chain);
    }

    @Test
    void Should_DoNothing_When_LeaveNode() {
        final CtsFieldChain chain = mock(CtsFieldChain.class);
        final Blocker blocker = LeafBlocker.getInstance();

        blocker.leaveNode(chain);

        verifyNoInteractions(chain);
    }

    private static Stream<Arguments> Should_Return_When_Block_Provider() {
        final CtsFieldChain root = CtsFieldChain.newRootInstance(Object.class);
        final CtsFieldChain leaf = appendPrivateLeaf(root, Object.class, "leaf");
        final CtsFieldChain node = appendPrivateNode(root, Object.class, "node");

        return Stream.of(
                Arguments.of("leaf", leaf, true),
                Arguments.of("node", node, false)
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_Return_When_Block_Provider")
    void Should_Return_When_Block(@SuppressWarnings("unused") final String description,
                                  final CtsFieldChain chain,
                                  final boolean expected) {
        final Blocker blocker = LeafBlocker.getInstance();

        final boolean actual = blocker.block(chain);

        assertThat(actual).isEqualTo(expected);
    }
}
