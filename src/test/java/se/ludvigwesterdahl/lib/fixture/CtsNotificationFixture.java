package se.ludvigwesterdahl.lib.fixture;

import se.ludvigwesterdahl.lib.cts.ClassToStringGenerator;

import java.util.List;

public interface CtsNotificationFixture {

    ClassToStringGenerator generator();

    List<CtsNotification> expected();
}
