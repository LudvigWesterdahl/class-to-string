package se.ludvigwesterdahl.lib.fixture.ctstestcases;

import se.ludvigwesterdahl.lib.cts.*;

import java.util.List;

import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateLeaf;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateNode;
import static se.ludvigwesterdahl.lib.fixture.GenerationStrategyFixture.newDefaultFlatGenerationStrategy;
import static se.ludvigwesterdahl.lib.fixture.ctstestcases.CtsNotification.Type.*;
import static se.ludvigwesterdahl.lib.fixture.ctstestcases.CtsNotification.notification;

public final class SimpleListSameErasure implements CtsTestCaseGroup {

    /**
     * The class structure used in the following test cases.
     */
    @SuppressWarnings("unused")
    private static final class Simple {

        @CtsNode
        private First first;
        @CtsNode
        private Second second;

        private static final class First {

            @CtsNode
            @CtsName(type = Result.class)
            private List<String> results;

            private static final class Result {

                private String field1;
            }
        }

        private static final class Second {

            @CtsNode
            @CtsName(type = Result.class)
            private List<String> results;

            private static final class Result {

                private String field2;
            }
        }
    }

    private static final class Default implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(Simple.class)
                    .addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain root = CtsFieldChain.newRootInstance(Simple.class);
            final CtsFieldChain node1 = appendPrivateNode(root, Simple.First.class, "first");
            final CtsFieldChain node2 = appendPrivateNode(root, Simple.Second.class, "second");
            final CtsFieldChain node3 = appendPrivateNode(node1, Simple.First.Result.class, "results");
            final CtsFieldChain node4 = appendPrivateNode(node2, Simple.Second.Result.class, "results");
            final CtsFieldChain leaf1 = appendPrivateLeaf(node3, String.class, "field1");
            final CtsFieldChain leaf2 = appendPrivateLeaf(node4, String.class, "field2");

            return List.of(
                    notification(ENTER_NODE, root),
                    notification(ENTER_NODE, node1),
                    notification(ENTER_NODE, node3),
                    notification(CONSUME_LEAF, leaf1),
                    notification(LEAVE_NODE, node3),
                    notification(LEAVE_NODE, node1),
                    notification(ENTER_NODE, node2),
                    notification(ENTER_NODE, node4),
                    notification(CONSUME_LEAF, leaf2),
                    notification(LEAVE_NODE, node4),
                    notification(LEAVE_NODE, node2),
                    notification(LEAVE_NODE, root)
            );
        }

        @Override
        public String expectedGenerate() {
            return "first/results/field1,second/results/field2";
        }
    }

    private static final class OnlyTraverseFirst implements CtsTestCase {

        private final CtsTestCase defaultTestCase = new Default();

        @Override
        public ClassToStringGenerator generator() {
            return defaultTestCase.generator()
                    .removeNode(Simple.Second.class, Identifier.newInstance(List.class, "results"));
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain root = CtsFieldChain.newRootInstance(Simple.class);
            final CtsFieldChain node1 = appendPrivateNode(root, Simple.First.class, "first");
            final CtsFieldChain node2 = appendPrivateNode(root, Simple.Second.class, "second");
            final CtsFieldChain node3 = appendPrivateNode(node1, Simple.First.Result.class, "results");
            final CtsFieldChain leaf1 = appendPrivateLeaf(node3, String.class, "field1");
            final CtsFieldChain leaf2 = appendPrivateLeaf(node2, Simple.Second.Result.class, "results");

            return List.of(
                    notification(ENTER_NODE, root),
                    notification(ENTER_NODE, node1),
                    notification(ENTER_NODE, node3),
                    notification(CONSUME_LEAF, leaf1),
                    notification(LEAVE_NODE, node3),
                    notification(LEAVE_NODE, node1),
                    notification(ENTER_NODE, node2),
                    notification(CONSUME_LEAF, leaf2),
                    notification(LEAVE_NODE, node2),
                    notification(LEAVE_NODE, root)
            );
        }

        @Override
        public String expectedGenerate() {
            return "first/results/field1,second/results";
        }
    }

    /**
     * Because the list does not have any declared fields, nothing will be found.
     */
    private static final class OnlyTraverseFirstAndSkipList implements CtsTestCase {

        private final CtsTestCase defaultTestCase = new Default();

        @Override
        public ClassToStringGenerator generator() {
            return defaultTestCase.generator()
                    .removeName(Simple.Second.class, Identifier.newInstance(List.class, "results"));
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain root = CtsFieldChain.newRootInstance(Simple.class);
            final CtsFieldChain node1 = appendPrivateNode(root, Simple.First.class, "first");
            final CtsFieldChain node2 = appendPrivateNode(root, Simple.Second.class, "second");
            final CtsFieldChain node3 = appendPrivateNode(node1, Simple.First.Result.class, "results");
            final CtsFieldChain node4 = appendPrivateNode(node2, List.class, "results");
            final CtsFieldChain leaf1 = appendPrivateLeaf(node3, String.class, "field1");

            return List.of(
                    notification(ENTER_NODE, root),
                    notification(ENTER_NODE, node1),
                    notification(ENTER_NODE, node3),
                    notification(CONSUME_LEAF, leaf1),
                    notification(LEAVE_NODE, node3),
                    notification(LEAVE_NODE, node1),
                    notification(ENTER_NODE, node2),
                    notification(ENTER_NODE, node4),
                    notification(LEAVE_NODE, node4),
                    notification(LEAVE_NODE, node2),
                    notification(LEAVE_NODE, root)
            );
        }

        @Override
        public String expectedGenerate() {
            return "first/results/field1";
        }
    }

    @Override
    public List<CtsTestCase> testCases() {
        return List.of(
                new Default(),
                new OnlyTraverseFirst(),
                new OnlyTraverseFirstAndSkipList()
        );
    }
}
