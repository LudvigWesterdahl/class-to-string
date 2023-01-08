package se.ludvigwesterdahl.lib.cts;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * TODO: check over this class and simplify it.
 * Do we need all these helper methods?
 * Maybe add a size? Then add that documentation to Blocker interface as well.
 */
public final class CtsFieldChain {

    private final List<CtsField> fields;
    private final int hashCode;

    private CtsFieldChain(final List<CtsField> fields) {
        this.fields = fields;
        hashCode = fields.hashCode();
    }

    public static CtsFieldChain newInstance(final CtsField tail) {
        Objects.requireNonNull(tail);
        return new CtsFieldChain(List.of(tail));
    }

    public CtsField rootNode() {
        return fields.get(0);
    }

    public CtsField head() {
        return fields.get(fields.size() - 1);
    }

    public CtsField lastNode() {
        if (head().isNode()) {
            return head();
        }

        return fields.get(Math.max(0, fields.size() - 2));
    }

    public Optional<CtsField> leaf() {
        if (head().isNode()) {
            return Optional.empty();
        }

        return Optional.of(head());
    }

    public List<CtsField> allFields() {
        return new ArrayList<>(fields);
    }

    private CtsFieldChain append(final CtsField head) {
        if (leaf().isPresent()) {
            throw new IllegalStateException("cannot append to this chain");
        }

        final List<CtsField> fields = new ArrayList<>(this.fields);
        fields.add(head);

        return new CtsFieldChain(fields);
    }

    public List<CtsFieldChain> appendAll(final List<CtsField> heads) {
        final List<CtsFieldChain> fieldChains = new ArrayList<>();
        for (final CtsField head : heads) {
            fieldChains.add(append(head));
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
                getClass().getCanonicalName(), fields);
    }
}
