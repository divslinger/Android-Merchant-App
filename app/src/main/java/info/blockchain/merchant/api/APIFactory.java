package info.blockchain.merchant.api;

import android.content.Context;
//import android.util.Log;

public class APIFactory	{

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
        return APIKey.getInstance().getKey();
    }

    public String getCallback()    {
        return APIKey.getInstance().getCallback();
    }

}
