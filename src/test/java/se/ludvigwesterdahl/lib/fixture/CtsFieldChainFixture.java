package se.ludvigwesterdahl.lib.fixture;

import se.ludvigwesterdahl.lib.cts.CtsField;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;
import se.ludvigwesterdahl.lib.cts.Identifier;

import java.lang.reflect.Modifier;
import java.util.List;

public final class CtsFieldChainFixture {

    private CtsFieldChainFixture() {
        throw new AssertionError("this private constructor is suppressed");
    }

    public static CtsFieldChain appendPrivateLeaf(final CtsFieldChain nodeFieldChain,
                                           final Class<?> type,
                                           final String name) {
        return nodeFieldChain.chainAll(List.of(
                CtsField.newLeaf(Identifier.newInstance(type, name), Modifier.PRIVATE)
        )).get(0);
    }

    public static CtsFieldChain appendPrivateNode(final CtsFieldChain nodeFieldChain,
                                           final Class<?> type,
                                           final String name) {
        return nodeFieldChain.chainAll(List.of(
                CtsField.newNode(Identifier.newInstance(type, name), Modifier.PRIVATE)
        )).get(0);
    }
}
