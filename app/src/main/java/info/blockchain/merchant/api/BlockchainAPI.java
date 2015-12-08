package info.blockchain.merchant.api;

import org.json.JSONException;

public abstract class BlockchainAPI	{

    protected static String strVersion = "0.90";
    protected String strData = null;
    protected String strUrl = null;

    protected BlockchainAPI()	{ ; }

    public BlockchainAPI(String data)	{
        strData = data;
    }

    public static String getVersion()	{
        return strVersion;
    }

    public void setData(String data)	{
        strData = data;
    }

    public String getData()	{
        return strData;
    }

    public void seturl(String url)	{
        strUrl = url;
    }

    public String getUrl()	{
        return strUrl;
    }

    /**
     *
     *
     */
    public abstract void parse();

    protected void handleJSONException(JSONException je)	{
        ;
    }

}
