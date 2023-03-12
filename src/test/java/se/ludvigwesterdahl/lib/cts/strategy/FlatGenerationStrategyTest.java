package se.ludvigwesterdahl.lib.cts.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateLeaf;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateNode;

final class FlatGenerationStrategyTest {

    @Test
    void Should_HaveDefaultConfiguration_When_BuilderIsNotModified() {
        final FlatGenerationStrategy expectedStrategy = new FlatGenerationStrategy.Builder()
                .withPathSeparator(",")
                .withLevelMarker("/")
                .withNodes(false)
                .withLeaf(true)
                .build();
        final String expectedPathSeparator = expectedStrategy.getPathSeparator();
        final String expectedLevelMarker = expectedStrategy.getLevelMarker();
        final boolean expectedNodes = expectedStrategy.isNodes();
        final boolean expectedLeaf = expectedStrategy.isLeaf();

        final FlatGenerationStrategy strategy = new FlatGenerationStrategy.Builder()
                .build();

        final String actualPathSeparator = strategy.getPathSeparator();
        final String actualLevelMarker = strategy.getLevelMarker();
        final boolean actualNodes = strategy.isNodes();
        final boolean actualLeaf = strategy.isLeaf();
        assertThat(actualPathSeparator).isEqualTo(expectedPathSeparator);
        assertThat(actualLevelMarker).isEqualTo(expectedLevelMarker);
        assertThat(actualNodes).isEqualTo(expectedNodes);
        assertThat(actualLeaf).isEqualTo(expectedLeaf);
    }

    private static Stream<Arguments> Should_Generate_When_ConfiguredNodesAndLeaf_Provider() {
        return Stream.of(
                Arguments.of("nodes and leaf", true, true,
                        "node1,node1/leaf1,node1/leaf2,node2,node2/leaf3,node2/leaf4"),
                Arguments.of("not nodes and leaf", false, true,
                        "node1/leaf1,node1/leaf2,node2/leaf3,node2/leaf4"),
                Arguments.of("nodes and not leaf", true, false,
                        "node1,node2"),
                Arguments.of("not nodes and not leaf", false, false, "")
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("Should_Generate_When_ConfiguredNodesAndLeaf_Provider")
    void Should_Generate_When_ConfiguredNodesAndLeaf(@SuppressWarnings("unused") final String description,
                                                     final boolean nodes,
                                                     final boolean leaf,
                                                     final String expected) {
        final CtsFieldChain root = CtsFieldChain.newRootInstance(Object.class);
        final CtsFieldChain node1 = appendPrivateNode(root, Object.class, "node1");
        final CtsFieldChain leaf1 = appendPrivateLeaf(node1, String.class, "leaf1");
        final CtsFieldChain leaf2 = appendPrivateLeaf(node1, String.class, "leaf2");
        final CtsFieldChain node2 = appendPrivateNode(root, Object.class, "node2");
        final CtsFieldChain leaf3 = appendPrivateLeaf(node2, String.class, "leaf3");
        final CtsFieldChain leaf4 = appendPrivateLeaf(node2, String.class, "leaf4");
        final FlatGenerationStrategy strategy = new FlatGenerationStrategy.Builder()
                .withNodes(nodes)
                .withLeaf(leaf)
                .build();
        strategy.enterNode(root);
        strategy.enterNode(node1);
        strategy.consumeLeaf(leaf1);
        strategy.consumeLeaf(leaf2);
        strategy.leaveNode(node1);
        strategy.enterNode(node2);
        strategy.consumeLeaf(leaf3);
        strategy.consumeLeaf(leaf4);
        strategy.leaveNode(node2);
        strategy.leaveNode(root);

        final String actual = strategy.generate();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void Should_GenerateSameString_When_GivenNoLeafAndWithNodes() {
        final CtsFieldChain root = CtsFieldChain.newRootInstance(Object.class);
        final CtsFieldChain node1 = appendPrivateNode(root, Object.class, "node1");
        final CtsFieldChain leaf1 = appendPrivateLeaf(node1, String.class, "leaf1");
        final CtsFieldChain leaf2 = appendPrivateLeaf(node1, String.class, "leaf2");
        final CtsFieldChain node2 = appendPrivateNode(root, Object.class, "node2");
        final CtsFieldChain leaf3 = appendPrivateLeaf(node2, String.class, "leaf3");
        final CtsFieldChain leaf4 = appendPrivateLeaf(node2, String.class, "leaf4");
        final FlatGenerationStrategy expectedStrategy = new FlatGenerationStrategy.Builder()
                .withNodes(true)
                .withLeaf(true)
                .build();
        expectedStrategy.enterNode(root);
        expectedStrategy.enterNode(node1);
        expectedStrategy.leaveNode(node1);
        expectedStrategy.enterNode(node2);
        expectedStrategy.leaveNode(node2);
        expectedStrategy.leaveNode(root);
        final String expected = expectedStrategy.generate();
        final FlatGenerationStrategy actualStrategy = new FlatGenerationStrategy.Builder()
                .withNodes(true)
                .withLeaf(false)
                .build();
        actualStrategy.enterNode(root);
        actualStrategy.enterNode(node1);
        actualStrategy.consumeLeaf(leaf1);
        actualStrategy.consumeLeaf(leaf2);
        actualStrategy.leaveNode(node1);
        actualStrategy.enterNode(node2);
        actualStrategy.consumeLeaf(leaf3);
        actualStrategy.consumeLeaf(leaf4);
        actualStrategy.leaveNode(node2);
        actualStrategy.leaveNode(root);

        final String actual = actualStrategy.generate();

        assertThat(actual).isEqualTo(expected);
    }
}
