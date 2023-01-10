package se.ludvigwesterdahl.lib.fixture.ctstestcases;

import se.ludvigwesterdahl.lib.cts.ClassToStringGenerator;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;
import se.ludvigwesterdahl.lib.cts.Identifier;
import se.ludvigwesterdahl.lib.fixture.BlockerFixture;

import java.util.List;

import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateLeaf;
import static se.ludvigwesterdahl.lib.fixture.GenerationStrategyFixture.newDefaultFlatGenerationStrategy;
import static se.ludvigwesterdahl.lib.fixture.ctstestcases.CtsNotification.Type.*;
import static se.ludvigwesterdahl.lib.fixture.ctstestcases.CtsNotification.notification;

public final class EmbedBeforeBlocker implements CtsTestCaseGroup {

    @SuppressWarnings("unused")
    private static final class Level0 {

        private Level1 next;

        private static final class Level1 {

            private Level2 next;

            private static final class Level2 {

                private String field;
            }
        }
    }

    @SuppressWarnings("unused")
    private static final class External0 {

        private Integer number;
        private External1 external;

        private static final class External1 {

            private Integer number;
        }
    }

    private static final class BlockAllNodes implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(Level0.class)
                    .addNode(Identifier.newInstance(Level0.Level1.class))
                    .addNode(Identifier.newInstance(Level0.Level1.Level2.class))
                    .embed(Identifier.newInstance(Level0.Level1.class))
                    .embed(Identifier.newInstance(Level0.Level1.Level2.class))
                    .addBlocker(BlockerFixture.blockAllNodesExceptRoot())
                    .addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain root = CtsFieldChain.newRootInstance(Level0.class);
            final CtsFieldChain leaf = appendPrivateLeaf(root, String.class, "field");

            return List.of(
                    notification(ENTER_NODE, root),
                    notification(CONSUME_LEAF, leaf),
                    notification(LEAVE_NODE, root)
            );
        }

        @Override
        public String expectedGenerate() {
            return "field";
        }
    }

    @Override
    public List<CtsTestCase> testCases() {
        return List.of(
                new BlockAllNodes()
        );
    }
}
