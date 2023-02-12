package se.ludvigwesterdahl.lib.cts;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

final class ReflectionHelper {

    static <T> boolean hasDefaultValues(final Class<T> annotationType, final T instance) {
        final Method[] methods = annotationType.getDeclaredMethods();
        for (final Method method : methods) {
            try {
                if (!Objects.equals(method.getDefaultValue(), method.invoke(instance))) {
                    return false;
                }
            } catch (final IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }
}
