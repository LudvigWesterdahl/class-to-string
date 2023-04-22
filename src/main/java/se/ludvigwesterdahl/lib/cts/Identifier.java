package se.ludvigwesterdahl.lib.cts;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable class to represent a field in a class or match when fields are traversed
 * in the {@link ClassToStringGenerator}.
 */
public final class Identifier {

    private final Class<?> type;
    private final String name;
    private final int hashCode;

    private Identifier(final Class<?> type, final String name) {
        this.type = type;
        this.name = name;

        hashCode = Objects.hash(type, name);
    }

    /**
     * Creates a new {@link Identifier} instance
     *
     * @param type the type
     * @param name the name
     * @return a new instance
     * @throws NullPointerException     if any of the arguments is {@code null}
     * @throws IllegalArgumentException if {@code name} is blank
     */
    public static Identifier newInstance(final Class<?> type, final String name) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(name);

        if (name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }

        return new Identifier(type, name);
    }

    /**
     * Creates a new {@link Identifier} without a name.
     *
     * @param type the type
     * @return a new instance
     * @throws NullPointerException if {@code type} is null
     */
    public static Identifier newInstance(final Class<?> type) {
        Objects.requireNonNull(type);

        return new Identifier(type, null);
    }

    /**
     * Returns the type.
     *
     * @return the type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Returns the name if one exists.
     *
     * @return maybe the name
     */
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    /**
     * Checks if this {@link Identifier} matches another by comparing the type and only comparing the name
     * if both {@link Identifier} has a name. If one is missing a name, it will only compare
     * the type. <br>
     * In other words, if both {@link Identifier} has a name, then this method is the same
     * as calling {@link Identifier#equals(Object)}.
     * <b>Example</b>
     * {@code (String.class, "hello")} will match {@code (String.class, null)}, but they will not equal.
     *
     * @param other the identifier to check for matching.
     * @return {@code true} if {@code this} has the same type as {@code other} and maybe the name.
     */
    public boolean matches(final Identifier other) {
        if (other == null) {
            return false;
        }

        if (name != null && other.name != null) {
            return equals(other);
        }

        return type.equals(other.type);
    }

    /**
     * Returns a new {@link Identifier} instance without the name.
     *
     * @return a {@link Identifier} without the name
     */
    public Identifier stripName() {
        return Identifier.newInstance(type);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Identifier)) {
            return false;
        }

        final Identifier identifier = (Identifier) o;

        if (hashCode != identifier.hashCode) {
            return false;
        }

        return type.equals(identifier.type)
                && Objects.equals(name, identifier.name);
    }

    @Override
    public String toString() {
        return String.format("%s[type=%s, name=%s]",
                getClass().getSimpleName(), type, name);
    }
}
