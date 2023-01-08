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
            final CtsFieldChain node1 = CtsFieldChain.newInstance(
                    CtsField.newNode(Identifier.newInstance(Simple.class), 0)
            );
            final CtsFieldChain leaf1 = node1.appendAll(List.of(
                    CtsField.newLeaf(Identifier.newInstance(String.class, "field1"), Modifier.PRIVATE)
            )).get(0);
            final CtsFieldChain node2 = node1.appendAll(List.of(
                    CtsField.newNode(Identifier.newInstance(Simple.Inner.class, "inner"), Modifier.PRIVATE)
            )).get(0);
            final CtsFieldChain leaf2 = node2.appendAll(List.of(
                    CtsField.newLeaf(Identifier.newInstance(String.class, "field1Inner"), Modifier.PRIVATE)
            )).get(0);
            final CtsFieldChain leaf3 = node2.appendAll(List.of(
                    CtsField.newLeaf(Identifier.newInstance(String.class, "field2Inner"), Modifier.PRIVATE)
            )).get(0);

            return List.of(
                    new CtsNotification(CtsNotification.Type.ENTER_NODE, node1),
                    new CtsNotification(CtsNotification.Type.CONSUME_LEAF, leaf1),
                    new CtsNotification(CtsNotification.Type.ENTER_NODE, node2),
                    new CtsNotification(CtsNotification.Type.CONSUME_LEAF, leaf2),
                    new CtsNotification(CtsNotification.Type.CONSUME_LEAF, leaf3),
                    new CtsNotification(CtsNotification.Type.LEAVE_NODE, node2),
                    new CtsNotification(CtsNotification.Type.LEAVE_NODE, node1)
            );
        }
    }

    /*
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
            return "field1,newInner/field1Inner,newInner/field2Inner";
        }
    }
    */

    /*
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
            return "field1,field1Inner,field2Inner";
        }
    }
    */

    @Override
    public List<CtsNotificationFixture> notificationFixtures() {
        return List.of(
                new SimpleClassStructure()//,
                //new SimpleClassStructureWithRenaming(),
                //new SimpleClassStructureWithEmbedding()
        );
    }
}
