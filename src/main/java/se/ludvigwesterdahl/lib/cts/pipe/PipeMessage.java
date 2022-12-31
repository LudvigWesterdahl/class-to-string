package se.ludvigwesterdahl.lib.cts.pipe;

import java.util.Objects;

public final class PipeMessage {

    public enum MessageType {
        NODE, LEAF;
    }

    private final MessageType messageType;
    private final Class<?> type;
    private final String name;

    public static final class Builder {

        private final MessageType messageType;
        private Class<?> type;
        private String name;

        public Builder(final MessageType messageType) {
            this.messageType = Objects.requireNonNull(messageType);
        }

        public Builder withType(final Class<?> type) {
            this.type = Objects.requireNonNull(type);
            return this;
        }

        public Builder withName(final String name) {
            this.name = Objects.requireNonNull(name);
            return this;
        }

        public PipeMessage build() {
            return new PipeMessage(this);
        }
    }

    private PipeMessage(final Builder builder) {
        messageType = builder.messageType;
        type = builder.type;
        name = builder.name;
    }

    public MessageType messageType() {
        return messageType;
    }

    public Class<?> type() {
        return type;
    }

    public String name() {
        return name;
    }

    public Builder toBuilder() {
        return new Builder(messageType)
                .withType(type)
                .withName(name);
    }
}
