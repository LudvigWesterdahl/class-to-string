package se.ludvigwesterdahl.lib.fixture.ctstestcases;

import se.ludvigwesterdahl.lib.cts.ClassToStringGenerator;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;
import se.ludvigwesterdahl.lib.cts.Identifier;
import se.ludvigwesterdahl.lib.cts.blocker.LoopBlocker;

import java.time.LocalDate;
import java.util.List;

import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateLeaf;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateNode;
import static se.ludvigwesterdahl.lib.fixture.GenerationStrategyFixture.newDefaultFlatGenerationStrategy;
import static se.ludvigwesterdahl.lib.fixture.ctstestcases.CtsNotification.Type.*;
import static se.ludvigwesterdahl.lib.fixture.ctstestcases.CtsNotification.notification;

public final class SimpleStructureWithCode implements CtsTestCaseGroup {

    /**
     * The class structure used in the following test cases.
     */
    @SuppressWarnings("unused")
    private static final class Simple {

        private String field1;
        private Inner inner;

        private static final class Inner {

            private String field1Inner;
            private String field2Inner;
        }
    }

    private static final class SimpleClassStructure implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(Simple.class)
                    .addNode(Simple.class, Identifier.newInstance(Simple.Inner.class))
                    .addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain node1 = CtsFieldChain.newRootInstance(Simple.class);
            final CtsFieldChain leaf1 = appendPrivateLeaf(node1, String.class, "field1");
            final CtsFieldChain node2 = appendPrivateNode(node1, Simple.Inner.class, "inner");
            final CtsFieldChain leaf2 = appendPrivateLeaf(node2, String.class, "field1Inner");
            final CtsFieldChain leaf3 = appendPrivateLeaf(node2, String.class, "field2Inner");

            return List.of(
                    notification(ENTER_NODE, node1),
                    notification(CONSUME_LEAF, leaf1),
                    notification(ENTER_NODE, node2),
                    notification(CONSUME_LEAF, leaf2),
                    notification(CONSUME_LEAF, leaf3),
                    notification(LEAVE_NODE, node2),
                    notification(LEAVE_NODE, node1)
            );
        }

        @Override
        public String expectedGenerate() {
            return "field1,inner/field1Inner,inner/field2Inner";
        }
    }

    private static final class SimpleClassStructureWithRenaming implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            final Class<?> type = Simple.Inner.class;

            return ClassToStringGenerator.from(Simple.class)
                    .addName(Identifier.newInstance(type, "inner"),
                            Identifier.newInstance(type, "newInner"))
                    .addNode(Simple.class, Identifier.newInstance(type))
                    .addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain node1 = CtsFieldChain.newRootInstance(Simple.class);
            final CtsFieldChain leaf1 = appendPrivateLeaf(node1, String.class, "field1");
            final CtsFieldChain node2 = appendPrivateNode(node1, Simple.Inner.class, "newInner");
            final CtsFieldChain leaf2 = appendPrivateLeaf(node2, String.class, "field1Inner");
            final CtsFieldChain leaf3 = appendPrivateLeaf(node2, String.class, "field2Inner");

            return List.of(
                    notification(ENTER_NODE, node1),
                    notification(CONSUME_LEAF, leaf1),
                    notification(ENTER_NODE, node2),
                    notification(CONSUME_LEAF, leaf2),
                    notification(CONSUME_LEAF, leaf3),
                    notification(LEAVE_NODE, node2),
                    notification(LEAVE_NODE, node1)
            );
        }

        @Override
        public String expectedGenerate() {
            return "field1,newInner/field1Inner,newInner/field2Inner";
        }
    }

    private static final class SimpleClassStructureWithEmbedding implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(Simple.class)
                    .addNode(Simple.class, Identifier.newInstance(Simple.Inner.class))
                    .addEmbedding(Simple.class, Identifier.newInstance(Simple.Inner.class))
                    .addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain node1 = CtsFieldChain.newRootInstance(Simple.class);
            final CtsFieldChain leaf1 = appendPrivateLeaf(node1, String.class, "field1");
            final CtsFieldChain leaf2 = appendPrivateLeaf(node1, String.class, "field1Inner");
            final CtsFieldChain leaf3 = appendPrivateLeaf(node1, String.class, "field2Inner");

            return List.of(
                    notification(ENTER_NODE, node1),
                    notification(CONSUME_LEAF, leaf1),
                    notification(CONSUME_LEAF, leaf2),
                    notification(CONSUME_LEAF, leaf3),
                    notification(LEAVE_NODE, node1)
            );
        }

        @Override
        public String expectedGenerate() {
            return "field1,field1Inner,field2Inner";
        }
    }

    /**
     * The specified rename and embedding should ensure that the outcome is exactly the same
     * as for {@link SimpleClassStructureWithEmbedding}
     */
    private static final class SimpleClassStructureWithRenameAndEmbedding implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            final ClassToStringGenerator generator = ClassToStringGenerator.from(Simple.class)
                    .addNode(Simple.class, Identifier.newInstance(Simple.Inner.class))
                    .addName(Identifier.newInstance(Simple.Inner.class, "inner"),
                            Identifier.newInstance(Simple.Inner.class, "date"));

            // See javadoc for explanation.
            generator.addEmbedding(Simple.class, Identifier.newInstance(Simple.Inner.class, "inner"));

            return generator.addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain node1 = CtsFieldChain.newRootInstance(Simple.class);
            final CtsFieldChain leaf1 = appendPrivateLeaf(node1, String.class, "field1");
            final CtsFieldChain leaf2 = appendPrivateLeaf(node1, String.class, "field1Inner");
            final CtsFieldChain leaf3 = appendPrivateLeaf(node1, String.class, "field2Inner");

            return List.of(
                    notification(ENTER_NODE, node1),
                    notification(CONSUME_LEAF, leaf1),
                    notification(CONSUME_LEAF, leaf2),
                    notification(CONSUME_LEAF, leaf3),
                    notification(LEAVE_NODE, node1)
            );
        }

        @Override
        public String expectedGenerate() {
            return "field1,field1Inner,field2Inner";
        }
    }

    private static final class SimpleClassStructureWithRenameAndUnusedEmbedding implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            final ClassToStringGenerator generator = ClassToStringGenerator.from(Simple.class)
                    .addName(Identifier.newInstance(Simple.Inner.class, "inner"),
                            Identifier.newInstance(LocalDate.class, "date"))
                    .addBlocker(LoopBlocker.loop(Identifier.newInstance(LocalDate.class), 3));

            // This embedding will not do anything.
            generator.addEmbedding(Simple.class, Identifier.newInstance(LocalDate.class));

            return generator.addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain node1 = CtsFieldChain.newRootInstance(Simple.class);
            final CtsFieldChain leaf1 = appendPrivateLeaf(node1, String.class, "field1");
            final CtsFieldChain leaf2 = appendPrivateLeaf(node1, LocalDate.class, "date");

            return List.of(
                    notification(ENTER_NODE, node1),
                    notification(CONSUME_LEAF, leaf1),
                    notification(CONSUME_LEAF, leaf2),
                    notification(LEAVE_NODE, node1)
            );
        }

        @Override
        public String expectedGenerate() {
            return "field1,date";
        }
    }

    /**
     * When renaming and adding an embedding, it should not be entered or embedded since it was
     * not specified as a node.
     */
    private static final class ShouldNotEmbedLeaf implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            final ClassToStringGenerator generator = ClassToStringGenerator.from(Simple.class)
                    .addName(Identifier.newInstance(Simple.Inner.class, "inner"),
                            Identifier.newInstance(Simple.Inner.class, "renamed"))
                    .addEmbedding(Simple.class, Identifier.newInstance(Simple.Inner.class, "renamed"));

            return generator.addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain node1 = CtsFieldChain.newRootInstance(Simple.class);
            final CtsFieldChain leaf1 = appendPrivateLeaf(node1, String.class, "field1");
            final CtsFieldChain leaf2 = appendPrivateLeaf(node1, Simple.Inner.class, "renamed");

            return List.of(
                    notification(ENTER_NODE, node1),
                    notification(CONSUME_LEAF, leaf1),
                    notification(CONSUME_LEAF, leaf2),
                    notification(LEAVE_NODE, node1)
            );
        }

        @Override
        public String expectedGenerate() {
            return "field1,renamed";
        }
    }

    @Override
    public List<CtsTestCase> testCases() {
        return List.of(
                new SimpleClassStructure(),
                new SimpleClassStructureWithRenaming(),
                new SimpleClassStructureWithEmbedding(),
                new SimpleClassStructureWithRenameAndEmbedding(),
                new SimpleClassStructureWithRenameAndUnusedEmbedding(),
                new ShouldNotEmbedLeaf()
        );
    }
}
