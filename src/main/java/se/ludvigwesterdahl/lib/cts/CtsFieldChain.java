package se.ludvigwesterdahl.lib.cts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This <b>immutable</b> class represents a chain of {@link CtsField}.
 */
public final class CtsFieldChain {

    private final List<CtsField> fields;
    private final int hashCode;

    private CtsFieldChain(final List<CtsField> fields) {
        this.fields = fields;
        hashCode = fields.hashCode();
    }

    /**
     * Creates a new {@link CtsFieldChain} instance from the root node.
     *
     * @param rootType the type of the root
     * @return a new chain instance
     */
    public static CtsFieldChain newRootInstance(final Class<?> rootType) {
        final CtsField rootField = CtsField.newNode(Identifier.newInstance(rootType), 0);
        final List<CtsField> fields = new ArrayList<>();
        fields.add(rootField);
        return new CtsFieldChain(fields);
    }

    /**
     * Returns the head (the last {@link CtsField}) of this chain.
     *
     * @return the last field
     */
    public CtsField head() {
        return fields.get(fields.size() - 1);
    }

    /**
     * Returns {@code true} if the head of this chain only contains the root. <br/>
     * <b>Note</b> that if this returns {@code true} then {@link CtsFieldChain#head()} will contain
     * a field with an {@link Identifier} that does not have a name.
     *
     * @return {@code true} if root, {@code false} otherwise
     */
    public boolean isRoot() {
        return fields.size() == 1;
    }

    /**
     * Returns all {@link CtsField} this chain contains.
     *
     * @return all fields
     */
    public List<CtsField> allFields() {
        return new ArrayList<>(fields);
    }

    private CtsFieldChain chain(final CtsField newHead) {
        if (newHead.getIdentifier().getName().isEmpty()) {
            throw new IllegalArgumentException("cannot append a head without a name");
        }

        if (!head().isNode()) {
            throw new IllegalStateException("cannot append to this chain");
        }

        final List<CtsField> fields = new ArrayList<>(this.fields);
        fields.add(newHead);

        return new CtsFieldChain(fields);
    }

    /**
     * Creates new {@link CtsFieldChain} by adding all {@code heads} to this chain.
     *
     * @param heads the fields to chain
     * @return new {@link CtsFieldChain} instances
     * @throws IllegalArgumentException if any of the {@link CtsField} has an {@link Identifier} without a name
     * @throws IllegalStateException    if the head of this chain is a leaf
     */
    public List<CtsFieldChain> chainAll(final Collection<CtsField> heads) {
        final List<CtsFieldChain> fieldChains = new ArrayList<>();
        for (final CtsField head : heads) {
            fieldChains.add(chain(head));
        }

        return fieldChains;
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

        if (!(o instanceof CtsFieldChain)) {
            return false;
        }

        final CtsFieldChain ctsFieldChain = (CtsFieldChain) o;

        if (hashCode != ctsFieldChain.hashCode) {
            return false;
        }

        return fields.equals(ctsFieldChain.fields);
    }

    @Override
    public String toString() {
        return String.format("%s[fields=%s]",
                getClass().getSimpleName(), fields);
    }
}
