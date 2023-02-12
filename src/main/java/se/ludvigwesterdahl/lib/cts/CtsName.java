package se.ludvigwesterdahl.lib.cts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * By default, fields are leafs unless node is specified on the class or the class field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CtsName {

    String name() default "";

    Class<?> type() default CtsName.class;
}
