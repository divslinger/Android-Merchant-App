package com.bitcoin.merchant.app.model.rest;

import java.util.Arrays;

/*
{
  "utxos": [
    {
      "txid": "0166...",
      "vout": 0,
      "amount": 0.0001511,
      "satoshis": 15110,
      "height": 593285,
      "confirmations": 0,
      "ts": 1564306265
    }
  ],
  "legacyAddress": "1E...",
  "cashAddress": "bitcoincash:qzfw...",
  "slpAddress": "simpleledger:qzfw..",
  "scriptPubKey": "76a9..."
}
 */
// https://rest.bitcoin.com/v2/address/unconfirmed/#
// https://rest.bitcoin.com/v2/address/utxo/#
@Deprecated
public class Utxos {
    // do NOT change names as they are used by Gson
    public String legacyAddress;
    public String cashAddress;
    public Utxo[] utxos;

    @Override
    public String toString() {
        return "Utxos{" +
                "legacyAddress='" + legacyAddress + '\'' +
                ", cashAddress='" + cashAddress + '\'' +
                ", utxos=" + Arrays.toString(utxos) +
                '}';
    }
}
