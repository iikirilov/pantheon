package tech.pegasys.pantheon.tests.web3j.privacy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tech.pegasys.pantheon.tests.acceptance.dsl.privacy.PrivacyNet;

public class SignPMTWithEthSigner extends PrivacyClusterAcceptanceTest{

    protected static final String CONTRACT_NAME = "Event Emitter";

    private EventEmitterHarness eventEmitterHarness;
    private PrivacyNet privacyNet;

    @Before
    public void setUp() throws Exception {
        privacyNet =
                PrivacyNet.builder(privacy, privacyPantheon, cluster, false).addMinerNodeWithEthSigner("Alice").build();
        privacyNet.startPrivacyNet();
        eventEmitterHarness =
                new EventEmitterHarness(
                        privateTransactionBuilder,
                        privacyNet,
                        privateTransactions,
                        privateTransactionVerifier,
                        eea);
    }

    @Test
    public void deployingMustGiveValidReceipt() {
        eventEmitterHarness.deploy(CONTRACT_NAME, "Alice");
    }

    @Test
    public void privateSmartContractMustEmitEvents() {
        eventEmitterHarness.deploy(CONTRACT_NAME, "Alice");
        eventEmitterHarness.store(CONTRACT_NAME, "Alice");
    }

    @Test
    public void privateSmartContractMustReturnValues() {
        eventEmitterHarness.deploy(CONTRACT_NAME, "Alice");
        eventEmitterHarness.store(CONTRACT_NAME, "Alice");
        eventEmitterHarness.get(CONTRACT_NAME, "Alice");
    }

    @After
    public void tearDown() {
        privacyNet.stopPrivacyNet();
    }
}
