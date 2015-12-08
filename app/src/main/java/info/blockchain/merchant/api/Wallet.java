package info.blockchain.merchant.api;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

//import android.util.Log;

/**
 * This class obtains info on a Bitcoin wallet address from Blockchain.info.
 *
 */
public class Wallet extends BlockchainAPI {

    private static final String TAG = "Wallet";

    private long latest_block = -1L;
    private List<Tx> txList = null;
    private String strAddress = null;

    /**
     * Constructor for this instance.
     *
     * @param String address wallet address for this instance.
     * @param int limit if limit > 0, this instance will only include this number of transactions.
     *
     */
    public Wallet(String address, int limit) {
        strAddress = address;
        strUrl = "https://blockchain.info/address/" + strAddress + "?format=json";
        if(limit > 0) {
            strUrl += "&limit=" + limit + "&offset=0";
        }
//		Log.d(TAG, strUrl);
    }

    /**
     * Set latest block height.
     *
     * @param long latest block height
     *
     */
    public void setLatestBlockHeight(long height)	{
        latest_block = height;
    }

    /**
     * This method returns the list of transactions for the wallet represented by this instance.
     *
     * @return List<Tx> list of transactions
     */
    public List<Tx> getTxs()	{
        return txList;
    }

    /**
     * Parse the data supplied to this instance.
     *
     */
    /*
    public void parse()	{
    	
    	txList = new ArrayList<Tx>();

        try {
    		JSONObject jsonObject = new JSONObject(strData);
    		if(jsonObject != null)	{
    			Log.d(TAG, "Object OK");
    			JSONArray txArray = jsonObject.getJSONArray("txs");
        		if(txArray != null)	{
        			Log.d(TAG, "Array OK, length=" + txArray.length());
        			JSONObject jtx = null;
        			for(int i = 0; i < txArray.length(); i++)	{
        				jtx = txArray.getJSONObject(i);

            			long height = 0L;
            			String hash = null;
            			long time = 0L;
            			if(jtx.has("block_height"))	{
                			height = jtx.getLong("block_height");
            			}
            			hash = jtx.getString("hash");
            			time = jtx.getLong("time");

            			Log.d(TAG, "Time:" + time + ", Height:" + height + ", Hash:" + hash);

            			if(height < 1L)	{
            				txList.add(new Tx(time, hash));
            			}
            			else	{
            				txList.add(new Tx(time, hash, height));
            			}
            			
        			}
        		}
    		}
    	} catch (JSONException je) {
    		je.printStackTrace();
    	}

    }
    */

    /**
     * Parse the data supplied to this instance.
     *
     */
    public void parse()	{

        txList = new ArrayList<Tx>();

        try {
            JSONObject jsonObject = new JSONObject(strData);
            if(jsonObject != null)	{
                JSONArray txArray = jsonObject.getJSONArray("txs");
                if(txArray != null)	{
                    JSONObject jtx = null;
                    for(int i = 0; i < txArray.length(); i++)	{
                        jtx = txArray.getJSONObject(i);

                        long height = 0L;
                        String hash = null;
                        long time = 0L;
                        if(jtx.has("block_height"))	{
                            height = jtx.getLong("block_height");
                        }
                        hash = jtx.getString("hash");
                        time = jtx.getLong("time");

                        ArrayList<String> addrs = new ArrayList<String>();
                        long value = 0L;

                        if(jtx.has("inputs"))	{
                            JSONArray inputs = jtx.getJSONArray("inputs");
                            for(int j = 0; j < inputs.length(); j++)	{
                                JSONObject input = inputs.getJSONObject(j);
                                JSONObject prev_out = input.getJSONObject("prev_out");
                                if(prev_out != null)	{
                                    if(prev_out.has("addr"))	{
                                        String addr = prev_out.getString("addr");
                                        addrs.add(addr);
                                    }
                                }
                            }
                        }

                        if(jtx.has("out"))	{
                            JSONArray outs = jtx.getJSONArray("out");
                            for(int k = 0; k < outs.length(); k++)	{
                                JSONObject out = outs.getJSONObject(k);
                                if(out.has("addr"))	{
                                    String addr = out.getString("addr");
                                    if(addr.equals(strAddress))	{
                                        // only accept incoming payments
                                        long val = out.getLong("value");
                                        value = val;

                                        Tx tx = null;
                                        if(height < 1L)	{
                                            tx = new Tx(time, hash);
                                        }
                                        else	{
                                            tx = new Tx(time, hash, height);
                                        }
                                        tx.setAddresses(addrs);
                                        tx.setAmount(value);
                                        txList.add(tx);
                                    }
                                }
                            }
                        }

                    }
                }
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }

    }

}
