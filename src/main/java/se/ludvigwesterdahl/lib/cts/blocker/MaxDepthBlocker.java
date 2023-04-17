package se.ludvigwesterdahl.lib.cts.blocker;

import se.ludvigwesterdahl.lib.cts.CtsFieldChain;
import se.ludvigwesterdahl.lib.cts.Identifier;

import java.util.Objects;

public final class MaxDepthBlocker extends AbstractBlocker {

    private final Identifier blockingPoint;
    private final int continueLevels;
    private int nextLevel;
    private boolean counting;

    private MaxDepthBlocker(final Identifier blockingPoint, final int continueLevels) {
        this.blockingPoint = blockingPoint;
        this.continueLevels = continueLevels;
    }

    /**
     * Stops the traversal after visiting {@code node} and traversing down {@code continueLevels} times. <br/>
     * <ul>
     *     <li>{@code continueLevels} == 0: enter {@code node} and process the leaf found but not any nodes</li>
     *     <li>{@code continueLevels} > 0: enter {@code node} and traverse down maximum X levels</li>
     * </ul>
     *
     * @param blockingPoint  the node to initiate the blocker from
     * @param continueLevels maximum number of levels to traverse
     * @return a {@link Blocker} instance
     * @throws NullPointerException     if {@code node == null}
     * @throws IllegalArgumentException if {@code continueLevels < 0}
     */
    public static Blocker newInstance(final Identifier blockingPoint, final int continueLevels) {
        Objects.requireNonNull(blockingPoint);
        if (continueLevels < 0) {
            throw new IllegalArgumentException("continueLevels cannot be negative");
        }

        return new MaxDepthBlocker(blockingPoint, continueLevels);
    }

    @Override
    public boolean block(final CtsFieldChain fieldChain) {
        if (!fieldChain.head().isNode()) {
            return false;
        }

        return nextLevel < 0;
    }

    private void reset() {
        counting = false;
        nextLevel = continueLevels;
    }

    @Override
    public void enterNode(final CtsFieldChain nodeFieldChain) {
        if (nodeFieldChain.isRoot()) {
            reset();
            return;
        }

        if (blockingPoint.matches(nodeFieldChain.head().getIdentifier())) {
            counting = true;
        }

        if (counting) {
            nextLevel--;
        }
    }

    @Override
    public void leaveNode(final CtsFieldChain nodeFieldChain) {
        if (counting) {
            nextLevel++;
        }

        if (counting && nextLevel == continueLevels && blockingPoint.matches(nodeFieldChain.head().getIdentifier())) {
            reset();
        }
    }
}
