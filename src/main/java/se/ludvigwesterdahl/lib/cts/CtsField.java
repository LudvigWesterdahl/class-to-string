package se.ludvigwesterdahl.lib.cts;

import java.util.Objects;

public final class CtsField {

    private final Identifier identifier;
    private final int modifiers;
    private final boolean node;
    private final int hashCode;

    private CtsField(final Identifier identifier, final int modifiers, final boolean node) {
        this.identifier = identifier;
        this.modifiers = modifiers;
        this.node = node;
        hashCode = Objects.hash(identifier, modifiers, node);
    }

    public static CtsField newNode(final Identifier identifier, final int modifiers) {
        Objects.requireNonNull(identifier);

        return new CtsField(identifier, modifiers, true);
    }

    public static CtsField newLeaf(final Identifier identifier, final int modifiers) {
        Objects.requireNonNull(identifier);

        return new CtsField(identifier, modifiers, false);
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public int getModifiers() {
        return modifiers;
    }

    public boolean isNode() {
        return node;
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

        if (!(o instanceof CtsField)) {
            return false;
        }

        final CtsField ctsField = (CtsField) o;

        if (hashCode != ctsField.hashCode) {
            return false;
        }

        return identifier.equals(ctsField.identifier)
                && modifiers == ctsField.modifiers
                && node == ctsField.node;
    }

    @Override
    public String toString() {
        return String.format("%s[identifier=%s, modifiers=%d, node=%b]",
                getClass().getSimpleName(), identifier, modifiers, node);
    }
}
