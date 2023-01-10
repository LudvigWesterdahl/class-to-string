package se.ludvigwesterdahl.lib.cts;

import se.ludvigwesterdahl.lib.cts.strategy.GenerationStrategy;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * TODO: explain this deeper
 * Renaming happens first.
 * Then embeddings. This means that an embedding can override a node.
 * Then checking if a field is a node.
 * Then blocking.
 * Then notifying.
 */
public final class ClassToStringGenerator {

    private final Class<?> rootNode;
    private final Set<Identifier> nodes;
    private final Map<Identifier, Identifier> renaming;
    private final Map<Identifier, Set<Identifier>> externalEmbeddings;
    private final Set<Identifier> embeddings;
    private final Set<Blocker> blockers;
    private final Set<Observer> observers;

    private ClassToStringGenerator(final Class<?> rootNode,
                                   final Set<Identifier> nodes,
                                   final Map<Identifier, Identifier> renaming,
                                   final Map<Identifier, Set<Identifier>> externalEmbeddings,
                                   final Set<Identifier> embeddings,
                                   final Set<Blocker> blockers,
                                   final Set<Observer> observers) {
        this.rootNode = rootNode;
        this.nodes = nodes;
        this.renaming = renaming;
        this.externalEmbeddings = externalEmbeddings;
        this.embeddings = embeddings;
        this.blockers = blockers;
        this.observers = observers;
    }

    public static ClassToStringGenerator from(final Class<?> rootNode) {
        // Compute structure based on annotations.

        final Set<Identifier> nodes = new HashSet<>();
        nodes.add(Identifier.newInstance(rootNode));

        return new ClassToStringGenerator(rootNode,
                nodes,
                new HashMap<>(),
                new HashMap<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>());
    }

    /**
     * Renames a node or leaf. Multiple calls with the same {@code from} will override the previous calls. <br/>
     * However, if multiple {@code from} {@link Identifier} has been provided where one includes just the type,
     * and others include a name. Then the most specific one will be preferred.
     *
     * @param from the real identifier
     * @param to   the new identifier
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException     if any argument is null
     * @throws IllegalArgumentException if {@code to} does not have a name
     */
    public ClassToStringGenerator rename(final Identifier from, final Identifier to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        if (to.getName().isEmpty()) {
            throw new IllegalArgumentException("to is missing a name");
        }

        renaming.put(from, to);

        return this;
    }

    /**
     * Removes a renaming.
     *
     * @param from the identifier to remove
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException if {@code from == null}
     */
    public ClassToStringGenerator removeRename(final Identifier from) {
        Objects.requireNonNull(from);

        renaming.remove(from);

        return this;
    }

    /**
     * Embeds a node into another node. This can be used if you want to inject some fields or nodes into an
     * existing structure without modifying it. <br/>
     * TODO: might not be true: Note that embeddings are only performed if no {@link Blocker} blocks the node from being entered. <br/>
     * TODO: check this javadoc to ensure it is correct
     * If the field you want to embed is a field in the same class that it should be embedded into, use
     * the {@link ClassToStringGenerator#embed(Identifier)} instead. <br/>
     * Multiple calls will override previous calls, however, if multiple {@code from} {@link Identifier}
     * has been provided as {@code toNode} where one includes just the type, and others include a name. Then
     * the most specific one will be preferred. <br/>
     * <b>Example</b> <br/>
     * <pre>
     * {@code
     * embed(FullName.class, Identifier.newInstance(Person.class))
     * embed(FirstName.class, Identifier.newInstance(Person.class, "erik"))
     * embed(LastName.class, Identifier.newInstance(Person.class, "erik"))
     * }
     * </pre>
     * Then the following will happen given when encountering fields. <br/>
     * Assume the Person class looks like this. <br/>
     * <pre>
     * {@code
     * public class Person {
     *     private String ssn;
     *     private int age;
     * }
     * }
     * </pre>
     * <pre>
     * {@code
     * public class SomeClass {
     *     private Person john; // FullName.class will be embedded into Person
     *     private Person erik; // FirstName.class and LastName.class will be embedded into Person
     * }
     * }
     * </pre>
     * What this does can be though of as the field {@code john} extending {@code Person extends FullName}
     * and the field {@code erik} having type {@code Person extends FirstName, LastName}
     * <p>
     * Embeddings can also be combined with renaming to handle generics. <br/>
     * <b>Example</b><br/>
     * <pre>
     * {@code
     * rename(Identifier.newInstance(List.class, "results"), Identifier.newInstance(Person.class, "results"))
     * embed(Identifier.newInstance(Person.class))
     *
     * public class TheClass {
     *     private List<Person> results; // Person.class will be embedded
     * }
     * }
     * </pre>
     * Then what happens is the following. <br/> <br/>
     * <b>Step 1 - renaming</b> <br/>
     * <pre>
     * {@code
     * public class TheClass {
     *     private Person results;
     * }
     * }
     * </pre>
     * <b>Step 2 - embedding</b> <br/>
     * <pre>
     * {@code
     * public class TheClass {
     *     private String ssn;
     *     private int age;
     * }
     * }
     * </pre>
     *
     * @param type   the type to embed
     * @param toNode the destination node
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException if any argument is null
     */
    public ClassToStringGenerator embed(final Class<?> type, final Identifier toNode) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(toNode);

        externalEmbeddings.computeIfAbsent(toNode, ignored -> new HashSet<>())
                .add(Identifier.newInstance(type));

        return this;
    }

    /**
     * Embeds a node into the parent node when encountered. It will inject all leaf/nodes into that node. <br/>
     * If you want to embed a node into a node that does not contain this node,
     * then use {@link ClassToStringGenerator#embed(Class, Identifier)} instead. <br/>
     *
     * @param field the field to embed
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException if {@code field == null}
     */
    public ClassToStringGenerator embed(final Identifier field) {
        Objects.requireNonNull(field);
        embeddings.add(field);
        return this;
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

    /**
     * Specifies that the field should be considered a node when encountered. It will traverse
     * into that structure.
     *
     * @param node the node
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException if {@code node == null}
     */
    public ClassToStringGenerator addNode(final Identifier node) {
        Objects.requireNonNull(node);

        nodes.add(node);

        return this;
    }

    /**
     * Removes a node. It will be considered a leaf when encountered.
     *
     * @param node the node to remove
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException if {@code node == null}
     */
    public ClassToStringGenerator removeNode(final Identifier node) {
        Objects.requireNonNull(node);

        nodes.remove(node);

        return this;
    }

    /**
     * Adds an observer.
     *
     * @param observer the observer to add
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException if {@code observer == null}
     */
    public ClassToStringGenerator addObserver(final Observer observer) {
        Objects.requireNonNull(observer);
        observers.add(observer);
        return this;
    }

    /**
     * Adds a blocker. This method will also call {@link ClassToStringGenerator#addObserver(Observer)}. <br/>
     * If any {@link Blocker} blocks a field from being processed, then the rest will not be called on that field.
     * In other words, it will short-circuit.
     *
     * @param blocker the blocker to add
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException if {@code blocker == null}
     */
    public ClassToStringGenerator addBlocker(final Blocker blocker) {
        Objects.requireNonNull(blocker);
        blockers.add(blocker);
        return addObserver(blocker);
    }

    private Identifier getIdentifier(final Class<?> type, final String name) {
        final Identifier generalIdentifier = Identifier.newInstance(type);
        final Identifier renamedGeneralIdentifier = renaming.get(generalIdentifier);
        final Identifier specificIdentifier = Identifier.newInstance(type, name);
        final Identifier renamedSpecificIdentifier = renaming.get(specificIdentifier);

        if (renamedSpecificIdentifier != null) {
            return renamedSpecificIdentifier;
        }

        if (renamedGeneralIdentifier != null) {
            return renamedGeneralIdentifier;
        }

        return specificIdentifier;
    }

    private boolean isEmbedded(final Identifier identifier) {
        if (embeddings.contains(identifier)) {
            return true;
        }

        return embeddings.contains(identifier.stripName());
    }

    private boolean isNode(final Identifier identifier) {
        if (nodes.contains(identifier)) {
            return true;
        }

        return nodes.contains(identifier.stripName());
    }

    private Set<Identifier> getExternalEmbeddings(final Identifier node) {
        final Set<Identifier> specificEmbeddings = externalEmbeddings.get(node);
        if (specificEmbeddings != null) {
            return specificEmbeddings;
        }

        final Set<Identifier> generalEmbeddings = externalEmbeddings.get(node.stripName());
        if (generalEmbeddings != null) {
            return generalEmbeddings;
        }

        return Set.of();
    }

    private List<CtsField> getFields(final Identifier originalNode, final Identifier node) {
        if (Objects.equals(originalNode, node)) {
            throw new IllegalStateException("illegal loop detected");
        }

        final Identifier currentNode = node == null ? originalNode : node;

        final List<CtsField> fields = new ArrayList<>();
        for (final Field rawField : currentNode.getType().getDeclaredFields()) {
            final Class<?> type = rawField.getType();
            final String name = rawField.getName();
            final int modifiers = rawField.getModifiers();

            final Identifier identifier = getIdentifier(type, name);

            if (isNode(identifier) && isEmbedded(identifier)) {
                final List<CtsField> embeddedFields = getFields(originalNode, identifier);
                fields.addAll(embeddedFields);
            } else if (isNode(identifier)) {
                final CtsField field = CtsField.newNode(identifier, modifiers);
                fields.add(field);
            } else {
                final CtsField field = CtsField.newLeaf(identifier, modifiers);
                fields.add(field);
            }
        }

        // Checks for any remote embeddings into this node.
        final Set<Identifier> currentNodeExternalEmbeddings = getExternalEmbeddings(currentNode);
        for (final Identifier identifier : currentNodeExternalEmbeddings) {
            final List<CtsField> embeddedFields = getFields(originalNode, identifier);
            fields.addAll(embeddedFields);
        }

        return fields;
    }

    private List<CtsField> getFields(final Identifier node) {
        return getFields(node, null);
    }

    private boolean isBlocked(final CtsFieldChain fieldChain) {
        for (final Blocker blocker : blockers) {
            if (blocker.block(fieldChain)) {
                return true;
            }
        }

        return false;
    }

    private void notifyAllObservers(final BiConsumer<Observer, CtsFieldChain> consumer,
                                    final CtsFieldChain fieldChain) {
        for (final Observer observer : observers) {
            consumer.accept(observer, fieldChain);
        }
    }

    private void notifyEnterNode(CtsFieldChain nodeFieldChain) {
        notifyAllObservers(Observer::enterNode, nodeFieldChain);
    }

    private void notifyConsumeLeaf(CtsFieldChain leafFieldChain) {
        notifyAllObservers(Observer::consumeLeaf, leafFieldChain);
    }

    private void notifyLeaveNode(CtsFieldChain nodeFieldChain) {
        notifyAllObservers(Observer::leaveNode, nodeFieldChain);
    }

    public List<GenerationStrategy> iterate() {
        final Set<CtsFieldChain> enteredNodes = new HashSet<>();
        final ArrayDeque<CtsFieldChain> queue = new ArrayDeque<>();

        final CtsFieldChain rootFieldChain = CtsFieldChain.newRootInstance(rootNode);
        queue.addFirst(rootFieldChain);

        while (!queue.isEmpty()) {
            final CtsFieldChain current = queue.removeFirst();
            if (isBlocked(current)) {
                continue;
            }

            if (enteredNodes.contains(current)) {
                notifyLeaveNode(current);

            } else if (!current.head().isNode()) {
                notifyConsumeLeaf(current);

            } else {
                notifyEnterNode(current);
                enteredNodes.add(current);
                queue.addFirst(current);

                final List<CtsField> fields = getFields(current.head().getIdentifier());
                final List<CtsFieldChain> nextFieldChains = current.chainAll(fields);
                for (int i = nextFieldChains.size() - 1; i >= 0; i--) {
                    queue.addFirst(nextFieldChains.get(i));
                }
            }
        }

        return observers.stream()
                .filter(o -> (o instanceof GenerationStrategy))
                .map(o -> (GenerationStrategy) o)
                .collect(Collectors.toList());
    }
}
