package com.bitcoin.merchant.app.network.websocket;

public interface TxWebSocketHandler extends WebSocketHandler {
    void setListener(WebSocketListener webSocketListener);

    void subscribeToAddress(String address);

}
