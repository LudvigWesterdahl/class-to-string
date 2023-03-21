package se.ludvigwesterdahl.lib.fixture.ctstestcases;

import se.ludvigwesterdahl.lib.cts.ClassToStringGenerator;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;
import se.ludvigwesterdahl.lib.cts.CtsNode;
import se.ludvigwesterdahl.lib.cts.Identifier;
import se.ludvigwesterdahl.lib.cts.blockers.LoopBlocker;

import java.util.List;

import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateLeaf;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateNode;
import static se.ludvigwesterdahl.lib.fixture.GenerationStrategyFixture.newDefaultFlatGenerationStrategy;
import static se.ludvigwesterdahl.lib.fixture.ctstestcases.CtsNotification.Type.*;
import static se.ludvigwesterdahl.lib.fixture.ctstestcases.CtsNotification.notification;

public final class FixEmbedAnnotationCycle implements CtsTestCaseGroup {

    @SuppressWarnings("unused")
    private static final class OneLevel {

        @CtsNode(embed = true)
        private OneLevel oneLevel;
        private String string1;
    }

    @SuppressWarnings("unused")
    private static final class TwoLevel {

        @CtsNode(embed = true)
        private First first;
        private String string1;

        private static final class First {

            private TwoLevel twoLevel;
            private String string2;
        }
    }

    @SuppressWarnings("unused")
    private static final class ThreeLevel {

        @CtsNode(embed = true)
        private First first;
        private String string1;

        private static final class First {

            @CtsNode(embed = true)
            private Second second;
            private String string2;

            private static final class Second {

                @CtsNode(embed = true)
                private ThreeLevel threeLevel;
                private String string3;
            }
        }
    }

    private static final class OneLevelCycle implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(OneLevel.class)
                    .addBlocker(LoopBlocker.loop(Identifier.newInstance(OneLevel.class), 2))
                    .removeEmbeddings(Identifier.newInstance(OneLevel.class, "oneLevel"))
                    .addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain root = CtsFieldChain.newRootInstance(OneLevel.class);
            final CtsFieldChain node1 = appendPrivateNode(root, OneLevel.class, "oneLevel");
            final CtsFieldChain leaf1 = appendPrivateLeaf(root, String.class, "string1");
            final CtsFieldChain leaf2 = appendPrivateNode(node1, String.class, "string1");
            final CtsFieldChain node2 = appendPrivateNode(node1, OneLevel.class, "oneLevel");
            final CtsFieldChain leaf3 = appendPrivateLeaf(node2, String.class, "string1");

            return List.of(
                    notification(ENTER_NODE, root),
                    notification(ENTER_NODE, node1),
                    notification(ENTER_NODE, node2),
                    notification(CONSUME_LEAF, leaf3),
                    notification(LEAVE_NODE, node2),
                    notification(CONSUME_LEAF, leaf2),
                    notification(LEAVE_NODE, node1),
                    notification(CONSUME_LEAF, leaf1),
                    notification(LEAVE_NODE, root)
            );
        }

        @Override
        public String expectedGenerate() {
            return "oneLevel/oneLevel/string1,oneLevel/string1,string1";
        }
    }

    private static final class TwoLevelCycle implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            return null;
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            return null;
        }

        @Override
        public String expectedGenerate() {
            return null;
        }
    }

    private static final class ThreeLevelCycle implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            return null;
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            return null;
        }

        @Override
        public String expectedGenerate() {
            return null;
        }
    }

    @Override
    public List<CtsTestCase> testCases() {
        return List.of(
                new OneLevelCycle(),
                new TwoLevelCycle(),
                new ThreeLevelCycle()
        );
    }
}
