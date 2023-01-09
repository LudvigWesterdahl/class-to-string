package se.ludvigwesterdahl.lib.fixture;

import se.ludvigwesterdahl.lib.cts.CtsFieldChain;

import java.util.Objects;

public final class CtsNotification {

    public enum Type {
        ENTER_NODE,
        CONSUME_LEAF,
        LEAVE_NODE
    }

    private final Type type;
    private final CtsFieldChain fieldChain;
    private final int hashCode;

    private CtsNotification(final Type type, final CtsFieldChain fieldChain) {
        this.type = type;
        this.fieldChain = fieldChain;
        hashCode = Objects.hash(type, fieldChain);
    }

    public static CtsNotification notification(final Type type, final CtsFieldChain fieldChain) {
        return new CtsNotification(type, fieldChain);
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

        if (!(o instanceof CtsNotification)) {
            return false;
        }

        final CtsNotification ctsNotification = (CtsNotification) o;

        if (hashCode != ctsNotification.hashCode) {
            return false;
        }

        return type == ctsNotification.type
                && fieldChain.equals(ctsNotification.fieldChain);
    }

    @Override
    public String toString() {
        return String.format("%s %s (%d)",
                type,
                fieldChain.head().getIdentifier().getName().orElse(""),
                fieldChain.head().getModifiers());
    }
}
