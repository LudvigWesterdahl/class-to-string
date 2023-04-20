package se.ludvigwesterdahl.samples;

import se.ludvigwesterdahl.lib.cts.ClassToStringGenerator;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;
import se.ludvigwesterdahl.lib.cts.CtsNode;
import se.ludvigwesterdahl.lib.cts.Identifier;
import se.ludvigwesterdahl.lib.cts.strategy.GenerationStrategy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class JsonTemplate {

    private static final class Movie {

        private String title;
        private int length;
        @CtsNode
        private Rating rating;
    }

    private static final class Rating {

        private int score;
        private String comment;
    }

    private static final class JsonGenerationStrategy implements GenerationStrategy {

        private Deque<String> appendStack;
        private List<String> rows;
        private int level;

        private static String indent(int level, String string) {
            return " ".repeat(2).repeat(level) + string;
        }

        @Override
        public void enterNode(final CtsFieldChain nodeFieldChain) {
            // Resets the generation strategy when iteration starts.
            if (nodeFieldChain.isRoot()) {
                appendStack = new ArrayDeque<>();
                rows = new ArrayList<>();
                level = 1;
                rows.add("{");
                appendStack.addFirst("}");
                return;
            }

            final Identifier head = nodeFieldChain.head().getIdentifier();
            final String name = head.getName().orElseThrow();
            final String jsonField = String.format("\"%s\": {", name);

            rows.add(indent(level, jsonField));
            appendStack.addFirst(indent(level, "}"));

            level++;
        }

        @Override
        public void consumeLeaf(final CtsFieldChain leafFieldChain) {
            final Identifier head = leafFieldChain.head().getIdentifier();
            final String name = head.getName().orElseThrow();
            final String type = head.getType().getSimpleName();
            final String jsonField = String.format("\"%s\": \"%s\",", name, type);
            rows.add(indent(level, jsonField));
        }

        @Override
        public void leaveNode(final CtsFieldChain nodeFieldChain) {
            if (!appendStack.isEmpty()) {
                // Remove comma after last field.
                final String lastRow = rows.get(rows.size() - 1);
                if (lastRow.endsWith(",")) {
                    rows.remove(rows.size() - 1);
                    rows.add(lastRow.substring(0, lastRow.length() - 1));
                }
                rows.add(appendStack.removeFirst());
            }
        }

        @Override
        public String generate() {
            return String.join("\n", rows);
        }
    }

    public static void main(String[] args) {
        // Creates the generation strategy.
        final GenerationStrategy generationStrategy = new JsonGenerationStrategy();

        // Creates the generator.
        final ClassToStringGenerator generator = ClassToStringGenerator.from(Movie.class)
                .addObserver(generationStrategy);

        // Iterates over the structure.
        generator.iterate();

        // Creates the string.
        final String result = generationStrategy.generate();

        // Prints the result.
        System.out.println(result);
        /*
            {
              "title": "String",
              "length": "int",
              "rating": {
                "score": "int",
                "comment": "String"
              }
            }
         */
    }
}
