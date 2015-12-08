package info.blockchain.merchant.api;

import java.util.List;
import java.util.ArrayList;

/**
 * Bitcoin transaction
 *
 */
public class Tx {

    private static final String TAG = "Tx";

    private long block_height = -1L;
    private long time;
    private String hash;
    private List<String> incoming_addresses = null;
    private long amount = -1L;

    private Tx() { ; }

    /**
     * Constructor for this instance.
     *
     * @param long time
     * @param String hash (tx) for this transaction
     * @param long height block height of this transaction
     *
     */
    public Tx(long time, String hash, long height) {
        this.time = time;
        this.hash = hash;
        this.block_height = height;
        incoming_addresses = new ArrayList<String>();
    }

    /**
     * Constructor for this instance.
     *
     * @param long time
     * @param String hash (tx) for this transaction
     *
     */
    public Tx(long time, String hash) {
        this.time = time;
        this.hash = hash;
        incoming_addresses = new ArrayList<String>();
    }

    /**
     * Get the time of this transaction.
     *
     * @return long time of this transaction
     *
     */
    public long getTime() {
        return time;
    }

    /**
     * Set the time of this transaction.
     *
     * @param long time of this transaction
     *
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * Get the amount of this transaction.
     *
     * @return long amount of this transaction
     *
     */
    public long getAmount() {
        return amount;
    }

    /**
     * Set the amount of this transaction.
     *
     * @param long amount of this transaction
     *
     */
    public void setAmount(long amount) {
        this.amount = amount;
    }

    /**
     * Get the hash of this transaction.
     *
     * @return String hash of this transaction
     *
     */
    public String getHash() {
        return hash;
    }

    /**
     * Set the hash of this transaction.
     *
     * @param String hash of this transaction
     *
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * Get the block height of this transaction.
     *
     * @return long block height of this transaction
     *
     */
    public long getBlockHeight() {
        return block_height;
    }

    /**
     * Set the block height of this transaction.
     *
     * @param long block height of this transaction
     *
     */
    public void setBlockHeight(long height) {
        this.block_height = height;
    }

    /**
     * Set the block height of this transaction.
     *
     * @return List<String> incoming addresses for this tx
     *
     */
    public List<String> getIncomingAddresses() {
        return incoming_addresses;
    }

    public void setAddress(String address) {
        incoming_addresses.add(address);
    }

    public void setAddresses(List<String> addresses) {
        incoming_addresses = addresses;
    }

    /**
     * Returns whether this transaction has at least 1 confirmation.
     *
     * @param long latest block height
     *
     * @return boolean
     *
     */
    /*
    public boolean isConfirmed(long latest) {
    	return (latest >= block_height);
    }
    */

    /**
     * Returns whether this transaction has at least 1 confirmation.
     *
     * @return boolean
     *
     */
    public boolean isConfirmed() {
        return (block_height > 0L);
    }

    /**
     * Get the number of confirmations for this transaction.
     *
     * @param long latest block height
     *
     * @return long number of confirmations for this transaction
     *
     */
    public long confirmations(long latest) {
        if(block_height > 0L) {
            return (latest - block_height) + 1L;
        }
        else {
            return 0L;
        }
    }

}
