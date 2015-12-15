package info.blockchain.merchant.api;

import android.content.Context;
//import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import info.blockchain.merchant.util.PrefsUtil;
import info.blockchain.wallet.util.WebUtil;

public class APIFactory	{

    private static String RECEIVE_PAYMENTS_API_KEY = null;

    private static int account_index = 0;

    private static APIFactory instance = null;
    private static Context context = null;

    private APIFactory()	{ ; }

    public static APIFactory getInstance(Context ctx) {

        context = ctx;

        if(instance == null) {
            instance = new APIFactory();
        }

        return instance;
    }

    public String getAPIKey()    {
        return RECEIVE_PAYMENTS_API_KEY;
    }

    public int getAccountIndex() {
        return account_index;
    }

    public void setAccountIndex(int index) {
        account_index = index;
    }

    public JSONObject getXPUB() {

        JSONObject jsonObject  = null;

        try {
            StringBuilder url = new StringBuilder(WebUtil.MULTIADDR_URL);
            url.append(PrefsUtil.getInstance(context).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, ""));
//            Log.i("APIFactory", "XPUB:" + url.toString());
            String response = WebUtil.getInstance().getURL(url.toString());
//            Log.i("APIFactory", "XPUB response:" + response);
            try {
                jsonObject = new JSONObject(response);
                parseXPUB(jsonObject);
            }
            catch(JSONException je) {
                je.printStackTrace();
                jsonObject = null;
            }
        }
        catch(Exception e) {
            jsonObject = null;
            e.printStackTrace();
        }

        return jsonObject;
    }

    public void parseXPUB(JSONObject jsonObject) throws JSONException {

        if(jsonObject != null)  {

            if(jsonObject.has("addresses"))  {
                JSONArray addressesArray = (JSONArray)jsonObject.get("addresses");
                JSONObject addrObj = null;
                for(int i = 0; i < addressesArray.length(); i++)  {
                    addrObj = (JSONObject)addressesArray.get(i);
                    if(addrObj.has("final_balance") && addrObj.has("address"))  {
                        account_index = addrObj.getInt("account_index");
                        PrefsUtil.getInstance(context).setValue(PrefsUtil.MERCHANT_KEY_ACCOUNT_INDEX, account_index);
                    }
                }
            }

        }

    }

}
