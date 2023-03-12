package se.ludvigwesterdahl.lib.cts.blockers;

import se.ludvigwesterdahl.lib.cts.Blocker;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;

import java.lang.reflect.Modifier;

/**
 * This {@link Blocker} is used to block leaf or nodes that have been declared {@code transient}.
 * For instance, when running tests with coverage in intellij, the IDE adds a transient variable: <br/>
 * {@code private static transient int[] __$lineHits$__;} <br/>
 * that can then be blocked using this {@link Blocker}.
 */
public final class TransientBlocker extends AbstractBlocker {

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
}
