package se.ludvigwesterdahl.lib.cts.strategy;

import se.ludvigwesterdahl.lib.cts.CtsFieldChain;
import se.ludvigwesterdahl.lib.cts.Observer;

import java.util.List;

public interface GenerationStrategy extends Observer {

    String generate();
}
