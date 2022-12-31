package se.ludvigwesterdahl.lib.cts.pipe;

public interface PipeActor {

    void startNode(Class<?> type);

    /**
     * Return null if message should not proceed further.
     *
     * @param message the message
     * @return message or null
     */
    PipeMessage act(PipeMessage message);

    void endNode(Class<?> type);
}
