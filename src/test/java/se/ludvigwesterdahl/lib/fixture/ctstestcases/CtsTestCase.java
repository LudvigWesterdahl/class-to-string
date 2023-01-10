package se.ludvigwesterdahl.lib.fixture.ctstestcases;

import se.ludvigwesterdahl.lib.cts.ClassToStringGenerator;

import java.util.List;

public interface CtsTestCase {

    ClassToStringGenerator generator();

    default boolean hasExpectedNotifications() {
        return true;
    }

    List<CtsNotification> expectedNotifications();


    default boolean hasExpectedGenerate() {
        return true;
    }

    String expectedGenerate();
}
