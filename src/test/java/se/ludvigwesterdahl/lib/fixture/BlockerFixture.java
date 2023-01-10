package se.ludvigwesterdahl.lib.fixture;

import se.ludvigwesterdahl.lib.cts.Blocker;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;
import se.ludvigwesterdahl.lib.cts.blockers.AbstractBlocker;

public final class BlockerFixture {

    private BlockerFixture() {
        throw new AssertionError("this private constructor is suppressed");
    }

    public static Blocker blockAllNodesExceptRoot() {
        return new AbstractBlocker() {
            @Override
            public boolean block(final CtsFieldChain fieldChain) {
                if (fieldChain.isRoot()) {
                    return false;
                }

                return fieldChain.head().isNode();
            }
        };
    }
}
