package se.ludvigwesterdahl.lib.cts;

// TODO: These need more information. Probably the raw field to be flexible.
// TODO: Can we have a SimpleSkipper class that takes an identifier to skip in constructor?

import java.lang.reflect.Field;

public interface Blocker extends Observer {


    boolean block(Field field);
}
