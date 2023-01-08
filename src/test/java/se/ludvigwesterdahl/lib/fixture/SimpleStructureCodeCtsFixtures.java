package se.ludvigwesterdahl.lib.fixture;

import se.ludvigwesterdahl.lib.cts.ClassToStringGenerator;
import se.ludvigwesterdahl.lib.cts.Identifier;
import se.ludvigwesterdahl.lib.cts.strategy.FlatGenerationStrategy;
import se.ludvigwesterdahl.lib.cts.strategy.GenerationStrategy;

import java.util.List;
import java.util.function.Supplier;

public final class SimpleStructureCodeCtsFixtures implements CtsFixtureGroup {

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

    private static final class SimpleClassStructure implements CtsFixture {

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(Simple.class)
                    .addNode(Identifier.newInstance(Simple.Inner.class))
                    .addObserver(GENERATION_STRATEGY_SUPPLIER.get());
        }

        @Override
        public String expected() {
            return "field1,inner/field1Inner,inner/field2Inner";
        }
    }

    private static final class SimpleClassStructureWithRenaming implements CtsFixture {

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(Simple.class)
                    .addNode(Identifier.newInstance(Simple.Inner.class))
                    .rename(Identifier.newInstance(Simple.Inner.class, "inner"),
                            Identifier.newInstance(Simple.Inner.class, "newInner"))
                    .addObserver(GENERATION_STRATEGY_SUPPLIER.get());
        }

        @Override
        public String expected() {
            return "field1,newInner/field1Inner,newInner/field2Inner";
        }
    }

    private static final class SimpleClassStructureWithEmbedding implements CtsFixture {

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(Simple.class)
                    .addNode(Identifier.newInstance(Simple.Inner.class))
                    .embed(Identifier.newInstance(Simple.Inner.class))
                    .addObserver(GENERATION_STRATEGY_SUPPLIER.get());
        }

        @Override
        public String expected() {
            return "field1,field1Inner,field2Inner";
        }
    }

    @Override
    public List<CtsFixture> ctsFixtures() {
        return List.of(
                new SimpleClassStructure(),
                new SimpleClassStructureWithRenaming(),
                new SimpleClassStructureWithEmbedding()
        );
    }
}
