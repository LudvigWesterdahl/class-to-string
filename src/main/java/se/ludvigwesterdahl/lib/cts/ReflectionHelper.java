package se.ludvigwesterdahl.lib.cts;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

final class ReflectionHelper {

    private ReflectionHelper() {
        throw new AssertionError("this private constructor is suppressed");
    }

    static <T extends Annotation> boolean hasDefaultValues(final Class<T> annotationType, final T instance) {
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

    static <T extends Annotation, R> R getAnnotationValue(final Class<T> annotationType,
                                                          final T instance,
                                                          final Class<R> valueType,
                                                          final String name,
                                                          final R defaultValue) {
        try {
            final Method method = annotationType.getMethod(name);
            final R annotationDefaultValue = valueType.cast(method.getDefaultValue());
            final R annotationCurrentValue = valueType.cast(method.invoke(instance));
            if (Objects.equals(annotationDefaultValue, annotationCurrentValue)) {
                return defaultValue;
            }

            return annotationCurrentValue;
        } catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
