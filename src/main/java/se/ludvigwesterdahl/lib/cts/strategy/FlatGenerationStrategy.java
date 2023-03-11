package se.ludvigwesterdahl.lib.cts.strategy;

import se.ludvigwesterdahl.lib.cts.CtsFieldChain;

import java.util.Objects;
import java.util.stream.Collectors;

public final class FlatGenerationStrategy implements GenerationStrategy {

    private final String pathSeparator;
    private final String levelMarker;
    private final boolean nodes;
    private final boolean leaf;
    private StringBuilder builder = new StringBuilder();
    private String result = "";

    public static final class Builder {

        private String pathSeparator = ",";
        private String levelMarker = "/";
        private boolean nodes = false;
        private boolean leaf = true;

        public Builder withPathSeparator(final String pathSeparator) {
            this.pathSeparator = Objects.requireNonNull(pathSeparator);
            return this;
        }

        public Builder withLevelMarker(final String levelMarker) {
            this.levelMarker = Objects.requireNonNull(levelMarker);
            return this;
        }

        public Builder withNodes(final boolean nodes) {
            this.nodes = nodes;
            return this;
        }

        public Builder withLeaf(final boolean leaf) {
            this.leaf = leaf;
            return this;
        }

        public FlatGenerationStrategy build() {
            return new FlatGenerationStrategy(this);
        }
    }

    private FlatGenerationStrategy(final Builder builder) {
        pathSeparator = builder.pathSeparator;
        levelMarker = builder.levelMarker;
        nodes = builder.nodes;
        leaf = builder.leaf;
    }

    public String getPathSeparator() {
        return pathSeparator;
    }

    public String getLevelMarker() {
        return levelMarker;
    }

    public boolean isNodes() {
        return nodes;
    }

    public boolean isLeaf() {
        return leaf;
    }

    private String createString(final CtsFieldChain leafFieldChain) {
        return leafFieldChain.allFields()
                .stream()
                .skip(1)
                .map(f -> f.getIdentifier().getName().orElseThrow())
                .collect(Collectors.joining(getLevelMarker()));
    }

    @Override
    public void enterNode(final CtsFieldChain nodeFieldChain) {
        // Reset when root node is encountered.
        if (result != null && nodeFieldChain.head().getIdentifier().getName().isEmpty()) {
            builder = new StringBuilder();
            result = null;
            return;
        }

        if (isNodes()) {
            final String nodeString = createString(nodeFieldChain);
            builder.append(nodeString)
                    .append(getPathSeparator());
        }
    }

    @Override
    public void consumeLeaf(final CtsFieldChain leafFieldChain) {
        if (isLeaf()) {
            final String leafString = createString(leafFieldChain);
            builder.append(leafString)
                    .append(getPathSeparator());
        }
    }

    @Override
    public void leaveNode(final CtsFieldChain nodeFieldChain) {
        // Generate once the root node has been left.
        if (result == null && nodeFieldChain.head().getIdentifier().getName().isEmpty()) {
            final int index = builder.lastIndexOf(getPathSeparator());
            builder.delete(Math.max(0, index), builder.length());
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
