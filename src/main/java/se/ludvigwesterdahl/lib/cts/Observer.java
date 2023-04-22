package se.ludvigwesterdahl.lib.cts;

import se.ludvigwesterdahl.lib.cts.blocker.Blocker;

public interface Observer {

    /**
     * Called after a node has been entered. <br>
     * Note that the first invocation of this will contain a {@link CtsFieldChain} that is the root.
     * Any state in the {@link Blocker} is highly recommended to be reset when this occurs.
     *
     * @param nodeFieldChain the node that was entered
     */
    void enterNode(CtsFieldChain nodeFieldChain);

    /**
     * Calls after a leaf has been processed.
     *
     * @param leafFieldChain the leaf that was processed
     */
    void consumeLeaf(CtsFieldChain leafFieldChain);

    /**
     * Called after a node has been left. This means that all leafs and nodes below has been traversed. <br>
     * The final invocation of this method will contain the root node. In other words, the same
     * argument given in the first invocation of {@link Observer#enterNode(CtsFieldChain)}, see that explanation.
     *
     * @param nodeFieldChain the node that was left
     */
    void leaveNode(CtsFieldChain nodeFieldChain);
}
