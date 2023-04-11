package se.ludvigwesterdahl.lib.cts;

import se.ludvigwesterdahl.lib.cts.strategy.GenerationStrategy;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * The following methods can be achieved using the annotations, {@link CtsNode} and {@link CtsName}.
 * <ul>
 *     <li>{@link ClassToStringGenerator#addName(Class, Identifier, Identifier)}</li>
 *     <li>{@link ClassToStringGenerator#removeName(Class, Identifier)}</li>
 *     <li>{@link ClassToStringGenerator#addEmbedding(Class, Identifier)}</li>
 *     <li>{@link ClassToStringGenerator#removeEmbedding(Class, Identifier)}</li>
 *     <li>{@link ClassToStringGenerator#addNode(Class, Identifier)}</li>
 *     <li>{@link ClassToStringGenerator#removeNode(Class, Identifier)}</li>
 * </ul>
 * <br/>
 * However, the following methods can be used to set the global name, irrespectively of the class
 * which the fields have been specified in.
 * <ul>
 *     <li>{@link ClassToStringGenerator#addName(Identifier, Identifier)}</li>
 *     <li>{@link ClassToStringGenerator#removeName(Identifier)}</li>
 * </ul>
 * <br/>
 * Because of this, you need to call the first set of "remove" methods if you want to remove
 * the effect of an annotation by code.
 */
public final class ClassToStringGenerator {

    private final Class<?> rootNode;
    private final Map<Class<?>, Set<Identifier>> nodes;
    private final Map<Class<?>, Map<Identifier, Identifier>> names;
    private final Map<Class<?>, Set<Identifier>> embeddings;
    private final List<Blocker> blockers;
    private final List<Observer> observers;

    private ClassToStringGenerator(final Class<?> rootNode,
                                   final Map<Class<?>, Set<Identifier>> nodes,
                                   final Map<Class<?>, Map<Identifier, Identifier>> names,
                                   final Map<Class<?>, Set<Identifier>> embeddings,
                                   final List<Blocker> blockers,
                                   final List<Observer> observers) {
        this.rootNode = rootNode;
        this.nodes = nodes;
        this.names = names;
        this.embeddings = embeddings;
        this.blockers = blockers;
        this.observers = observers;
    }

    public static ClassToStringGenerator from(final Class<?> rootNode) {
        final Map<Class<?>, Set<Identifier>> nodes = new HashMap<>();
        nodes.computeIfAbsent(null, ignored -> new HashSet<>())
                .add(Identifier.newInstance(rootNode));
        final Map<Class<?>, Map<Identifier, Identifier>> names = new HashMap<>();
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

            final Identifier identifier = Identifier.newInstance(field.getType(), field.getName());
            Class<?> type = field.getType();
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

                names.computeIfAbsent(field.getDeclaringClass(), ignored -> new HashMap<>())
                        .put(identifier, renamed);
                type = newType;
            }

            final CtsNode ctsNode = field.getAnnotation(CtsNode.class);
            if (ctsNode != null) {
                // Using the possibly redirected type.
                fields.addAll(Arrays.asList(type.getDeclaredFields()));
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
                names,
                embeddings,
                new ArrayList<>(),
                new ArrayList<>());
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

        names.computeIfAbsent(nodeType, ignored -> new HashMap<>())
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

        final Map<Identifier, Identifier> rename = names.get(nodeType);

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
     * Embeds a node into the parent node when encountered. It will inject all leaf/nodes into that node. <br/>
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
        final Identifier specificRename = Optional.ofNullable(node)
                .map(n -> names.get(n.getType()))
                .map(r -> r.get(identifier))
                .orElse(null);

        if (specificRename != null) {
            return specificRename;
        }

        return Optional.ofNullable(names.get(null))
                .map(r -> r.get(identifier))
                .orElse(null);
    }

    private Identifier getIdentifier(final Identifier previousNode, final Identifier nodeOrLeaf) {
        final Identifier renamedSpecificIdentifier = getRenamedIdentifier(previousNode, nodeOrLeaf);
        final Identifier renamedGeneralIdentifier = getRenamedIdentifier(previousNode, nodeOrLeaf.stripName());

        if (renamedSpecificIdentifier != null) {
            return renamedSpecificIdentifier;
        }

        if (renamedGeneralIdentifier != null) {
            return renamedGeneralIdentifier;
        }

        return nodeOrLeaf;
    }

    private boolean isEmbedded(final Identifier previousNode, final Identifier node) {
        final Set<Identifier> generalEmbeddings = embeddings.get(null);
        if (generalEmbeddings != null
                && (generalEmbeddings.contains(node) || generalEmbeddings.contains(node.stripName()))) {
            return true;
        }

        final Set<Identifier> typeEmbeddings = Optional.ofNullable(previousNode)
                .map(p -> embeddings.get(p.getType()))
                .orElse(null);
        if (typeEmbeddings == null) {
            return false;
        }

        if (typeEmbeddings.contains(node)) {
            return true;
        }

        return typeEmbeddings.contains(node.stripName());
    }

    private boolean isNode(final Identifier previousNode, final Identifier node) {
        final Set<Identifier> generalNodes = nodes.get(null);
        if (generalNodes != null
                && (generalNodes.contains(node) || generalNodes.contains(node.stripName()))) {
            return true;
        }

        final Set<Identifier> typeNodes = Optional.ofNullable(previousNode)
                .map(p -> nodes.get(p.getType()))
                .orElse(null);
        if (typeNodes == null) {
            return false;
        }

        if (typeNodes.contains(node)) {
            return true;
        }

        return typeNodes.contains(node.stripName());
    }

    private List<CtsField> getFields(final Identifier previousNode, final Identifier node) {
        final Set<Field> visited = new HashSet<>();
        final Identifier renamedNode = getIdentifier(previousNode, node);
        final ArrayDeque<Field> rawFields = new ArrayDeque<>(List.of(renamedNode.getType().getDeclaredFields()));
        // Used to keep track of the container nodes for embedded fields.
        final Map<Identifier, Identifier> fieldIdentifierToNode = new HashMap<>();

        final List<CtsField> fields = new ArrayList<>();
        while (!rawFields.isEmpty()) {
            final Field rawField = rawFields.removeFirst();
            if (visited.contains(rawField)) {
                // This happens when embeddings create an infinite loop.
                throw new IllegalStateException("illegal loop detected");
            }
            visited.add(rawField);

            final Identifier fieldIdentifier = Identifier.newInstance(rawField.getType(), rawField.getName());
            final Identifier parentNode = fieldIdentifierToNode.getOrDefault(fieldIdentifier, node);
            final Identifier renamedFieldIdentifier = getIdentifier(parentNode, fieldIdentifier);

            if (isNode(parentNode, fieldIdentifier)) {
                if (isEmbedded(parentNode, fieldIdentifier)) {
                    final Field[] embedRawFields = renamedFieldIdentifier.getType().getDeclaredFields();
                    for (int i = embedRawFields.length - 1; i >= 0; i--) {
                        final Field embedRawField = embedRawFields[i];
                        rawFields.addFirst(embedRawField);
                        final Identifier embedFieldIdentifier
                                = Identifier.newInstance(embedRawField.getType(), embedRawField.getName());
                        fieldIdentifierToNode.put(embedFieldIdentifier, renamedFieldIdentifier);
                    }
                } else {
                    final CtsField field = CtsField.newNode(renamedFieldIdentifier, rawField.getModifiers());
                    fields.add(field);
                }
            } else {
                final CtsField field = CtsField.newLeaf(renamedFieldIdentifier, rawField.getModifiers());
                fields.add(field);
            }
        }
        return fields;
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
                final Identifier previousNode = current.isRoot()
                        ? null
                        : current.allFields().get(current.allFields().size() - 2).getIdentifier();
                final List<CtsField> fields = getFields(previousNode, current.head().getIdentifier());
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
