package se.ludvigwesterdahl.lib.cts.strategy;

import se.ludvigwesterdahl.lib.cts.Observer;

public interface GenerationStrategy extends Observer {

    String generate();
}
