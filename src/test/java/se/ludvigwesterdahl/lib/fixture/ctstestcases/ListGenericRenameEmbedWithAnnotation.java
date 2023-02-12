package se.ludvigwesterdahl.lib.fixture.ctstestcases;

import se.ludvigwesterdahl.lib.cts.*;

import java.util.List;

import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateLeaf;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateNode;
import static se.ludvigwesterdahl.lib.fixture.GenerationStrategyFixture.newDefaultFlatGenerationStrategy;
import static se.ludvigwesterdahl.lib.fixture.ctstestcases.CtsNotification.Type.*;
import static se.ludvigwesterdahl.lib.fixture.ctstestcases.CtsNotification.notification;

public final class ListGenericRenameEmbedWithAnnotation implements CtsTestCaseGroup {

    @SuppressWarnings("unused")
    private static final class Root {

        @CtsNode
        private First first;
        @CtsNode
        private Second second;

        private static final class First {

            @CtsNode
            @CtsName(type = FirstResult.class, name = "firstResultsRenamed")
            private List<FirstResult> results;

            private static final class FirstResult {

                private String firstValue;
            }
        }

        private static final class Second {

            @CtsNode
            @CtsName(type = SecondResult.class, name = "secondResultsRenamed")
            private List<SecondResult> results;

            private static final class SecondResult {

                private String secondValue;
            }
        }
    }

    private static final class ListDefaultRename implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(Root.class)
                    .addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain root = CtsFieldChain.newRootInstance(Root.class);

            final CtsFieldChain node1 = appendPrivateNode(root, Root.First.class, "first");
            final CtsFieldChain node2 = appendPrivateNode(node1, Root.First.FirstResult.class, "firstResultsRenamed");
            final CtsFieldChain leaf1 = appendPrivateLeaf(node2, String.class, "firstValue");

            final CtsFieldChain node3 = appendPrivateNode(root, Root.Second.class, "second");
            final CtsFieldChain node4 = appendPrivateNode(node3, Root.Second.SecondResult.class, "secondResultsRenamed");
            final CtsFieldChain leaf2 = appendPrivateLeaf(node4, String.class, "secondValue");

            return List.of(
                    notification(ENTER_NODE, root),
                    notification(ENTER_NODE, node1),
                    notification(ENTER_NODE, node2),
                    notification(CONSUME_LEAF, leaf1),
                    notification(LEAVE_NODE, node2),
                    notification(LEAVE_NODE, node1),
                    notification(ENTER_NODE, node3),
                    notification(ENTER_NODE, node4),
                    notification(CONSUME_LEAF, leaf2),
                    notification(LEAVE_NODE, node4),
                    notification(LEAVE_NODE, node3),
                    notification(LEAVE_NODE, root)
            );
        }

        @Override
        public String expectedGenerate() {
            return "first/firstResultsRenamed/firstValue,second/secondResultsRenamed/secondValue";
        }
    }

    private static final class EmbedWithCode implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(Root.class)
                    .embed(Identifier.newInstance(Root.First.FirstResult.class, "firstResultsRenamed"))
                    .embed(Identifier.newInstance(Root.Second.SecondResult.class, "secondResultsRenamed"))
                    .addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain root = CtsFieldChain.newRootInstance(Root.class);

            final CtsFieldChain node1 = appendPrivateNode(root, Root.First.class, "first");
            final CtsFieldChain leaf1 = appendPrivateLeaf(node1, String.class, "firstValue");

            final CtsFieldChain node2 = appendPrivateNode(root, Root.Second.class, "second");
            final CtsFieldChain leaf2 = appendPrivateLeaf(node2, String.class, "secondValue");

            return List.of(
                    notification(ENTER_NODE, root),
                    notification(ENTER_NODE, node1),
                    notification(CONSUME_LEAF, leaf1),
                    notification(LEAVE_NODE, node1),
                    notification(ENTER_NODE, node2),
                    notification(CONSUME_LEAF, leaf2),
                    notification(LEAVE_NODE, node2),
                    notification(LEAVE_NODE, root)
            );
        }

        @Override
        public String expectedGenerate() {
            return "first/firstValue,second/secondValue";
        }
    }

    private static final class ClearAllNodeAnnotations implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(Root.class)
                    .removeNode(Identifier.newInstance(Root.First.class))
                    .removeNode(Identifier.newInstance(Root.Second.class))
                    .addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain root = CtsFieldChain.newRootInstance(Root.class);

            final CtsFieldChain leaf1 = appendPrivateNode(root, Root.First.class, "first");
            final CtsFieldChain leaf2 = appendPrivateNode(root, Root.Second.class, "second");

            return List.of(
                    notification(ENTER_NODE, root),
                    notification(CONSUME_LEAF, leaf1),
                    notification(CONSUME_LEAF, leaf2),
                    notification(LEAVE_NODE, root)
            );
        }

        @Override
        public String expectedGenerate() {
            return "first,second";
        }
    }

    @Override
    public List<CtsTestCase> testCases() {
        return List.of(
                new ListDefaultRename(),
                new EmbedWithCode(),
                new ClearAllNodeAnnotations()
        );
    }
}
