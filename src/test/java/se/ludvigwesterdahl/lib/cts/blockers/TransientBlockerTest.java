package se.ludvigwesterdahl.lib.cts.blockers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.ludvigwesterdahl.lib.cts.Blocker;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;

import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendLeaf;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendNode;

final class TransientBlockerTest {

    @Test
    void Should_ReturnSameInstance_When_GetInstance() {
        final Blocker expected = TransientBlocker.getInstance();

        final Blocker actual = TransientBlocker.getInstance();

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void Should_DoNothing_When_EnterNode() {
        final CtsFieldChain chain = mock(CtsFieldChain.class);
        final Blocker blocker = TransientBlocker.getInstance();

        blocker.enterNode(chain);

        verifyNoInteractions(chain);
    }

    @Test
    void Should_DoNothing_When_ConsumeLeaf() {
        final CtsFieldChain chain = mock(CtsFieldChain.class);
        final Blocker blocker = TransientBlocker.getInstance();

        blocker.consumeLeaf(chain);

        verifyNoInteractions(chain);
    }

    @Test
    void Should_DoNothing_When_LeaveNode() {
        final CtsFieldChain chain = mock(CtsFieldChain.class);
        final Blocker blocker = TransientBlocker.getInstance();

        blocker.leaveNode(chain);

        verifyNoInteractions(chain);
    }

    private static Stream<Arguments> Should_Return_When_Block_Provider() {
        final CtsFieldChain root = CtsFieldChain.newRootInstance(Object.class);
        final CtsFieldChain transientLeaf = appendLeaf(root, Object.class, "leaf", Modifier.TRANSIENT);
        final CtsFieldChain leaf = appendLeaf(root, Object.class, "leaf", 0);
        final CtsFieldChain transientNode = appendNode(root, Object.class, "node", Modifier.TRANSIENT);
        final CtsFieldChain node = appendNode(root, Object.class, "node", 0);

        return Stream.of(
                Arguments.of("transient leaf", transientLeaf, true),
                Arguments.of("leaf", leaf, false),
                Arguments.of("transient node", transientNode, true),
                Arguments.of("node", node, false)
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_Return_When_Block_Provider")
    void Should_Return_When_Block(@SuppressWarnings("unused") final String description,
                                  final CtsFieldChain chain,
                                  final boolean expected) {
        final Blocker blocker = TransientBlocker.getInstance();

        final boolean actual = blocker.block(chain);

        assertThat(actual).isEqualTo(expected);
    }
}
