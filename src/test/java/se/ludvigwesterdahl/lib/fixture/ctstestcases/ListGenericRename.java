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

public final class ListGenericRename implements CtsTestCaseGroup {

    @SuppressWarnings("unused")
    private static final class Root {

        private First first;
        private Second second;

        private static final class First {

            private List<FirstResult> results;

            private static final class FirstResult {

                private String firstValue;
            }
        }

        private static final class Second {

            private List<SecondResult> results;

            private static final class SecondResult {

                private String secondValue;
            }
        }
    }

    private static final class ListDefaultRename implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            // Because this specifies without a node, then both will get the same rename.
            final ClassToStringGenerator generator = ClassToStringGenerator.from(Root.class)
                    .addNode(Root.class, Identifier.newInstance(Root.First.class))
                    .addNode(Root.class, Identifier.newInstance(Root.Second.class))
                    .addName(Identifier.newInstance(List.class, "results"),
                            Identifier.newInstance(Root.First.FirstResult.class, "firstResultsRenamed"))
                    .addObserver(newDefaultFlatGenerationStrategy());

            // Adding that node as a general node so that the renamed node will also be a node
            // and traversed down.
            generator.addNode(null, Identifier.newInstance(Root.First.FirstResult.class));

            return generator;
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain root = CtsFieldChain.newRootInstance(Root.class);

            final CtsFieldChain node1 = appendPrivateNode(root, Root.First.class, "first");
            final CtsFieldChain node2 = appendPrivateNode(node1, Root.First.FirstResult.class, "firstResultsRenamed");
            final CtsFieldChain leaf1 = appendPrivateLeaf(node2, String.class, "firstValue");

            final CtsFieldChain node3 = appendPrivateNode(root, Root.Second.class, "second");
            final CtsFieldChain node4 = appendPrivateNode(node3, Root.First.FirstResult.class, "firstResultsRenamed");
            final CtsFieldChain leaf2 = appendPrivateLeaf(node4, String.class, "firstValue");

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
            return "first/firstResultsRenamed/firstValue,second/firstResultsRenamed/firstValue";
        }
    }

    private static final class ListSpecificRename implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            // This specifies both as a specific rename and will therefore get different rename.
            return ClassToStringGenerator.from(Root.class)
                    .addNode(Root.class, Identifier.newInstance(Root.First.class))
                    .addNode(Root.First.class, Identifier.newInstance(Root.First.FirstResult.class))
                    .addNode(Root.class, Identifier.newInstance(Root.Second.class))
                    .addNode(Root.Second.class, Identifier.newInstance(Root.Second.SecondResult.class))
                    .addName(Root.First.class,
                            Identifier.newInstance(List.class, "results"),
                            Identifier.newInstance(Root.First.FirstResult.class, "firstResultsRenamed"))
                    .addName(Root.Second.class,
                            Identifier.newInstance(List.class, "results"),
                            Identifier.newInstance(Root.Second.SecondResult.class, "secondResultsRenamed"))
                    .addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain root = CtsFieldChain.newRootInstance(Root.class);

            final CtsFieldChain node1 = appendPrivateNode(root, Root.First.class, "first");
            final CtsFieldChain node2
                    = appendPrivateNode(node1, Root.First.FirstResult.class, "firstResultsRenamed");
            final CtsFieldChain leaf1 = appendPrivateLeaf(node2, String.class, "firstValue");

            final CtsFieldChain node3 = appendPrivateNode(root, Root.Second.class, "second");
            final CtsFieldChain node4
                    = appendPrivateNode(node3, Root.Second.SecondResult.class, "secondResultsRenamed");
            final CtsFieldChain leaf5 = appendPrivateLeaf(node4, String.class, "secondValue");

            return List.of(
                    notification(ENTER_NODE, root),
                    notification(ENTER_NODE, node1),
                    notification(ENTER_NODE, node2),
                    notification(CONSUME_LEAF, leaf1),
                    notification(LEAVE_NODE, node2),
                    notification(LEAVE_NODE, node1),
                    notification(ENTER_NODE, node3),
                    notification(ENTER_NODE, node4),
                    notification(CONSUME_LEAF, leaf5),
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

    /**
     * This test case simply builds on {@link ListSpecificRename} but adds a
     * global list rename as well which should be ignored.
     */
    private static final class ListGlobalAndSpecificRename implements CtsTestCase {

        private final CtsTestCase listSpecificRename = new ListSpecificRename();

        @Override
        public ClassToStringGenerator generator() {
            return listSpecificRename.generator()
                    .addName(Identifier.newInstance(List.class, "results"),
                            Identifier.newInstance(Root.First.FirstResult.class, "firstResultsRenamed"));
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            return listSpecificRename.expectedNotifications();
        }

        @Override
        public String expectedGenerate() {
            return listSpecificRename.expectedGenerate();
        }
    }

    /**
     * This test case is similar to {@link ListSpecificRename} but uses a global rename
     * for the first one, but a specific for the second.
     */
    private static final class ListGlobalAndOneSpecificRename implements CtsTestCase {

        private final CtsTestCase listSpecificRename = new ListSpecificRename();

        @Override
        public ClassToStringGenerator generator() {
            return listSpecificRename.generator()
                    .removeName(Root.First.class, Identifier.newInstance(List.class, "results"))
                    .addName(Identifier.newInstance(List.class, "results"),
                            Identifier.newInstance(Root.First.FirstResult.class, "firstResultsRenamed"));
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            return listSpecificRename.expectedNotifications();
        }

        @Override
        public String expectedGenerate() {
            return listSpecificRename.expectedGenerate();
        }
    }

    @Override
    public List<CtsTestCase> testCases() {
        return List.of(
                new ListDefaultRename(),
                new ListSpecificRename(),
                new ListGlobalAndSpecificRename(),
                new ListGlobalAndOneSpecificRename()
        );
    }
}
