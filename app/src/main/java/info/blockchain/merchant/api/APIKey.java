package info.blockchain.merchant.api;

public class APIKey	{

    private static final String API_KEY = null;//[--REDACTED--]
    private static final String API_CALLBACK = null;//[--REDACTED--]

    private static APIKey instance = null;

    private APIKey()	{ ; }

    public static APIKey getInstance() {

        if(instance == null) {
            instance = new APIKey();
        }

        return instance;
    }

    public String getKey()    {
        return API_KEY;
    }

    public String getCallback()    {
        return API_CALLBACK;
    }

}
