package se.ludvigwesterdahl.lib.cts;

import java.lang.reflect.Field;

public interface Observer {

    void consume(Field field);
}
