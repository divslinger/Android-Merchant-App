package org.bitcoindotcom.bchprocessor

interface Action {
    companion object {
        const val PACKAGE = "org.bitcoindotcom.bchprocessor"
        const val INVOICE_PAYMENT_ACKNOWLEDGED = PACKAGE + "Action.INVOICE_PAYMENT_ACKNOWLEDGED"
        const val INVOICE_PAYMENT_EXPIRED = PACKAGE + "Action.INVOICE_PAYMENT_EXPIRED"
        const val UPDATE_CONNECTION_STATUS = PACKAGE + "Action.UPDATE_CONNECTION_STATUS"
        const val NETWORK_RECONNECT = PACKAGE + "Action.NETWORK_RECONNECT"
        const val PARAM_INVOICE_STATUS = "invoiceStatus"
        const val PARAM_CONNECTION_STATUS_ENABLED = "connectionStatus"
        const val QUERY_ALL_TX_FROM_BITCOIN_COM_PAY = "queryTxFromBitcoinPayServer"
    }
}