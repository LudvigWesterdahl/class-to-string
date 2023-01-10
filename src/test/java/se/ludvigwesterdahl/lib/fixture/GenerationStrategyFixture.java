package se.ludvigwesterdahl.lib.fixture;

import se.ludvigwesterdahl.lib.cts.strategy.FlatGenerationStrategy;
import se.ludvigwesterdahl.lib.cts.strategy.GenerationStrategy;

public final class GenerationStrategyFixture {

    private GenerationStrategyFixture() {
        throw new AssertionError("this private constructor is suppressed");
    }

    public static GenerationStrategy newDefaultFlatGenerationStrategy() {
        return new FlatGenerationStrategy.Builder()
                .build();
    }
}
