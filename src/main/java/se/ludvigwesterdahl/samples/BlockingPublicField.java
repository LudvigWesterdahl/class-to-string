package se.ludvigwesterdahl.samples;

import se.ludvigwesterdahl.lib.cts.ClassToStringGenerator;
import se.ludvigwesterdahl.lib.cts.CtsField;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;
import se.ludvigwesterdahl.lib.cts.CtsNode;
import se.ludvigwesterdahl.lib.cts.blocker.AbstractBlocker;
import se.ludvigwesterdahl.lib.cts.strategy.FlatGenerationStrategy;
import se.ludvigwesterdahl.lib.cts.strategy.GenerationStrategy;

import java.lang.reflect.Modifier;

public final class BlockingPublicField {

    public static final class Car {

        /**
         * This will be blocked.
         */
        public String name;
        private String brand;
        @CtsNode
        private Wheel wheel;
        /**
         * This will be blocked.
         */
        @CtsNode
        public Roof roof;
        /**
         * This will be embedded, even though we have a blocker for public fields.
         */
        @CtsNode(embed = true)
        public Door door;
    }

    public static final class Wheel {

        /**
         * This will be blocked.
         */
        public String description;
        private int weight;
        private int price;
    }

    /**
     * This node will not even be entered.
     */
    public static final class Roof {

        private String color;
    }

    public static final class Door {

        private String doorColor;
        private int doorWeight;
    }

    public static final class PublicFieldBlocker extends AbstractBlocker {

        @Override
        public boolean block(final CtsFieldChain fieldChain) {
            final CtsField field = fieldChain.head();

            return Modifier.isPublic(field.getModifiers());
        }
    }

    public static void main(String[] args) {
        // Creates the generator and adds our custom blocker.
        final ClassToStringGenerator generator = ClassToStringGenerator.from(Car.class)
                .addBlocker(new PublicFieldBlocker())
                .addObserver(new FlatGenerationStrategy.Builder().build());

        // Iterates over the class structure and retrieves the added generation strategy.
        final GenerationStrategy strategy = generator.iterate()
                .get(0);

        // Creates the string.
        final String result = strategy.generate();

        // Prints the result.
        System.out.println(result); // brand,wheel/weight,wheel/price,doorColor,doorWeight
    }
}
