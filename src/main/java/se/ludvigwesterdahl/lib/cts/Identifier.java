package se.ludvigwesterdahl.lib.cts;

import java.net.IDN;
import java.util.Objects;
import java.util.Optional;

public final class Identifier {

    private final Class<?> nodeType;
    private final String nodeName;
    private final Class<?> leafType;
    private final String leafName;
    private final int hashCode;

    private Identifier(final Class<?> nodeType, final String nodeName, final Class<?> leafType, final String leafName) {
        this.nodeType = nodeType;
        this.nodeName = nodeName;
        this.leafType = leafType;
        this.leafName = leafName;

        hashCode = Objects.hash(nodeType, nodeName, leafType, leafName);
    }

    public static Identifier newLeaf(final Class<?> nodeType,
                                     final String nodeName,
                                     final Class<?> leafType,
                                     final String leafName) {
        Objects.requireNonNull(nodeType);
        Objects.requireNonNull(nodeName);
        Objects.requireNonNull(leafType);
        Objects.requireNonNull(leafName);

        if (nodeName.isBlank()) {
            throw new IllegalArgumentException("nodeName cannot be blank");
        }

        if (leafName.isBlank()) {
            throw new IllegalArgumentException("leafName cannot be blank");
        }

        return new Identifier(nodeType, nodeName, leafType, leafName);
    }

    public static Identifier newNode(final Class<?> nodeType) {
        Objects.requireNonNull(nodeType);

        return new Identifier(nodeType, null, null, null);
    }

    public static Identifier newNode(final Class<?> nodeType, final String nodeName) {
        Objects.requireNonNull(nodeType);
        Objects.requireNonNull(nodeName);

        if (nodeName.isBlank()) {
            throw new IllegalArgumentException("nodeName cannot be blank");
        }

        return new Identifier(nodeType, nodeName, null, null);
    }

    public Class<?> getNodeType() {
        return nodeType;
    }

    public Optional<String> getNodeName() {
        return Optional.ofNullable(nodeName);
    }

    public Optional<Class<?>> getLeafType() {
        return Optional.ofNullable(leafType);
    }

    public Optional<String> getLeafName() {
        return Optional.ofNullable(leafName);
    }

    public boolean isNode() {
        return getLeafType().isEmpty();
    }

    public boolean isLeaf() {
        return !isNode();
    }

    public Identifier mapLeaf(final Class<?> newLeafType, final String newLeafName) {
        if (isNode()) {
            throw new IllegalStateException("cannot map a node to a leaf");
        }

        return newLeaf(nodeType, nodeName, newLeafType, newLeafName);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Identifier)) {
            return false;
        }

        final Identifier identifier = (Identifier) o;

        if (identifier.hashCode != hashCode) {
            return false;
        }

        return nodeType == identifier.nodeType
                && Objects.equals(nodeName, identifier.nodeName)
                && Objects.equals(leafType, identifier.leafType)
                && Objects.equals(leafName, identifier.leafName);
    }

    @Override
    public String toString() {
        return String.format("%s[nodeType=%s, nodeName=%s, leafType=%s, leafName=%s]",
                getClass().getCanonicalName(), nodeType, nodeName, leafType, leafName);
    }
}
