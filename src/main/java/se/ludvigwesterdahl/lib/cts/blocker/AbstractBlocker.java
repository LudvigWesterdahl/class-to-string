package se.ludvigwesterdahl.lib.cts.blocker;

import se.ludvigwesterdahl.lib.cts.CtsFieldChain;

/**
 * Base class for {@link Blocker} implementations. This class implements all methods
 * in {@link se.ludvigwesterdahl.lib.cts.Observer} as no-ops. <br/>
 */
public abstract class AbstractBlocker implements Blocker {

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
