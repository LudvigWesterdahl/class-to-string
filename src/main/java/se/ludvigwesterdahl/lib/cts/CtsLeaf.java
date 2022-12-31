package se.ludvigwesterdahl.lib.cts;


import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CtsLeaf {

    boolean ignore() default false;

    String name() default "";
}
