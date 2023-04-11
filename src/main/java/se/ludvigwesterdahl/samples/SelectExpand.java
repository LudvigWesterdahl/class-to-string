package se.ludvigwesterdahl.samples;

import se.ludvigwesterdahl.lib.cts.ClassToStringGenerator;
import se.ludvigwesterdahl.lib.cts.CtsNode;
import se.ludvigwesterdahl.lib.cts.strategy.FlatGenerationStrategy;
import se.ludvigwesterdahl.lib.cts.strategy.GenerationStrategy;

import java.util.List;

public final class SelectExpand {

    public static final class PersonResponse {

        private String firstName;
        private String lastName;
        @CtsNode
        private Address address;
        @CtsNode
        private Contact contact;

        private static final class Address {

            private String street;
            private String zip;
            private String city;
        }

        private static final class Contact {

            private String email;
            private String phone;
        }
    }

    public static void main(String[] args) {
        // Creates the generator and adds two strategies, one for the select and one for the expand
        // query parameters.
        final ClassToStringGenerator generator = ClassToStringGenerator.from(PersonResponse.class)
                .addObserver(new FlatGenerationStrategy.Builder()
                        .withNodes(false)
                        .withLeaf(true)
                        .build())
                .addObserver(new FlatGenerationStrategy.Builder()
                        .withNodes(true)
                        .withLeaf(false)
                        .build());

        // Iterates over the class structure and retrieves the added generation strategy.
        final List<GenerationStrategy> strategyList = generator.iterate();
        final GenerationStrategy selectStrategy = strategyList.get(0);
        final GenerationStrategy expandStrategy = strategyList.get(1);

        // Creates the strings
        // $select=firstName,lastName,address/street,address/zip,address/city,contact/email,contact/phone
        final String select = selectStrategy.generate();
        final String expand = expandStrategy.generate();

        // Prints the result.
        // $select=firstName,lastName,address/street,address/zip,address/city,contact/email,contact/phone
        System.out.println("$select=" + select);
        // $expand=address,contact
        System.out.println("$expand=" + expand);
    }
}
