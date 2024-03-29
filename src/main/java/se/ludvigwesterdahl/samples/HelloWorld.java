package se.ludvigwesterdahl.samples;

import se.ludvigwesterdahl.lib.cts.ClassToStringGenerator;
import se.ludvigwesterdahl.lib.cts.strategy.FlatGenerationStrategy;
import se.ludvigwesterdahl.lib.cts.strategy.GenerationStrategy;

public final class HelloWorld {

    public static final class Person {

        private String firstName;
        private String lastName;
    }

    public static void main(String[] args) {
        // Creates the generator.
        final ClassToStringGenerator generator = ClassToStringGenerator.from(Person.class)
                .addObserver(new FlatGenerationStrategy.Builder().build());

        // Iterates over the class structure and retrieves the added generation strategy.
        final GenerationStrategy strategy = generator.iterate()
                .get(0);

        // Creates the string.
        final String result = strategy.generate();

        // Prints the result.
        System.out.println(result); // firstName,lastName
    }
}
