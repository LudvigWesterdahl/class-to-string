package se.ludvigwesterdahl.lib.fixture;

import se.ludvigwesterdahl.lib.cts.ClassToStringGenerator;

public interface CtsFixture {

    ClassToStringGenerator generator();

    String expected();
}
