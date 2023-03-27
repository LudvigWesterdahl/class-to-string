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
    private final Map<Class<?>, Set<Identifier>> nodes;
    private final Map<Class<?>, Map<Identifier, Identifier>> renaming;
    private final Map<Class<?>, Set<Identifier>> embeddings;
    private final Map<Identifier, Set<Identifier>> externalEmbeddings;
    private final Set<Blocker> blockers;
    private final Set<Observer> observers;

    private ClassToStringGenerator(final Class<?> rootNode,
                                   final Map<Class<?>, Set<Identifier>> nodes,
                                   final Map<Class<?>, Map<Identifier, Identifier>> renaming,
                                   final Map<Class<?>, Set<Identifier>> embeddings,
                                   final Map<Identifier, Set<Identifier>> externalEmbeddings,
                                   final Set<Blocker> blockers,
                                   final Set<Observer> observers) {
        this.rootNode = rootNode;
        this.nodes = nodes;
        this.renaming = renaming;
        this.embeddings = embeddings;
        this.externalEmbeddings = externalEmbeddings;
        this.blockers = blockers;
        this.observers = observers;
    }

    public static ClassToStringGenerator from(final Class<?> rootNode) {
        final Map<Class<?>, Set<Identifier>> nodes = new HashMap<>();
        nodes.computeIfAbsent(null, ignored -> new HashSet<>())
                .add(Identifier.newInstance(rootNode));
        final Map<Class<?>, Map<Identifier, Identifier>> renaming = new HashMap<>();
        final Map<Class<?>, Set<Identifier>> embeddings = new HashMap<>();

        final Set<Field> visited = new HashSet<>();
        final Deque<Field> fields = new ArrayDeque<>(Arrays.asList(rootNode.getDeclaredFields()));
        while (!fields.isEmpty()) {
            final Field field = fields.removeFirst();
            if (visited.contains(field)) {
                continue;
            }
            visited.add(field);

            final CtsName ctsName = field.getAnnotation(CtsName.class);
            Identifier identifier = Identifier.newInstance(field.getType(), field.getName());
            if (ctsName != null && !ReflectionHelper.hasDefaultValues(CtsName.class, ctsName)) {
                final Class<?> newType = ReflectionHelper.getAnnotationValue(
                        CtsName.class,
                        ctsName,
                        Class.class,
                        "type",
                        identifier.getType());
                final String newName = ReflectionHelper.getAnnotationValue(
                        CtsName.class,
                        ctsName,
                        String.class,
                        "name",
                        identifier.getName().orElseThrow());
                final Identifier renamed = Identifier.newInstance(newType, newName);

                renaming.computeIfAbsent(field.getDeclaringClass(), ignored -> new HashMap<>())
                        .put(identifier, renamed);
            }

            final CtsNode ctsNode = field.getAnnotation(CtsNode.class);
            if (ctsNode != null) {
                fields.addAll(Arrays.asList(field.getType().getDeclaredFields()));
                nodes.computeIfAbsent(field.getDeclaringClass(), ignored -> new HashSet<>())
                        .add(identifier);
                if (ctsNode.embed()) {
                    embeddings.computeIfAbsent(field.getDeclaringClass(), ignored -> new HashSet<>())
                            .add(identifier);
                }
            }
        }

        return new ClassToStringGenerator(
                rootNode,
                nodes,
                renaming,
                embeddings,
                new HashMap<>(),
                new HashSet<>(),
                new HashSet<>());
    }

    /**
     * Renames a node or leaf. Multiple calls with the same {@code from} will override the previous calls. <br/>
     * However, if multiple {@code from} {@link Identifier} has been provided where one includes just the type,
     * and others include a name. Then the most specific one will be preferred. <br/>
     * If rename has been provided with a nodeType and one with {@code null}, then the most specific is used.
     *
     * @param nodeType the type of the node,
     *                 if {@code null} then it is the same as {@link ClassToStringGenerator#addName(Identifier, Identifier)}
     * @param from     the real identifier
     * @param to       the new identifier
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException     if {@code from} or {@code to} is {@code null}
     * @throws IllegalArgumentException if {@code to} does not have a name
     */
    public ClassToStringGenerator addName(final Class<?> nodeType, final Identifier from, final Identifier to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        if (to.getName().isEmpty()) {
            throw new IllegalArgumentException("to is missing a name");
        }

        renaming.computeIfAbsent(nodeType, ignored -> new HashMap<>())
                .put(from, to);

        return this;
    }

    /**
     * Renames a node or leaf. Multiple calls with the same {@code from} will override the previous calls. <br/>
     * However, if multiple {@code from} {@link Identifier} has been provided where one includes just the type,
     * and others include a name. Then the most specific one will be preferred. <br/>
     * Note that this method renames all node or leaf found in any node. If a specific one is required,
     * then use {@link ClassToStringGenerator#addName(Class, Identifier, Identifier)} instead.
     *
     * @param from the real identifier
     * @param to   the new identifier
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException     if any argument is null
     * @throws IllegalArgumentException if {@code to} does not have a name
     */
    public ClassToStringGenerator addName(final Identifier from, final Identifier to) {
        return addName(null, from, to);
    }

    /**
     * Removes a renaming.
     *
     * @param nodeType the node
     * @param from     the identifier to remove
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException if {@code from == null}
     */
    public ClassToStringGenerator removeName(final Class<?> nodeType, final Identifier from) {
        Objects.requireNonNull(from);

        final Map<Identifier, Identifier> rename = renaming.get(nodeType);

        if (rename != null) {
            rename.remove(from);
        }

        return this;
    }

    /**
     * Removes a renaming.
     *
     * @param from the identifier to remove
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException if {@code from == null}
     */
    public ClassToStringGenerator removeName(final Identifier from) {
        return removeName(null, from);
    }

    /**
     * Embeds a node into another node. This can be used if you want to inject some fields or nodes into an
     * existing structure without modifying it. <br/>
     * If the field you want to embed is a field in the same class that it should be embedded into, use
     * the {@link ClassToStringGenerator#addEmbedding(Class, Identifier)} instead. <br/>
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
    public ClassToStringGenerator addExternalEmbedding(final Class<?> type, final Identifier toNode) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(toNode);

        externalEmbeddings.computeIfAbsent(toNode, ignored -> new HashSet<>())
                .add(Identifier.newInstance(type));

        return this;
    }

    /**
     * Embeds a node into the parent node when encountered. It will inject all leaf/nodes into that node. <br/>
     * If you want to embed a node into a node that does not contain this node,
     * then use {@link ClassToStringGenerator#addExternalEmbedding(Class, Identifier)} instead. <br/>
     *
     * @param type  the class containing the field
     * @param field the field to embed
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException if {@code field == null}
     */
    public ClassToStringGenerator addEmbedding(final Class<?> type, final Identifier field) {
        Objects.requireNonNull(field);
        embeddings.computeIfAbsent(type, ignored -> new HashSet<>())
                .add(field);
        return this;
    }

    /**
     * Removes all embeddings associated with the node.
     *
     * @param type the class containing the field
     * @param node the node to clear all embeddings for
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException if {@code node == null}
     */
    public ClassToStringGenerator removeEmbedding(final Class<?> type, final Identifier node) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(node);

        final Set<Identifier> typeEmbeddings = embeddings.get(type);
        if (typeEmbeddings != null) {
            typeEmbeddings.remove(node);
        }

        return this;
    }

    /**
     * Specifies that the field should be considered a node when encountered. It will traverse
     * into that structure.
     *
     * @param type the class containing the node field; if null is always a node
     * @param node the node
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException if {@code node == null}
     */
    public ClassToStringGenerator addNode(final Class<?> type, final Identifier node) {
        Objects.requireNonNull(node);

        nodes.computeIfAbsent(type, ignored -> new HashSet<>())
                .add(node);

        return this;
    }

    /**
     * Removes a node. It will be considered a leaf when encountered.
     *
     * @param type the class containing the node field; can be null
     * @param node the node to remove
     * @return this {@link ClassToStringGenerator} instance
     * @throws NullPointerException if {@code node == null}
     */
    public ClassToStringGenerator removeNode(final Class<?> type, final Identifier node) {
        Objects.requireNonNull(node);

        final Set<Identifier> typeNodes = nodes.get(type);
        if (typeNodes != null) {
            typeNodes.remove(node);
        }

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

    private Identifier getRenamedIdentifier(final Identifier node, final Identifier identifier) {
        final Identifier specificRename = Optional.ofNullable(renaming.get(node.getType()))
                .map(r -> r.get(identifier))
                .orElse(null);

        if (specificRename != null) {
            return specificRename;
        }

        return Optional.ofNullable(renaming.get(null))
                .map(r -> r.get(identifier))
                .orElse(null);
    }

    private Identifier getIdentifier(final Identifier node, final Field rawField) {
        final Class<?> type = rawField.getType();
        final String name = rawField.getName();
        final Identifier generalIdentifier = Identifier.newInstance(type);
        final Identifier renamedGeneralIdentifier = getRenamedIdentifier(node, generalIdentifier);

        final Identifier specificIdentifier = Identifier.newInstance(type, name);
        final Identifier renamedSpecificIdentifier = getRenamedIdentifier(node, specificIdentifier);

        if (renamedSpecificIdentifier != null) {
            return renamedSpecificIdentifier;
        }

        if (renamedGeneralIdentifier != null) {
            return renamedGeneralIdentifier;
        }

        return specificIdentifier;
    }

    private boolean isEmbedded(final Identifier identifier, final Field rawField) {
        final Set<Identifier> generalEmbeddings = embeddings.get(null);
        if (generalEmbeddings != null
                && (generalEmbeddings.contains(identifier) || generalEmbeddings.contains(identifier.stripName()))) {
            return true;
        }

        final Set<Identifier> typeEmbeddings = embeddings.get(rawField.getDeclaringClass());
        if (typeEmbeddings == null) {
            return false;
        }

        if (typeEmbeddings.contains(identifier)) {
            return true;
        }

        return typeEmbeddings.contains(identifier.stripName());
    }

    private boolean isNode(final Identifier identifier, final Field rawField) {
        final Set<Identifier> generalNodes = nodes.get(null);
        if (generalNodes != null
                && (generalNodes.contains(identifier) || generalNodes.contains(identifier.stripName()))) {
            return true;
        }

        final Set<Identifier> typeNodes = nodes.get(rawField.getDeclaringClass());
        if (typeNodes == null) {
            return false;
        }

        if (typeNodes.contains(identifier)) {
            return true;
        }

        return typeNodes.contains(identifier.stripName());
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
        if (originalNode.matches(node)) {
            throw new IllegalStateException("illegal loop detected");
        }

        final Identifier currentNode = node == null
                ? originalNode
                : node;

        final List<CtsField> fields = new ArrayList<>();
        for (final Field rawField : currentNode.getType().getDeclaredFields()) {
            final int modifiers = rawField.getModifiers();
            final Identifier identifier = Identifier.newInstance(rawField.getType(), rawField.getName());

            if (isNode(identifier, rawField)) {
                if (isEmbedded(identifier, rawField)) {
                    final List<CtsField> embeddedFields = getFields(originalNode, identifier);
                    fields.addAll(embeddedFields);
                } else {
                    final CtsField field = CtsField.newNode(identifier, modifiers);
                    fields.add(field);
                }
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
            final boolean blocked = isBlocked(current);

            if (enteredNodes.contains(current)) {
                notifyLeaveNode(current);
            } else if (!blocked && !current.head().isNode()) {
                notifyConsumeLeaf(current);
            } else if (!blocked) {
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
