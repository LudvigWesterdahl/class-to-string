package se.ludvigwesterdahl.lib.cts;

import java.util.*;
import java.util.stream.Stream;

public final class ClassToStringGenerator {

    private final Set<Identifier> nodes;
    private final Map<Identifier, Identifier> renaming;
    private final Map<Identifier, List<Identifier>> embeddings;

    private ClassToStringGenerator(final Set<Identifier> nodes,
                                   final Map<Identifier, Identifier> renaming,
                                   final Map<Identifier, List<Identifier>> embeddings) {
        this.nodes = nodes;
        this.renaming = renaming;
        this.embeddings = embeddings;
    }

    public static ClassToStringGenerator from(final Class<?> rootNode) {
        // Compute structure based on annotations.

        return new ClassToStringGenerator(new HashSet<>(), new HashMap<>(), new HashMap<>());
    }

    /**
     * Specifies that the field should be considered a node when encountered. It will traverse
     * into that structure.
     *
     * @param node the node
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException if {@code node == null}
     */
    public ClassToStringGenerator addNode(final Identifier node) {
        nodes.add(node);

        return this;
    }


    public ClassToStringGenerator rename(final Identifier from, final Identifier to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        renaming.put(from, to);

        return this;
    }

    /**
     * Embeds a node into another node. This can be used if you want to inject some fields or nodes into an
     * existing structure without modifying it. <br/>
     * If the node you want to embed is a field in some class that it should be embedded into, use
     * the {@link ClassToStringGenerator#embed(Identifier)} instead. <br/>
     *
     * @param node   the node to embed
     * @param toNode the destination node;
     *               if {@code null} behaves the same as {@link ClassToStringGenerator#embed(Identifier)}
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException if {@code node == null}
     */
    public ClassToStringGenerator embed(final Identifier node, final Identifier toNode) {
        Objects.requireNonNull(node);

        embeddings.computeIfAbsent(node, ignored -> new ArrayList<>())
                .add(toNode);
        return this;
    }

    /**
     * Embeds a node into the parent node when encountered. It will inject all leaf/nodes into that node. <br/>
     * If you want to embed a node into a node that does not contain this node,
     * then use {@link ClassToStringGenerator#embed(Identifier, Identifier)} instead.
     *
     * @param node the node to embed
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException if {@code node == null}
     */
    public ClassToStringGenerator embed(final Identifier node) {
        return embed(node, null);
    }

    /**
     * Removes all embeddings associated with the node.
     *
     * @param node the node to clear all embeddings for
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException if {@code node == null}
     */
    public ClassToStringGenerator removeEmbeddings(final Identifier node) {
        embeddings.remove(node);
        return this;
    }

    public String generate() {

        // Order
        // 1. Transform names & types
        //     (basically redirects, this is where generics are solved)
        //     Such that Map<Map<String, String>, List<List<MyObject>>> fieldName;
        //     can be redirected to node MyObject by specifying.
        //     redirect(Map.class, fieldName).to(MyObject.class, newName)
        // 3. Filters (stoppers and ignorers)
        //    A stopper is used when encountering a node, it can either say "continue" or "stop".
        //        This means the traversal will not be allowed to enter into the node.
        //    An ignore is used to ignore a certain leaf (could be because it is transient, or just any other reason)
        //    An embedding is used to pull all fields/nodes into that node.


        return "";
    }
}
