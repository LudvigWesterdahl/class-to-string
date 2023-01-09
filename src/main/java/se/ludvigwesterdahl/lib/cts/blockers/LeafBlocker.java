package se.ludvigwesterdahl.lib.cts.blockers;

import se.ludvigwesterdahl.lib.cts.Blocker;
import se.ludvigwesterdahl.lib.cts.CtsField;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;

import java.lang.reflect.Modifier;

/**
 * This {@link Blocker} is used to block leaf.
 */
public final class LeafBlocker implements Blocker {

    private static final class FieldHolder {
        private static final LeafBlocker INSTANCE = new LeafBlocker();
    }

    private LeafBlocker() {
        // empty
    }

    /**
     * Returns the blocker instance to block leaf.
     *
     * @return a {@link Blocker} instance
     */
    public static Blocker getInstance() {
        return FieldHolder.INSTANCE;
    }

    @Override
    public boolean block(final CtsFieldChain fieldChain) {
        return fieldChain.isLeaf();
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
