package se.ludvigwesterdahl.lib.cts;

public interface Blocker extends Observer {

    /**
     * This method is called before processing a field. It will be invoked before any
     * method in {@link Observer}. <br/>
     * See some implementation hints on {@link ClassToStringGenerator#addBlocker(Blocker)}.
     *
     * @param fieldChain the field about to be processed
     * @return {@code true} if the field should not be processed and {@code false} otherwise
     */
    boolean block(CtsFieldChain fieldChain);
}
