package se.ludvigwesterdahl.lib.fixture.ctstestcases;

import se.ludvigwesterdahl.lib.cts.ClassToStringGenerator;
import se.ludvigwesterdahl.lib.cts.CtsFieldChain;
import se.ludvigwesterdahl.lib.cts.CtsName;
import se.ludvigwesterdahl.lib.cts.CtsNode;
import se.ludvigwesterdahl.lib.cts.blockers.LeafBlocker;
import se.ludvigwesterdahl.lib.cts.strategy.FlatGenerationStrategy;

import java.net.URI;
import java.util.List;

import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateLeaf;
import static se.ludvigwesterdahl.lib.fixture.CtsFieldChainFixture.appendPrivateNode;
import static se.ludvigwesterdahl.lib.fixture.GenerationStrategyFixture.newDefaultFlatGenerationStrategy;
import static se.ludvigwesterdahl.lib.fixture.ctstestcases.CtsNotification.Type.*;
import static se.ludvigwesterdahl.lib.fixture.ctstestcases.CtsNotification.notification;

public final class TypicalSelectExpandQuery implements CtsTestCaseGroup {

    @SuppressWarnings("unused")
    private static final class Response {

        @CtsNode(embed = true)
        private EmployeeResponse employeeResponse;

        private static final class EmployeeResponse {

            @CtsNode(embed = true)
            @CtsName(type = EmployeeResult.class)
            private List<EmployeeResult> results;
            private int count;
            private URI nextPage;

            private static final class EmployeeResult {

                @CtsName(name = "identifier")
                private String employeeId;
                @CtsNode
                @CtsName(name = "personalInformation")
                private PersonalInformationResponse personalInformationResponse;
                @CtsNode
                @CtsName(name = "contact")
                private ContactResponse contactResponse;

                private static final class PersonalInformationResponse {

                    private String firstName;
                    private String lastName;
                }

                private static final class ContactResponse {

                    @CtsNode(embed = true)
                    @CtsName(type = ContactResult.class)
                    private List<ContactResult> results;

                    private static final class ContactResult {

                        private String type;
                        private String value;
                    }
                }
            }
        }
    }

    private static final class Select implements CtsTestCase {

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(Response.class)
                    .addObserver(newDefaultFlatGenerationStrategy());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain root = CtsFieldChain.newRootInstance(Response.class);
            final CtsFieldChain leaf1 = appendPrivateLeaf(root,
                    String.class,
                    "identifier");
            final CtsFieldChain node1 = appendPrivateNode(root,
                    Response.EmployeeResponse.EmployeeResult.PersonalInformationResponse.class,
                    "personalInformation");
            final CtsFieldChain leaf2 = appendPrivateLeaf(node1,
                    String.class,
                    "firstName");
            final CtsFieldChain leaf3 = appendPrivateLeaf(node1,
                    String.class,
                    "lastName");
            final CtsFieldChain node2 = appendPrivateNode(root,
                    Response.EmployeeResponse.EmployeeResult.ContactResponse.class,
                    "contact");
            final CtsFieldChain leaf4 = appendPrivateLeaf(node2,
                    String.class,
                    "type");
            final CtsFieldChain leaf5 = appendPrivateLeaf(node2,
                    String.class,
                    "value");
            final CtsFieldChain leaf6 = appendPrivateLeaf(root,
                    int.class,
                    "count");
            final CtsFieldChain leaf7 = appendPrivateLeaf(root,
                    URI.class,
                    "nextPage");

            return List.of(
                    notification(ENTER_NODE, root),
                    notification(CONSUME_LEAF, leaf1),
                    notification(ENTER_NODE, node1),
                    notification(CONSUME_LEAF, leaf2),
                    notification(CONSUME_LEAF, leaf3),
                    notification(LEAVE_NODE, node1),
                    notification(ENTER_NODE, node2),
                    notification(CONSUME_LEAF, leaf4),
                    notification(CONSUME_LEAF, leaf5),
                    notification(LEAVE_NODE, node2),
                    notification(CONSUME_LEAF, leaf6),
                    notification(CONSUME_LEAF, leaf7),
                    notification(LEAVE_NODE, root)
            );
        }

        @Override
        public String expectedGenerate() {
            return String.join(",",
                    "identifier",
                    "personalInformation/firstName",
                    "personalInformation/lastName",
                    "contact/type",
                    "contact/value",
                    "count",
                    "nextPage"
            );
        }
    }

    private static final class Expand implements CtsTestCase {

        private final CtsTestCase select = new Select();

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(Response.class)
                    .addObserver(new FlatGenerationStrategy.Builder()
                            .withNodes(true)
                            .withLeaf(false)
                            .build());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            return select.expectedNotifications();
        }

        @Override
        public String expectedGenerate() {
            return String.join(",",
                    "personalInformation",
                    "contact"
            );
        }
    }

    private static final class ExpandWithBlocker implements CtsTestCase {

        private final CtsTestCase expand = new Expand();

        @Override
        public ClassToStringGenerator generator() {
            return ClassToStringGenerator.from(Response.class)
                    .addBlocker(LeafBlocker.getInstance())
                    .addObserver(new FlatGenerationStrategy.Builder()
                            .withNodes(true)
                            .withLeaf(true)
                            .build());
        }

        @Override
        public List<CtsNotification> expectedNotifications() {
            final CtsFieldChain root = CtsFieldChain.newRootInstance(Response.class);
            final CtsFieldChain node1 = appendPrivateNode(root,
                    Response.EmployeeResponse.EmployeeResult.PersonalInformationResponse.class,
                    "personalInformation");
            final CtsFieldChain node2 = appendPrivateNode(root,
                    Response.EmployeeResponse.EmployeeResult.ContactResponse.class,
                    "contact");

            return List.of(
                    notification(ENTER_NODE, root),
                    notification(ENTER_NODE, node1),
                    notification(LEAVE_NODE, node1),
                    notification(ENTER_NODE, node2),
                    notification(LEAVE_NODE, node2),
                    notification(LEAVE_NODE, root)
            );
        }

        @Override
        public String expectedGenerate() {
           return expand.expectedGenerate();
        }
    }

    @Override
    public List<CtsTestCase> testCases() {
        return List.of(
                new Select(),
                new Expand(),
                new ExpandWithBlocker()
        );
    }
}
