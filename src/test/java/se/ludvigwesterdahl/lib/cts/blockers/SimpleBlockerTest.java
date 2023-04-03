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
                Arguments.of("block node", SimpleBlocker.blockNode(null, Identifier.newInstance(Object.class))),
                Arguments.of("block leaf", SimpleBlocker.blockLeaf(null, Identifier.newInstance(Object.class))),
                Arguments.of("block", SimpleBlocker.block(null, Identifier.newInstance(Object.class)))
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
                Arguments.of("block node", SimpleBlocker.blockNode(null, Identifier.newInstance(Object.class))),
                Arguments.of("block leaf", SimpleBlocker.blockLeaf(null, Identifier.newInstance(Object.class))),
                Arguments.of("block", SimpleBlocker.block(null, Identifier.newInstance(Object.class)))
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
                Arguments.of("block node", SimpleBlocker.blockNode(null, Identifier.newInstance(Object.class))),
                Arguments.of("block leaf", SimpleBlocker.blockLeaf(null, Identifier.newInstance(Object.class))),
                Arguments.of("block", SimpleBlocker.block(null, Identifier.newInstance(Object.class)))
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

        final Blocker leafBlocker = SimpleBlocker.blockLeaf(null, Identifier.newInstance(Object.class, "leaf"));
        final Blocker leafBlockerWithoutName = SimpleBlocker.blockLeaf(null, Identifier.newInstance(Object.class));
        final Blocker nodeBlocker = SimpleBlocker.blockNode(null, Identifier.newInstance(Object.class, "node"));
        final Blocker nodeBlockerWithoutName = SimpleBlocker.blockNode(null, Identifier.newInstance(Object.class));
        final Blocker blockerWithLeafName = SimpleBlocker.block(null, Identifier.newInstance(Object.class, "leaf"));
        final Blocker blockerWithNodeName = SimpleBlocker.block(null, Identifier.newInstance(Object.class, "node"));
        final Blocker blocker = SimpleBlocker.block(null, Identifier.newInstance(Object.class));

        final Identifier parentNode = root.head().getIdentifier();
        final Blocker leafBlockerForNode
                = SimpleBlocker.blockLeaf(parentNode, Identifier.newInstance(Object.class, "leaf"));
        final Blocker leafBlockerWithoutNameForNode
                = SimpleBlocker.blockLeaf(parentNode, Identifier.newInstance(Object.class));
        final Blocker nodeBlockerForNode
                = SimpleBlocker.blockNode(parentNode, Identifier.newInstance(Object.class, "node"));
        final Blocker nodeBlockerWithoutNameForNode
                = SimpleBlocker.blockNode(parentNode, Identifier.newInstance(Object.class));
        final Blocker blockerWithLeafNameForNode
                = SimpleBlocker.block(parentNode, Identifier.newInstance(Object.class, "leaf"));
        final Blocker blockerWithNodeNameForNode
                = SimpleBlocker.block(parentNode, Identifier.newInstance(Object.class, "node"));
        final Blocker blockerForNode
                = SimpleBlocker.block(parentNode, Identifier.newInstance(Object.class));

        final Blocker invalidParentLeafBlocker
                = SimpleBlocker.blockLeaf(Identifier.newInstance(Integer.class),
                Identifier.newInstance(Object.class, "leaf"));
        final Blocker invalidParentNodeBlocker
                = SimpleBlocker.blockNode(Identifier.newInstance(Integer.class),
                Identifier.newInstance(Object.class, "node"));

        final Blocker rootBlocker
                = SimpleBlocker.block(null, parentNode);

        return Stream.of(
                Arguments.of("blocking leaf with leaf blocker", leafBlocker, leaf, true),
                Arguments.of("blocking node with leaf blocker", leafBlocker, node, false),
                Arguments.of("blocking leaf with leaf blocker without name",
                        leafBlockerWithoutName, leaf, true),
                Arguments.of("blocking node with leaf blocker without name",
                        leafBlockerWithoutName, node, false),
                Arguments.of("blocking leaf with node blocker", nodeBlocker, leaf, false),
                Arguments.of("blocking node with node blocker", nodeBlocker, node, true),
                Arguments.of("blocking leaf with node blocker without name",
                        nodeBlockerWithoutName, leaf, false),
                Arguments.of("blocking node with node blocker without name",
                        nodeBlockerWithoutName, node, true),
                Arguments.of("blocking leaf with leaf or node blocker with leaf name",
                        blockerWithLeafName, leaf, true),
                Arguments.of("blocking node with leaf or node blocker with leaf name",
                        blockerWithLeafName, node, false),
                Arguments.of("blocking leaf with leaf or node blocker with node name",
                        blockerWithNodeName, leaf, false),
                Arguments.of("blocking node with leaf or node blocker with node name",
                        blockerWithNodeName, node, true),
                Arguments.of("blocking leaf with blocker", blocker, leaf, true),
                Arguments.of("blocking node with blocker", blocker, leaf, true),

                Arguments.of("blocking leaf with leaf blocker for node", leafBlockerForNode, leaf, true),
                Arguments.of("blocking node with leaf blocker for node", leafBlockerForNode, node, false),
                Arguments.of("blocking leaf with leaf blocker without name for node",
                        leafBlockerWithoutNameForNode, leaf, true),
                Arguments.of("blocking node with leaf blocker without name for node",
                        leafBlockerWithoutNameForNode, node, false),
                Arguments.of("blocking leaf with node blocker for node for node", nodeBlockerForNode, leaf, false),
                Arguments.of("blocking node with node blocker for node", nodeBlockerForNode, node, true),
                Arguments.of("blocking leaf with node blocker without name for node",
                        nodeBlockerWithoutNameForNode, leaf, false),
                Arguments.of("blocking node with node blocker without name for node",
                        nodeBlockerWithoutNameForNode, node, true),
                Arguments.of("blocking leaf with leaf or node blocker with leaf name for node",
                        blockerWithLeafNameForNode, leaf, true),
                Arguments.of("blocking node with leaf or node blocker with leaf name for node",
                        blockerWithLeafNameForNode, node, false),
                Arguments.of("blocking leaf with leaf or node blocker with node name for node",
                        blockerWithNodeNameForNode, leaf, false),
                Arguments.of("blocking node with leaf or node blocker with node name for node",
                        blockerWithNodeNameForNode, node, true),
                Arguments.of("blocking leaf with blocker for node", blockerForNode, leaf, true),
                Arguments.of("blocking node with blocker for node", blockerForNode, leaf, true),

                Arguments.of("trying to block leaf bad parent", invalidParentLeafBlocker, leaf, false),
                Arguments.of("trying to block node with bad parent", invalidParentNodeBlocker, node, false),

                Arguments.of("blocking root", rootBlocker, root, true)
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
