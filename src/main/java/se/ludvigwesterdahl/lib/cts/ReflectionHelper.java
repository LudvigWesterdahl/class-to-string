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

    static <T, R> R getValueUnlessDefault(final Class<T> annotationType,
                                          final T instance,
                                          final Class<R> valueType,
                                          final String name,
                                          final R value) {
        try {
            final Method method = annotationType.getMethod(name);
            final R defaultValue = valueType.cast(method.getDefaultValue());
            final R currentValue = valueType.cast(method.invoke(instance));
            if (Objects.equals(defaultValue, currentValue)) {
                return value;
            }

            return currentValue;
        } catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
