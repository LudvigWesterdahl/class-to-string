package se.ludvigwesterdahl.lib.fixture.ctstestcases;

import se.ludvigwesterdahl.lib.cts.*;

import java.util.List;

import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateLeaf;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateNode;
import static se.ludvigwesterdahl.lib.fixture.GenerationStrategyFixture.newDefaultFlatGenerationStrategy;
import static se.ludvigwesterdahl.lib.fixture.ctstestcases.CtsNotification.Type.*;
import static se.ludvigwesterdahl.lib.fixture.ctstestcases.CtsNotification.notification;

public final class RemoveRenameAnnotation implements CtsTestCaseGroup {

    @SuppressWarnings("unused")
    private static final class Root {

        @CtsNode
        @CtsName(name = "notFirst", type = Second.class)
        private First first;
        @CtsNode
        @CtsName(name = "notSecond", type = First.class)
        private Second second;
        @CtsNode
        @CtsName(name = "maybeThird")
        private Third third;

        private static final class First {

            @CtsName(name = "name1")
            private String string1;
        }

        private static final class Second {

            @CtsName(name = "name2")
            private String string2;
        }

        private static final class Third {

            @CtsName(name = "name3")
            private String string3;
        }
    }

    /**
     * This test case is just to show the default behaviour of the annotations
     * without removing anything by code.
     */
    private static final class Default implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(Root.class)
                    .addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain root = CtsFieldChain.newRootInstance(Root.class);
            final CtsFieldChain node1 = appendPrivateNode(root, Root.Second.class, "notFirst");
            final CtsFieldChain node2 = appendPrivateNode(root, Root.First.class, "notSecond");
            final CtsFieldChain node3 = appendPrivateNode(root, Root.Third.class, "maybeThird");
            final CtsFieldChain leaf1 = appendPrivateLeaf(node1, String.class, "name2");
            final CtsFieldChain leaf2 = appendPrivateLeaf(node2, String.class, "name1");
            final CtsFieldChain leaf3 = appendPrivateLeaf(node3, String.class, "name3");

            return List.of(
                    notification(ENTER_NODE, root),
                    notification(ENTER_NODE, node1),
                    notification(CONSUME_LEAF, leaf1),
                    notification(LEAVE_NODE, node1),
                    notification(ENTER_NODE, node2),
                    notification(CONSUME_LEAF, leaf2),
                    notification(LEAVE_NODE, node2),
                    notification(ENTER_NODE, node3),
                    notification(CONSUME_LEAF, leaf3),
                    notification(LEAVE_NODE, node3),
                    notification(LEAVE_NODE, root)
            );
        }

        @Override
        public String expectedGenerate() {
            return String.join(",",
                    "notFirst/name2",
                    "notSecond/name1",
                    "maybeThird/name3"
            );
        }
    }

    private static final class CleanedUpAnnotationsWithCode implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(Root.class)
                    .removeName(Root.class, Identifier.newInstance(Root.First.class, "first"))
                    .removeName(Root.class, Identifier.newInstance(Root.Second.class, "second"))
                    .removeName(Root.First.class, Identifier.newInstance(String.class, "string1"))
                    .removeName(Root.Second.class, Identifier.newInstance(String.class, "string2"))
                    .removeName(Root.class, Identifier.newInstance(Root.Third.class, "third"))
                    .removeNode(Root.class, Identifier.newInstance(Root.Third.class, "third"))
                    .addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain root = CtsFieldChain.newRootInstance(Root.class);
            final CtsFieldChain node1 = appendPrivateNode(root, Root.First.class, "first");
            final CtsFieldChain node2 = appendPrivateNode(root, Root.Second.class, "second");
            final CtsFieldChain leaf1 = appendPrivateLeaf(root, Root.Third.class, "third");
            final CtsFieldChain leaf2 = appendPrivateLeaf(node1, String.class, "string1");
            final CtsFieldChain leaf3 = appendPrivateLeaf(node2, String.class, "string2");

            return List.of(
                    notification(ENTER_NODE, root),
                    notification(ENTER_NODE, node1),
                    notification(CONSUME_LEAF, leaf2),
                    notification(LEAVE_NODE, node1),
                    notification(ENTER_NODE, node2),
                    notification(CONSUME_LEAF, leaf3),
                    notification(LEAVE_NODE, node2),
                    notification(CONSUME_LEAF, leaf1),
                    notification(LEAVE_NODE, root)
            );
        }

        @Override
        public String expectedGenerate() {
            return String.join(",",
                    "first/string1",
                    "second/string2",
                    "third"
            );
        }
    }

    @Override
    public List<CtsTestCase> testCases() {
        return List.of(
                new Default(),
                new CleanedUpAnnotationsWithCode()
        );
    }
}
