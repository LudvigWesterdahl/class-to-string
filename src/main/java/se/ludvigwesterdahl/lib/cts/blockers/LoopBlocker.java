package se.ludvigwesterdahl.lib.cts.blockers;

import se.ludvigwesterdahl.lib.cts.Blocker;
import se.ludvigwesterdahl.lib.cts.CtsField;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;
import se.ludvigwesterdahl.lib.cts.Identifier;

import java.util.Objects;

/**
 * This {@link se.ludvigwesterdahl.lib.cts.Blocker} is used to block repeated traversals into a node. <br/>
 * Assume A -> B -> C -> B, then that would result in an infinite loop (B -> C -> B).
 * It can only be used to block repeated processing of leaf.
 */
public final class LoopBlocker implements Blocker {

    private final Identifier blockingPoint;
    private final int times;
    private int counter;
    private boolean counting;

    private LoopBlocker(final Identifier blockingPoint, int times) {
        this.blockingPoint = blockingPoint;
        this.times = times;
    }

    /**
     * Blocks a given {@link Identifier} from being encountered more than {@code times}. <br/>
     * Note that if {@code times == 0} then this is the same as {@link SimpleBlocker#block(Identifier)}.
     *
     * @param blockingPoint the leaf or node that is blocked
     * @param times the maximum number of times {@code blockingPoint} can be entered
     * @return a {@link Blocker} instance
     * @throws NullPointerException if {@code blockingPoint == null}
     * @throws IllegalArgumentException if {@code times < 0}
     */
    public static Blocker loop(final Identifier blockingPoint, int times) {
        Objects.requireNonNull(blockingPoint);
        if (times < 0) {
            throw new IllegalArgumentException("times cannot be negative");
        }

        if (times == 0) {
            return SimpleBlocker.block(blockingPoint);
        }

        return new LoopBlocker(blockingPoint, times);
    }

    private void reset() {
        counter = times;
        counting = false;
    }

    @Override
    public boolean block(CtsFieldChain fieldChain) {
        final CtsField field = fieldChain.head();
        if (!blockingPoint.matches(field.getIdentifier())) {
            return false;
        }

        return counter < 0;
    }

    @Override
    public void enterNode(CtsFieldChain nodeFieldChain) {
        if (nodeFieldChain.isRoot()) {
            reset();
            return;
        }

        if (blockingPoint.matches(nodeFieldChain.head().getIdentifier())) {
            counting = true;
        }

        if (counting) {
            counter--;
        }
    }

    @Override
    public void consumeLeaf(CtsFieldChain leafFieldChain) {

    }

    @Override
    public void leaveNode(CtsFieldChain nodeFieldChain) {
        if (blockingPoint.matches(nodeFieldChain.head().getIdentifier())) {
            reset();
        }
    }
}
