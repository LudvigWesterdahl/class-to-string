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

public final class AddRemoveNameWithCode implements CtsTestCaseGroup {

    /**
     * The class structure used in the following test cases.
     */
    @SuppressWarnings("unused")
    private static final class Simple {

        private String field1;
        private String field2;
        private Inner inner;

        private static final class Inner {

            private String field1;
            private String field2;
        }
    }

    private static final class Default implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            // Since the more specific name will be used first.
            // That name is also globally set.
            // That means that "field1" will get the "first" name.
            // And the second string "field2" will get named "second".
            return ClassToStringGenerator.from(Simple.class)
                    .addName(Identifier.newInstance(String.class, "field1"),
                            Identifier.newInstance(String.class, "first"))
                    .addName(Simple.class,
                            Identifier.newInstance(String.class),
                            Identifier.newInstance(String.class, "second"))
                    .addNode(Simple.class, Identifier.newInstance(Simple.Inner.class))
                    .addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain root = CtsFieldChain.newRootInstance(Simple.class);
            final CtsFieldChain leaf1 = appendPrivateLeaf(root, String.class, "first");
            final CtsFieldChain leaf2 = appendPrivateLeaf(root, String.class, "second");
            final CtsFieldChain node1 = appendPrivateNode(root, Simple.Inner.class, "inner");
            final CtsFieldChain leaf3 = appendPrivateLeaf(node1, String.class, "first");
            final CtsFieldChain leaf4 = appendPrivateLeaf(node1, String.class, "field2");

            return List.of(
                    notification(ENTER_NODE, root),
                    notification(CONSUME_LEAF, leaf1),
                    notification(CONSUME_LEAF, leaf2),
                    notification(ENTER_NODE, node1),
                    notification(CONSUME_LEAF, leaf3),
                    notification(CONSUME_LEAF, leaf4),
                    notification(LEAVE_NODE, node1),
                    notification(LEAVE_NODE, root)
            );
        }

        @Override
        public String expectedGenerate() {
            return "first,second,inner/first,inner/field2";
        }
    }

    private static final class AddRemove implements CtsTestCase {

        private final CtsTestCase defaultTestCase = new Default();

        @Override
        public ClassToStringGenerator generator() {
            return defaultTestCase.generator()
                    .removeName(Identifier.newInstance(String.class, "field1"))
                    .removeName(Simple.class, Identifier.newInstance(String.class));
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain root = CtsFieldChain.newRootInstance(Simple.class);
            final CtsFieldChain leaf1 = appendPrivateLeaf(root, String.class, "field1");
            final CtsFieldChain leaf2 = appendPrivateLeaf(root, String.class, "field2");
            final CtsFieldChain node1 = appendPrivateNode(root, Simple.Inner.class, "inner");
            final CtsFieldChain leaf3 = appendPrivateLeaf(node1, String.class, "field1");
            final CtsFieldChain leaf4 = appendPrivateLeaf(node1, String.class, "field2");

            return List.of(
                    notification(ENTER_NODE, root),
                    notification(CONSUME_LEAF, leaf1),
                    notification(CONSUME_LEAF, leaf2),
                    notification(ENTER_NODE, node1),
                    notification(CONSUME_LEAF, leaf3),
                    notification(CONSUME_LEAF, leaf4),
                    notification(LEAVE_NODE, node1),
                    notification(LEAVE_NODE, root)
            );
        }

        @Override
        public String expectedGenerate() {
            return "field1,field2,inner/field1,inner/field2";
        }
    }

    @Override
    public List<CtsTestCase> testCases() {
        return List.of(
                new Default(),
                new AddRemove()
        );
    }
}
