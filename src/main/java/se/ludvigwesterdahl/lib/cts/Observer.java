package se.ludvigwesterdahl.lib.cts;

public interface Observer {

    /**
     * Called after a node has been entered. <br/>
     * Note that the first invocation of this will contain a {@link CtsField} with an {@link Identifier}
     * that does not have a name, this can be used to determine when the traversal starts from the root. Any
     * other {@link CtsField} will have an identifier with a name.
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
     * Called after a node has been left. This means that all leafs and nodes below has been traversed. <br/>
     * The final invocation of this method will contain the root node. In other words, the same
     * argument given in the first invocation of {@link Observer#enterNode(CtsFieldChain)}, see that explanation.
     *
     * @param nodeFieldChain the node that was left
     */
    void leaveNode(CtsFieldChain nodeFieldChain);
}
