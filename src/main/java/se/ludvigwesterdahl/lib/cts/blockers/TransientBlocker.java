package se.ludvigwesterdahl.lib.cts.blockers;

import se.ludvigwesterdahl.lib.cts.Blocker;
import se.ludvigwesterdahl.lib.cts.CtsField;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;

import java.lang.reflect.Modifier;

/**
 * This {@link Blocker} is used to block leaf or nodes that have been declared {@code transient}.
 */
public final class TransientBlocker implements Blocker {

    private static final class FieldHolder {
        private static final TransientBlocker INSTANCE = new TransientBlocker();
    }

    private TransientBlocker() {
        // empty
    }

    /**
     * Returns the blocker instance to block transient leaf or nodes.
     *
     * @return a {@link Blocker} instance
     */
    public static Blocker getInstance() {
        return FieldHolder.INSTANCE;
    }

    @Override
    public boolean block(final CtsFieldChain fieldChain) {
        return Modifier.isTransient(fieldChain.head().getModifiers());
    }

    @Override
    public void enterNode(final CtsFieldChain nodeFieldChain) {
        // empty
    }

    @Override
    public void consumeLeaf(final CtsFieldChain leafFieldChain) {
        // empty
    }

    @Override
    public void leaveNode(final CtsFieldChain nodeFieldChain) {
        // empty
    }
}
