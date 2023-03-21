package se.ludvigwesterdahl.lib.cts.blockers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.ludvigwesterdahl.lib.cts.Blocker;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;
import se.ludvigwesterdahl.lib.cts.Identifier;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateLeaf;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateNode;

final class SimpleBlockerTest {

    private static Stream<Arguments> Should_DoNothing_When_EnterNode_Provider() {
        return Stream.of(
                Arguments.of("block node", SimpleBlocker.blockNode(Identifier.newInstance(Object.class))),
                Arguments.of("block leaf", SimpleBlocker.blockLeaf(Identifier.newInstance(Object.class))),
                Arguments.of("block", SimpleBlocker.block(Identifier.newInstance(Object.class)))
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_DoNothing_When_EnterNode_Provider")
    void Should_DoNothing_When_EnterNode(@SuppressWarnings("unused") final String description,
                                         final Blocker blocker) {
        final CtsFieldChain chain = mock(CtsFieldChain.class);

        blocker.enterNode(chain);

        verifyNoInteractions(chain);
    }

    private static Stream<Arguments> Should_DoNothing_When_ConsumeLeaf_Provider() {
        return Stream.of(
                Arguments.of("block node", SimpleBlocker.blockNode(Identifier.newInstance(Object.class))),
                Arguments.of("block leaf", SimpleBlocker.blockLeaf(Identifier.newInstance(Object.class))),
                Arguments.of("block", SimpleBlocker.block(Identifier.newInstance(Object.class)))
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_DoNothing_When_ConsumeLeaf_Provider")
    void Should_DoNothing_When_ConsumeLeaf(@SuppressWarnings("unused") final String description,
                                           final Blocker blocker) {
        final CtsFieldChain chain = mock(CtsFieldChain.class);

        blocker.consumeLeaf(chain);

        verifyNoInteractions(chain);
    }

    private static Stream<Arguments> Should_DoNothing_When_LeaveNode_Provider() {
        return Stream.of(
                Arguments.of("block node", SimpleBlocker.blockNode(Identifier.newInstance(Object.class))),
                Arguments.of("block leaf", SimpleBlocker.blockLeaf(Identifier.newInstance(Object.class))),
                Arguments.of("block", SimpleBlocker.block(Identifier.newInstance(Object.class)))
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_DoNothing_When_LeaveNode_Provider")
    void Should_DoNothing_When_LeaveNode(@SuppressWarnings("unused") final String description,
                                         final Blocker blocker) {
        final CtsFieldChain chain = mock(CtsFieldChain.class);

        blocker.leaveNode(chain);

        verifyNoInteractions(chain);
    }

    private static Stream<Arguments> Should_Return_When_Block_Provider() {
        final CtsFieldChain root = CtsFieldChain.newRootInstance(String.class);
        final CtsFieldChain leaf = appendPrivateLeaf(root, Object.class, "leaf");
        final CtsFieldChain node = appendPrivateNode(root, Object.class, "node");

        final Blocker leafBlocker = SimpleBlocker.blockLeaf(Identifier.newInstance(Object.class, "leaf"));
        final Blocker leafBlockerWithoutName = SimpleBlocker.blockLeaf(Identifier.newInstance(Object.class));
        final Blocker nodeBlocker = SimpleBlocker.blockNode(Identifier.newInstance(Object.class, "node"));
        final Blocker nodeBlockerWithoutName = SimpleBlocker.blockNode(Identifier.newInstance(Object.class));
        final Blocker blockerWithLeafName = SimpleBlocker.block(Identifier.newInstance(Object.class, "leaf"));
        final Blocker blockerWithNodeName = SimpleBlocker.block(Identifier.newInstance(Object.class, "node"));
        final Blocker blocker = SimpleBlocker.block(Identifier.newInstance(Object.class));

        return Stream.of(
                Arguments.of("blocking leaf with leaf blocker", leafBlocker, leaf, true),
                Arguments.of("blocking node with leaf blocker", leafBlocker, node, false),
                Arguments.of("blocking leaf with leaf blocker without name", leafBlockerWithoutName, leaf, true),
                Arguments.of("blocking node with leaf blocker without name", leafBlockerWithoutName, node, false),
                Arguments.of("blocking leaf with node blocker", nodeBlocker, leaf, false),
                Arguments.of("blocking node with node blocker", nodeBlocker, node, true),
                Arguments.of("blocking leaf with node blocker without name", nodeBlockerWithoutName, leaf, false),
                Arguments.of("blocking node with node blocker without name", nodeBlockerWithoutName, node, true),
                Arguments.of("blocking leaf with leaf or node blocker with leaf name",
                        blockerWithLeafName, leaf, true),
                Arguments.of("blocking node with leaf or node blocker with leaf name",
                        blockerWithLeafName, node, false),
                Arguments.of("blocking leaf with leaf or node blocker with node name",
                        blockerWithNodeName, leaf, false),
                Arguments.of("blocking node with leaf or node blocker with node name",
                        blockerWithNodeName, node, true),
                Arguments.of("blocking leaf with blocker", blocker, leaf, true),
                Arguments.of("blocking node with blocker", blocker, leaf, true)
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_Return_When_Block_Provider")
    void Should_Return_When_Block(@SuppressWarnings("unused") final String description,
                                  final Blocker blocker,
                                  final CtsFieldChain chain,
                                  final boolean expected) {
        final boolean actual = blocker.block(chain);

        assertThat(actual).isEqualTo(expected);
    }
}
