package se.ludvigwesterdahl.lib.fixture.ctstestcases;

import se.ludvigwesterdahl.lib.cts.ClassToStringGenerator;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;
import se.ludvigwesterdahl.lib.cts.Identifier;

import java.util.List;

import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateLeaf;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateNode;
import static se.ludvigwesterdahl.lib.fixture.GenerationStrategyFixture.newDefaultFlatGenerationStrategy;
import static se.ludvigwesterdahl.lib.fixture.ctstestcases.CtsNotification.Type.*;
import static se.ludvigwesterdahl.lib.fixture.ctstestcases.CtsNotification.notification;

public final class EmbedNodeWithExternalEmbeddings implements CtsTestCaseGroup {

    @SuppressWarnings("unused")
    private static final class Level0 {

        private Level0.Level1 next;

        private static final class Level1 {

            private String field;
        }
    }

    @SuppressWarnings("unused")
    private static final class External0 {

        private Integer number;
        private External0.External1 external;

        private static final class External1 {

            private Integer number;
        }
    }

    private static final class Default implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            final Identifier level1 = Identifier.newInstance(Level0.Level1.class);
            // Specifying the name "external" is not really required here.
            final Identifier external1 = Identifier.newInstance(External0.External1.class, "external");

            return ClassToStringGenerator.from(Level0.class)
                    .addNode(null, level1)
                    .addNode(null, external1)
                    .addEmbedding(Level0.class, level1)
                    .addExternalEmbedding(External0.class, level1)
                    .addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain root = CtsFieldChain.newRootInstance(Level0.class);
            final CtsFieldChain leaf1 = appendPrivateLeaf(root, String.class, "field");
            final CtsFieldChain leaf2 = appendPrivateLeaf(root, Integer.class, "number");
            final CtsFieldChain node1 = appendPrivateNode(root, External0.External1.class, "external");
            final CtsFieldChain leaf3 = appendPrivateLeaf(node1, Integer.class, "number");

            return List.of(
                    notification(ENTER_NODE, root),
                    notification(CONSUME_LEAF, leaf1),
                    notification(CONSUME_LEAF, leaf2),
                    notification(ENTER_NODE, node1),
                    notification(CONSUME_LEAF, leaf3),
                    notification(LEAVE_NODE, node1),
                    notification(LEAVE_NODE, root)
            );
        }

        @Override
        public String expectedGenerate() {
            return "field,number,external/number";
        }
    }

    private static final class ExternalEmbeddingHasEmbeddings implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            final Identifier level1 = Identifier.newInstance(Level0.Level1.class);
            // Specifying the name "external" is not really required here.
            final Identifier external1 = Identifier.newInstance(External0.External1.class, "external");

            return ClassToStringGenerator.from(Level0.class)
                    .addNode(Level0.class, level1)
                    .addNode(External0.class, external1)
                    .addEmbedding(Level0.class, level1)
                    .addExternalEmbedding(External0.class, level1)
                    .addEmbedding(External0.class, external1)
                    .addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain root = CtsFieldChain.newRootInstance(Level0.class);
            final CtsFieldChain leaf1 = appendPrivateLeaf(root, String.class, "field");
            final CtsFieldChain leaf2 = appendPrivateLeaf(root, Integer.class, "number");
            final CtsFieldChain leaf3 = appendPrivateLeaf(root, Integer.class, "number");

            return List.of(
                    notification(ENTER_NODE, root),
                    notification(CONSUME_LEAF, leaf1),
                    notification(CONSUME_LEAF, leaf2),
                    notification(CONSUME_LEAF, leaf3),
                    notification(LEAVE_NODE, root)
            );
        }

        @Override
        public String expectedGenerate() {
            return "field,number,number";
        }
    }

    @Override
    public List<CtsTestCase> testCases() {
        return List.of(
                new Default(),
                new ExternalEmbeddingHasEmbeddings()
        );
    }
}
