package se.ludvigwesterdahl.lib.fixture;

import se.ludvigwesterdahl.lib.cts.ClassToStringGenerator;

public interface CtsGenerationFixture {

    ClassToStringGenerator generator();

    String expected();
}
