package se.ludvigwesterdahl.lib.cts.blocker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;

import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendLeaf;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendNode;

final class StaticBlockerTest {

    @Test
    void Should_ReturnSameInstance_When_GetInstance() {
        final Blocker expected = StaticBlocker.getInstance();

        final Blocker actual = StaticBlocker.getInstance();

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void Should_DoNothing_When_EnterNode() {
        final CtsFieldChain chain = mock(CtsFieldChain.class);
        final Blocker blocker = StaticBlocker.getInstance();

        blocker.enterNode(chain);

        verifyNoInteractions(chain);
    }

    @Test
    void Should_DoNothing_When_ConsumeLeaf() {
        final CtsFieldChain chain = mock(CtsFieldChain.class);
        final Blocker blocker = StaticBlocker.getInstance();

        blocker.consumeLeaf(chain);

        verifyNoInteractions(chain);
    }

    @Test
    void Should_DoNothing_When_LeaveNode() {
        final CtsFieldChain chain = mock(CtsFieldChain.class);
        final Blocker blocker = StaticBlocker.getInstance();

        blocker.leaveNode(chain);

        verifyNoInteractions(chain);
    }

    private static Stream<Arguments> Should_Return_When_Block_Provider() {
        final CtsFieldChain root = CtsFieldChain.newRootInstance(Object.class);
        final CtsFieldChain staticLeaf = appendLeaf(root, Object.class, "leaf", Modifier.PRIVATE | Modifier.STATIC);
        final CtsFieldChain leaf = appendLeaf(root, Object.class, "leaf", Modifier.PRIVATE);
        final CtsFieldChain staticNode = appendNode(root, Object.class, "node", Modifier.PRIVATE | Modifier.STATIC);
        final CtsFieldChain node = appendNode(root, Object.class, "node", Modifier.PRIVATE);

        return Stream.of(
                Arguments.of("static leaf", staticLeaf, true),
                Arguments.of("leaf", leaf, false),
                Arguments.of("static node", staticNode, true),
                Arguments.of("node", node, false)
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_Return_When_Block_Provider")
    void Should_Return_When_Block(@SuppressWarnings("unused") final String description,
                                  final CtsFieldChain chain,
                                  final boolean expected) {
        final Blocker blocker = StaticBlocker.getInstance();

        final boolean actual = blocker.block(chain);

        assertThat(actual).isEqualTo(expected);
    }
}
