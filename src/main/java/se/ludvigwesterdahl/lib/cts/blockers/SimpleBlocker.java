package se.ludvigwesterdahl.lib.cts.blockers;

import se.ludvigwesterdahl.lib.cts.Blocker;
import se.ludvigwesterdahl.lib.cts.CtsField;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;
import se.ludvigwesterdahl.lib.cts.Identifier;

import java.util.Objects;

/**
 * This {@link Blocker} is used to block any leaf or node when encountered.
 */
public final class SimpleBlocker extends AbstractBlocker {

    private final Identifier parentNode;
    private final Identifier leafOrNode;
    private final Boolean blockNodes;

    private SimpleBlocker(final Identifier parentNode,
                          final Identifier leafOrNode,
                          final Boolean blockNodes) {
        this.parentNode = parentNode;
        this.leafOrNode = leafOrNode;
        this.blockNodes = blockNodes;
    }

    /**
     * Blocks a node with the given {@link Identifier} when encountered.
     *
     * @param parentNode the parent node; can be null meaning any parent node
     * @param node the node to block
     * @return a {@link Blocker} instance
     */
    public static Blocker blockNode(final Identifier parentNode, final Identifier node) {
        Objects.requireNonNull(node);

        return new SimpleBlocker(parentNode, node, true);
    }

    /**
     * Blocks a leaf with the given {@link Identifier} when encountered.
     *
     * @param parentNode the parent node; can be null meaning any parent node
     * @param leaf the leaf to block
     * @return a {@link Blocker} instance
     */
    public static Blocker blockLeaf(final Identifier parentNode, final Identifier leaf) {
        Objects.requireNonNull(leaf);

        return new SimpleBlocker(parentNode, leaf, false);
    }

    /**
     * Blocks a leaf or node with the given {@link Identifier} when encountered.
     *
     * @param parentNode the parent node; can be null meaning any parent node
     * @param leafOrNode the leaf or node to block
     * @return a {@link Blocker} instance
     */
    public static Blocker block(final Identifier parentNode, final Identifier leafOrNode) {
        Objects.requireNonNull(leafOrNode);

        return new SimpleBlocker(parentNode, leafOrNode, null);
    }

    @Override
    public boolean block(final CtsFieldChain fieldChain) {
        final CtsField field = fieldChain.head();

        if (!leafOrNode.matches(field.getIdentifier())) {
            return false;
        }

        if (blockNodes == null) {
            return true;
        }

        return Objects.equals(blockNodes, field.isNode());
    }
}
