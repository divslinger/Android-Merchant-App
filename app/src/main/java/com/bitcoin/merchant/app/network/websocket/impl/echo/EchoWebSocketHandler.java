package com.bitcoin.merchant.app.network.websocket.impl.echo;

import com.bitcoin.merchant.app.network.websocket.impl.TxWebSocketHandlerImpl;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;

public class EchoWebSocketHandler extends TxWebSocketHandlerImpl {
    public EchoWebSocketHandler() {
        TAG = "NoOpSocket";
    }

    @Override
    protected WebSocket createWebSocket(WebSocketFactory factory) throws IOException {
        return factory.createSocket("wss://echo.websocket.org");
    }

    @Override
    protected void parseTx(String message) throws Exception {
    }
}
