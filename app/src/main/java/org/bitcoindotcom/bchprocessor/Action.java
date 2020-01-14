package org.bitcoindotcom.bchprocessor;

public interface Action {
    String PACKAGE = "org.bitcoindotcom.bchprocessor";
    String INVOICE_PAYMENT_ACKNOWLEDGED = PACKAGE + "Action.INVOICE_PAYMENT_ACKNOWLEDGED";
    String INVOICE_PAYMENT_EXPIRED = PACKAGE + "Action.INVOICE_PAYMENT_EXPIRED";
    String NETWORK_RECONNECT = PACKAGE + "Action.NETWORK_RECONNECT";
    String PARAM_INVOICE_STATUS = "invoiceStatus";
    String QUERY_ALL_TX_FROM_BITCOIN_COM_PAY = "queryTxFromBitcoinPayServer";
}
