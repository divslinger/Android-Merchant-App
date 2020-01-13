package com.bitcoin.merchant.app.model.websocket;

import java.util.Arrays;

// {
// "txid":"ABCDEF...", "fees":0, "confirmations":0, "amount":27420,
// "outputs": [{"address":"1...", "value":27420}]
// }
@Deprecated
public class Tx {
    // do NOT change names as they are used by Gson
    public String txid;
    public long fees;
    public long confirmations;
    public long amount;
    public Output[] outputs;

    @Override
    public String toString() {
        return "Tx{" +
                "txid='" + txid + '\'' +
                ", fees=" + fees +
                ", confirmations=" + confirmations +
                ", amount=" + amount +
                ", outputs=" + Arrays.toString(outputs) +
                '}';
    }

    public static class Output {
        public String address;
        public long value;

        @Override
        public String toString() {
            return "Output{" +
                    "address='" + address + '\'' +
                    ", value=" + value +
                    '}';
        }
    }
}
