package se.ludvigwesterdahl.lib.fixture.notification;

import se.ludvigwesterdahl.lib.cts.ClassToStringGenerator;
import se.ludvigwesterdahl.lib.cts.CtsField;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;
import se.ludvigwesterdahl.lib.cts.Identifier;
import se.ludvigwesterdahl.lib.cts.strategy.FlatGenerationStrategy;
import se.ludvigwesterdahl.lib.cts.strategy.GenerationStrategy;
import se.ludvigwesterdahl.lib.fixture.*;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Supplier;

import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateLeaf;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateNode;
import static se.ludvigwesterdahl.lib.fixture.CtsNotification.*;
import static se.ludvigwesterdahl.lib.fixture.CtsNotification.Type.*;

public final class SimpleStructureWithCodeNotification implements CtsNotificationFixtureGroup {

    private static final Supplier<GenerationStrategy> GENERATION_STRATEGY_SUPPLIER = () ->
            new FlatGenerationStrategy.Builder()
                    .withPathSeparator(",")
                    .withLevelMarker("/")
                    .build();

    @SuppressWarnings("unused")
    private static final class Simple {

        private String field1;
        private Inner inner;

        private static final class Inner {

            private String field1Inner;
            private String field2Inner;
        }
    }

    private static final class SimpleClassStructure implements CtsNotificationFixture {

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(Simple.class)
                    .addNode(Identifier.newInstance(Simple.Inner.class))
                    .addObserver(GENERATION_STRATEGY_SUPPLIER.get());
        }

        @Override
        public List<CtsNotification> expected() {
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
    }


    private static final class SimpleClassStructureWithRenaming implements CtsNotificationFixture {

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(Simple.class)
                    .addNode(Identifier.newInstance(Simple.Inner.class))
                    .rename(Identifier.newInstance(Simple.Inner.class, "inner"),
                            Identifier.newInstance(Simple.Inner.class, "newInner"))
                    .addObserver(GENERATION_STRATEGY_SUPPLIER.get());
        }

        @Override
        public List<CtsNotification> expected() {
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
    }

    private static final class SimpleClassStructureWithEmbedding implements CtsNotificationFixture {

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(Simple.class)
                    .addNode(Identifier.newInstance(Simple.Inner.class))
                    .embed(Identifier.newInstance(Simple.Inner.class))
                    .addObserver(GENERATION_STRATEGY_SUPPLIER.get());
        }

        @Override
        public List<CtsNotification> expected() {
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
    }

    @Override
    public List<CtsNotificationFixture> notificationFixtures() {
        return List.of(
                new SimpleClassStructure(),
                new SimpleClassStructureWithRenaming(),
                new SimpleClassStructureWithEmbedding()
        );
    }
}
