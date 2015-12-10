package info.blockchain.merchant.service;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

public class WebSocketHandler {

    private WebSocket mConnection = null;

    private static Context context = null;

    private HashSet<String> sentMessageSet = new HashSet<String>();
    private HashSet<String> addressSet = new HashSet<String>();

    private Timer keepAliveTimer = null;
    private static final long keepAliveTimerInterval = 10000L;

    public WebSocketHandler(Context ctx) {
        this.context = ctx;
    }

    public void send(String message) {
        //Make sure each message is only sent once per socket lifetime
        if(!sentMessageSet.contains(message)) {
            try {
                if (mConnection != null && mConnection.isOpen()) {
                    Log.i("WebSocketHandler", "Websocket subscribe:" +message);
                    mConnection.sendText(message);
                    sentMessageSet.add(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
//            Log.d("WebSocketHandler", "Message sent already: "+message);
        }
    }

    public synchronized void subscribeToAddress(String address) {
        addressSet.add(address);
        send("{\"op\":\"addr_sub\", \"addr\":\"" + address + "\"}");
    }

    public boolean isConnected() {
        return  mConnection != null && mConnection.isOpen();
    }

    public void stop() {

        if(keepAliveTimer != null) keepAliveTimer.cancel();

        if(mConnection != null && mConnection.isOpen()) {
            mConnection.disconnect();
        }
    }

    public void start() {

        try {
            stop();
            connect();

            keepAliveTimer = new Timer();
            keepAliveTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (mConnection != null) {
                        mConnection.sendPing();
                    }
                }
            }, keepAliveTimerInterval, keepAliveTimerInterval);

        }
        catch (IOException | com.neovisionaries.ws.client.WebSocketException e) {
            e.printStackTrace();
        }

    }

    /**
     * Connect to the server.
     */
    private void connect() throws IOException, WebSocketException
    {
        new ConnectionTask().execute();
    }

    private class ConnectionTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... args) {

            try {
                //Seems we make a new connection here, so we should clear our HashSet
//                    Log.d("WebSocketHandler", "Reconnect of websocket..");
                sentMessageSet.clear();

                mConnection = new WebSocketFactory()
                        .createSocket("wss://ws.blockchain.info/inv")
                        .addHeader("Origin", "https://blockchain.info").recreate()
                        .addListener(new WebSocketAdapter() {

                            public void onTextMessage(WebSocket websocket, String message) {
//                                    Log.d("WebSocket", message);

                                try {
                                    JSONObject jsonObject = null;
                                    try {
                                        jsonObject = new JSONObject(message);
                                    } catch (JSONException je) {
//                                            Log.i("WebSocketHandler", "JSONException:" + je.getMessage());
                                        jsonObject = null;
                                    }

                                    if (jsonObject == null) {
//                                            Log.i("WebSocketHandler", "jsonObject is null");
                                        return;
                                    }

//                                        Log.i("WebSocketHandler", jsonObject.toString());

                                    String op = (String) jsonObject.get("op");
                                    if (op.equals("utx") && jsonObject.has("x")) {

                                        JSONObject objX = (JSONObject) jsonObject.get("x");

                                        long value = 0L;
                                        long total_value = 0L;
                                        String in_addr = null;

                                        if (objX.has("inputs")) {
                                            JSONArray inputArray = (JSONArray) objX.get("inputs");
                                            JSONObject inputObj = null;
                                            for (int j = 0; j < inputArray.length(); j++) {
                                                inputObj = (JSONObject) inputArray.get(j);
                                                if (inputObj.has("prev_out")) {
                                                    JSONObject prevOutObj = (JSONObject) inputObj.get("prev_out");
                                                    if (prevOutObj.has("value")) {
                                                        value = prevOutObj.getLong("value");
                                                    }
                                                    if (prevOutObj.has("xpub")) {
                                                        total_value -= value;
                                                    } else if (prevOutObj.has("addr")) {
                                                        if (addressSet.contains((String) prevOutObj.get("addr"))) {
                                                            total_value -= value;
                                                        } else if (in_addr == null) {
                                                            in_addr = (String) prevOutObj.get("addr");
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (objX.has("out")) {
                                            JSONArray outArray = (JSONArray) objX.get("out");
                                            JSONObject outObj = null;
                                            for (int j = 0; j < outArray.length(); j++) {
                                                outObj = (JSONObject) outArray.get(j);
                                                if (outObj.has("value")) {
                                                    value = outObj.getLong("value");
                                                }
                                                if (outObj.has("xpub")) {
                                                    total_value += value;
                                                } else if (outObj.has("addr")) {
                                                    if (addressSet.contains((String) outObj.get("addr"))) {
                                                        total_value += value;
                                                    }
                                                }
                                            }
                                        }

                                        if (total_value > 0L) {

                                            //Incoming tx
                                            Intent intent = new Intent(WebSocketService.ACTION_INTENT_INCOMING_TX);
                                            intent.putExtra("total_value",total_value);
                                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                mConnection.connect();
            }
            catch(Exception e)	{
                e.printStackTrace();
            }

            return null;
        }
    }
}
