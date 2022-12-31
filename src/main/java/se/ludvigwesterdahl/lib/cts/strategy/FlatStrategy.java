package se.ludvigwesterdahl.lib.cts.strategy;

import java.util.Objects;

public final class FlatStrategy {

    private final String pathSeparator;
    private final String levelMarker;
    private final boolean sorted;
    private final boolean minimal;
    private final boolean includeLeaf;
    private final boolean ignoreTransientLeaf;

    public static final class Builder {

        private String pathSeparator = ",";
        private String levelMarker = "/";
        private boolean sorted = false;
        private boolean minimal = false;
        // TODO: these are general stopper
        private boolean includeLeaf = true;
        // TODO: these are general stopper
        private boolean ignoreTransientLeaf = true;

        public Builder withPathSeparator(final String pathSeparator) {
            this.pathSeparator = Objects.requireNonNull(pathSeparator);
            return this;
        }

        public Builder withLevelMarker(final String levelMarker) {
            this.levelMarker = Objects.requireNonNull(levelMarker);
            return this;
        }

        public Builder withSorted(final boolean sorted) {
            this.sorted = sorted;
            return this;
        }

        public Builder withMinimal(final boolean minimal) {
            this.minimal = minimal;
            return this;
        }

        public Builder withIncludeLeaf(final boolean includeLeaf) {
            this.includeLeaf = includeLeaf;
            return this;
        }

        public Builder withIgnoreTransientLeaf(final boolean ignoreTransientLeaf) {
            this.ignoreTransientLeaf = ignoreTransientLeaf;
            return this;
        }

        public FlatStrategy build() {
            return new FlatStrategy(this);
        }
    }

    private FlatStrategy(final Builder builder) {
        pathSeparator = builder.pathSeparator;
        levelMarker = builder.levelMarker;
        sorted = builder.sorted;
        minimal = builder.minimal;
        includeLeaf = builder.includeLeaf;
        ignoreTransientLeaf = builder.ignoreTransientLeaf;
    }

}
