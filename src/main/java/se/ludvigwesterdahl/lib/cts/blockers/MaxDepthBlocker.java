package se.ludvigwesterdahl.lib.cts.blockers;

import se.ludvigwesterdahl.lib.cts.Blocker;
import se.ludvigwesterdahl.lib.cts.CtsField;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;
import se.ludvigwesterdahl.lib.cts.Identifier;

import java.util.Objects;

public final class MaxDepthBlocker implements Blocker {

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

    /**
     * Same as calling {@link MaxDepthBlocker#newInstance(Identifier, int)}
     * with {@code continueLevels == 0}.
     *
     * @param node the node
     * @return a {@link Blocker} instance
     */
    public static Blocker stopAfter(final Identifier node) {
        return newInstance(node, 0);
    }

    @Override
    public boolean block(final CtsFieldChain fieldChain) {
        if (fieldChain.leaf().isPresent()) {
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
        final CtsField node = nodeFieldChain.lastNode();

        if (node.getIdentifier().getName().isEmpty()) {
            reset();
            return;
        }

        if (blockingPoint.matches(node.getIdentifier())) {
            counting = true;
        }

        if (counting) {
            nextLevel--;
        }
    }

    @Override
    public void consumeLeaf(final CtsFieldChain leafFieldChain) {
        // empty
    }

    @Override
    public void leaveNode(final CtsFieldChain nodeFieldChain) {
        final CtsField node = nodeFieldChain.lastNode();

        if (blockingPoint.matches(node.getIdentifier())) {
            reset();
        }
    }
}
