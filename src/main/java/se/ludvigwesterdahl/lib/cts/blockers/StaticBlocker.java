package se.ludvigwesterdahl.lib.cts.blockers;

import se.ludvigwesterdahl.lib.cts.Blocker;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;

import java.lang.reflect.Modifier;

/**
 * This {@link Blocker} is used to block leaf or nodes that have been declared {@code static}.
 */
public final class StaticBlocker extends AbstractBlocker {

    private static final class FieldHolder {
        private static final StaticBlocker INSTANCE = new StaticBlocker();
    }

    private StaticBlocker() {
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
        return Modifier.isStatic(fieldChain.head().getModifiers());
    }
}
