package tech.pegasys.pantheon.tests.acceptance.dsl.privacy.contract;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.Contract;
import org.web3j.tx.LegacyPrivateTransactionManager;
import org.web3j.tx.PrivateTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.PantheonPrivacyGasProvider;
import org.web3j.utils.Base64String;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.NodeRequests;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.Transaction;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.List;

public class PrivateLoadSmartContractTransaction<T extends Contract> implements Transaction<T> {
    private static PantheonPrivacyGasProvider GAS_POROVIDER =
            new PantheonPrivacyGasProvider(BigInteger.valueOf(1000));
    private static final Object METHOD_IS_STATIC = null;

    private final Class<T> clazz;
    private final Credentials senderCredentials;
    private final long chainId;
    private final Base64String privateFrom;
    private final List<Base64String> privateFor;
    private String contractAddress;

    public PrivateLoadSmartContractTransaction(final String contractAddress, final Class<T> clazz, final String transactionSigningKey, final long chainId, final String privateFrom, final List<String> privateFor) {

        this.contractAddress = contractAddress;
        this.clazz = clazz;
        this.senderCredentials = Credentials.create(transactionSigningKey);
        this.chainId = chainId;
        this.privateFrom = Base64String.wrap(privateFrom);
        this.privateFor = Base64String.wrapList(privateFor);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T execute(NodeRequests node) {
        final PrivateTransactionManager privateTransactionManager =
                new LegacyPrivateTransactionManager(
                        node.privacy().getPantheonClient(),
                        GAS_POROVIDER,
                        senderCredentials,
                        chainId,
                        privateFrom,
                        privateFor);
        try {
            final Method method =
                    clazz.getMethod(
                            "load", String.class, Web3j.class, TransactionManager.class, ContractGasProvider.class);

            return (T) method.invoke(
                    METHOD_IS_STATIC,
                    contractAddress,
                    node.privacy().getPantheonClient(),
                    privateTransactionManager,
                    GAS_POROVIDER);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
