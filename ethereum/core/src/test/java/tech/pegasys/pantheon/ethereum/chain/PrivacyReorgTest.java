package tech.pegasys.pantheon.ethereum.chain;

import org.junit.Test;
import tech.pegasys.pantheon.ethereum.core.Block;
import tech.pegasys.pantheon.ethereum.core.BlockDataGenerator;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.core.TransactionReceipt;
import tech.pegasys.pantheon.ethereum.mainnet.MainnetBlockHeaderFunctions;
import tech.pegasys.pantheon.ethereum.storage.keyvalue.KeyValueStoragePrefixedKeyBlockchainStorage;
import tech.pegasys.pantheon.metrics.noop.NoOpMetricsSystem;
import tech.pegasys.pantheon.services.kvstore.InMemoryKeyValueStorage;
import tech.pegasys.pantheon.services.kvstore.KeyValueStorage;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class PrivacyReorgTest {
    @Test
    public void appendBlockWithReorgToChainAtEqualHeight() {
        final BlockDataGenerator gen = new BlockDataGenerator(1);

        // Setup an initial blockchain
        final int chainLength = 3;
        final List<Block> chain = gen.blockSequence(chainLength);
        final List<List<TransactionReceipt>> blockReceipts =
                chain.stream().map(gen::receipts).collect(Collectors.toList());
        final KeyValueStorage kvStore = new InMemoryKeyValueStorage();
        final DefaultBlockchain blockchain = createMutableBlockchain(kvStore, chain.get(0));
        for (int i = 1; i < chain.size(); i++) {
            blockchain.appendBlock(chain.get(i), blockReceipts.get(i));
        }
        assertThat(blockchain.getForks()).isEmpty();
        final Block originalHead = chain.get(chainLength - 1);

        // TODO: set-up the privacy components here
        // new PrivacyPrecompiledContract()

        // Create parallel fork of length 1
        final int forkBlock = 2;
        final int commonAncestor = 1;
        final BlockDataGenerator.BlockOptions options =
                new BlockDataGenerator.BlockOptions()
                        .setParentHash(chain.get(commonAncestor).getHash())
                        .setBlockNumber(forkBlock)
                        .setDifficulty(chain.get(forkBlock).getHeader().getDifficulty().plus(10L));
        final Block fork = gen.block(options);
        final List<TransactionReceipt> forkReceipts = gen.receipts(fork);
        final List<Block> reorgedChain = new ArrayList<>(chain.subList(0, forkBlock));
        reorgedChain.add(fork);
        final List<List<TransactionReceipt>> reorgedReceipts =
                new ArrayList<>(blockReceipts.subList(0, forkBlock));
        reorgedReceipts.add(forkReceipts);

        // Add fork
        blockchain.appendBlock(fork, forkReceipts);

        // Check chain has reorganized
        for (int i = 0; i < reorgedChain.size(); i++) {
            assertBlockDataIsStored(blockchain, reorgedChain.get(i), reorgedReceipts.get(i));
        }
        // Check old transactions have been removed
        for (final Transaction tx : originalHead.getBody().getTransactions()) {
            assertThat(blockchain.getTransactionByHash(tx.hash())).isNotPresent();
        }

        assertBlockIsHead(blockchain, fork);
        assertTotalDifficultiesAreConsistent(blockchain, fork);
        // Old chain head should now be tracked as a fork.
        final Set<Hash> forks = blockchain.getForks();
        assertThat(forks.size()).isEqualTo(1);
        assertThat(forks.stream().anyMatch(f -> f.equals(originalHead.getHash()))).isTrue();
        // Old chain should not be on canonical chain.
        for (int i = commonAncestor + 1; i < chainLength; i++) {
            assertThat(blockchain.blockIsOnCanonicalChain(chain.get(i).getHash())).isFalse();
        }
    }

    /*
     * Check that block header, block body, block number, transaction locations, and receipts for this
     * block are all stored.
     */
    private void assertBlockDataIsStored(
            final Blockchain blockchain, final Block block, final List<TransactionReceipt> receipts) {
        final Hash hash = block.getHash();
        assertEquals(hash, blockchain.getBlockHashByNumber(block.getHeader().getNumber()).get());
        assertEquals(block.getHeader(), blockchain.getBlockHeader(block.getHeader().getNumber()).get());
        assertEquals(block.getHeader(), blockchain.getBlockHeader(hash).get());
        assertEquals(block.getBody(), blockchain.getBlockBody(hash).get());
        assertThat(blockchain.blockIsOnCanonicalChain(block.getHash())).isTrue();

        final List<Transaction> txs = block.getBody().getTransactions();
        for (int i = 0; i < txs.size(); i++) {
            final Transaction expected = txs.get(i);
            final Transaction actual = blockchain.getTransactionByHash(expected.hash()).get();
            assertEquals(expected, actual);
        }
        final List<TransactionReceipt> actualReceipts = blockchain.getTxReceipts(hash).get();
        assertEquals(receipts, actualReceipts);
    }

    private void assertBlockIsHead(final Blockchain blockchain, final Block head) {
        assertEquals(head.getHash(), blockchain.getChainHeadHash());
        assertEquals(head.getHeader().getNumber(), blockchain.getChainHeadBlockNumber());
        assertEquals(head.getHash(), blockchain.getChainHead().getHash());
    }

    private void assertTotalDifficultiesAreConsistent(final Blockchain blockchain, final Block head) {
        // Check that total difficulties are summed correctly
        long num = BlockHeader.GENESIS_BLOCK_NUMBER;
        UInt256 td = UInt256.of(0);
        while (num <= head.getHeader().getNumber()) {
            final Hash curHash = blockchain.getBlockHashByNumber(num).get();
            final BlockHeader curHead = blockchain.getBlockHeader(curHash).get();
            td = td.plus(curHead.getDifficulty());
            assertEquals(td, blockchain.getTotalDifficultyByHash(curHash).get());

            num += 1;
        }

        // Check reported chainhead td
        assertEquals(td, blockchain.getChainHead().getTotalDifficulty());
    }

    private DefaultBlockchain createMutableBlockchain(
            final KeyValueStorage kvStore, final Block genesisBlock) {
        return (DefaultBlockchain)
                DefaultBlockchain.createMutable(
                        genesisBlock, createStorage(kvStore), new NoOpMetricsSystem());
    }

    private BlockchainStorage createStorage(final KeyValueStorage kvStore) {
        return new KeyValueStoragePrefixedKeyBlockchainStorage(
                kvStore, new MainnetBlockHeaderFunctions());
    }
}
