package se.ludvigwesterdahl.samples;

import se.ludvigwesterdahl.lib.cts.ClassToStringGenerator;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;
import se.ludvigwesterdahl.lib.cts.CtsNode;
import se.ludvigwesterdahl.lib.cts.strategy.GenerationStrategy;

import java.util.ArrayList;
import java.util.List;

public final class SkipIterationResult {

    public static final class Root {

        private String rootField;
        @CtsNode
        private Node node;

        public static final class Node {

            private String nodeField;
        }
    }

    /**
     * This generator will print the result once the iteration is finished.
     */
    public static final class CustomGenerationStrategy implements GenerationStrategy {

        private List<String> names;

        @Override
        public void enterNode(final CtsFieldChain nodeFieldChain) {
            // Resets the builder when iteration starts;
            if (nodeFieldChain.isRoot()) {
                names = new ArrayList<>();
                return;
            }

            // This sample will simply add the name of the node field, not taking into account any levels.
            names.add(nodeFieldChain.head().getIdentifier().getName().orElseThrow());
        }

        @Override
        public void consumeLeaf(final CtsFieldChain leafFieldChain) {
            // This sample will simply add the name of the leaf field, not taking into account any levels.
            names.add(leafFieldChain.head().getIdentifier().getName().orElseThrow());
        }

        @Override
        public void leaveNode(final CtsFieldChain nodeFieldChain) {
            // Checks if the iteration is completed.
            if (nodeFieldChain.isRoot()) {
                System.out.println(String.join(" + ", names));
            }
        }

        /**
         * This method will not be used in this sample.
         */
        @Override
        public String generate() {
            throw new UnsupportedOperationException();
        }
    }

    public static void main(String[] args) {
        // Creates the generation strategy.
        final GenerationStrategy generationStrategy = new CustomGenerationStrategy();

        // Creates the generator and runs the iteration which should print the expected result.
        ClassToStringGenerator.from(Root.class)
                .addObserver(generationStrategy)
                .iterate(); // rootField + node + nodeField
    }
}
