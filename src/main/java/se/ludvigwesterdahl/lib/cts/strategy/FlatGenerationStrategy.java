package se.ludvigwesterdahl.lib.cts.strategy;

import se.ludvigwesterdahl.lib.cts.CtsFieldChain;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class FlatGenerationStrategy implements GenerationStrategy {

    private final String pathSeparator;
    private final String levelMarker;
    private StringBuilder builder = new StringBuilder();
    private String result = "";

    public static final class Builder {

        private String pathSeparator = ",";
        private String levelMarker = "/";

        public Builder withPathSeparator(final String pathSeparator) {
            this.pathSeparator = Objects.requireNonNull(pathSeparator);
            return this;
        }

        public Builder withLevelMarker(final String levelMarker) {
            this.levelMarker = Objects.requireNonNull(levelMarker);
            return this;
        }

        public GenerationStrategy build() {
            return new FlatGenerationStrategy(this);
        }
    }

    private FlatGenerationStrategy(final Builder builder) {
        pathSeparator = builder.pathSeparator;
        levelMarker = builder.levelMarker;
    }

    public String getPathSeparator() {
        return pathSeparator;
    }

    public String getLevelMarker() {
        return levelMarker;
    }

    @Override
    public void enterNode(final CtsFieldChain nodeFieldChain) {
        // Reset when root node is encountered.
        if (result != null && nodeFieldChain.head().getIdentifier().getName().isEmpty()) {
            builder = new StringBuilder();
            result = null;
        }
    }

    private String createLeafString(final CtsFieldChain leafFieldChain) {
        return leafFieldChain.allFields()
                .stream()
                .skip(1)
                .map(f -> f.getIdentifier().getName().orElseThrow())
                .collect(Collectors.joining(getLevelMarker()));
    }

    @Override
    public void consumeLeaf(final CtsFieldChain leafFieldChain) {
        final String leafString = createLeafString(leafFieldChain);
        builder.append(leafString)
                .append(getPathSeparator());
    }

    @Override
    public void leaveNode(final CtsFieldChain nodeFieldChain) {
        // Generate once the root node has been left.
        if (result == null && nodeFieldChain.head().getIdentifier().getName().isEmpty()) {
            final int index = builder.lastIndexOf(getPathSeparator());
            builder.delete(index, builder.length());
            result = builder.toString();
        }
    }

    @Override
    public String generate() {
        if (result == null) {
            return "";
        }

        return result;
    }
}
