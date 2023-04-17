package se.ludvigwesterdahl.lib.cts.blocker;

import se.ludvigwesterdahl.lib.cts.CtsFieldChain;

/**
 * This {@link Blocker} is used to block leaf.
 */
public final class LeafBlocker extends AbstractBlocker {

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
        return !fieldChain.head().isNode();
    }
}
