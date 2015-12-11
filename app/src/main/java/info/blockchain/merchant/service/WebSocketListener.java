package info.blockchain.merchant.service;

public interface WebSocketListener {
    void onIncomingPayment(long paymentAmount);
}
