package info.blockchain.merchant.tabsswipe;

import java.text.SimpleDateFormat;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.math.BigInteger;
import java.net.URL;

import android.content.Context;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
//import android.widget.ImageView;
import android.widget.ListView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.util.Log;

import com.google.bitcoin.uri.BitcoinURI;
import com.markupartist.android.widget.PullToRefreshListView;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;

import org.apache.commons.io.IOUtils;

import info.blockchain.api.*;
import info.blockchain.merchant.db.DBController;
import info.blockchain.merchant.R;

public class TransactionsFragment extends ListFragment	{
    
    private static String receiving_address = null;
	private SharedPreferences prefs = null;
	private List<ContentValues> mListItems;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    ViewGroup viewGroup = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

	    View lvOld = viewGroup.findViewById(android.R.id.list);

	    final PullToRefreshListView listView = new PullToRefreshListView(getActivity());
	    listView.setId(android.R.id.list);
	    listView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	    listView.setDrawSelectorOnTop(false);

	    FrameLayout parent = (FrameLayout) lvOld.getParent();

	    parent.removeView(lvOld);
	    lvOld.setVisibility(View.GONE);

	    parent.addView(listView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	    
	    // Set a listener to be invoked when the list should be refreshed.
        ((PullToRefreshListView)listView).setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetDataTask().execute();
            }
        });

        mListItems = new ArrayList<ContentValues>();
        setListAdapter(new TransactionAdapter());

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        receiving_address = prefs.getString("receiving_address", "");

	    return viewGroup;
	}

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser) {
            new GetDataTask().execute();
        }
        else {
        	;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.i("TransactionsFragment", "Item clicked: " + id);
    }

    private class GetDataTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
        	
        	if(receiving_address.length() > 0) {

        		//
        		// get database list 
        		// first confront with tcs pulled in from Blaockchain API
        		// then use for display
        		//
                DBController pdb = new DBController(getActivity());
                ArrayList<ContentValues> vals = pdb.getAllPayments();
                ArrayList<String> addrs = new ArrayList<String>();
                for(ContentValues v : vals) {
                	addrs.add(v.getAsString("iad"));
                }

//                Wallet wallet = new Wallet(receiving_address, 50);
                WalletReceipts wallet = new WalletReceipts(receiving_address, 50);
                String json = null;
                try {
                    json = IOUtils.toString(new URL(wallet.getUrl()), "UTF-8");
                    wallet.setData(json);
                    wallet.parse();
                }
                catch(Exception e) {
                	e.printStackTrace();
                }

                List<Tx> txs = wallet.getTxs();
                if(txs != null && txs.size() > 0) {
//                	Collections.sort(txs, new TxComparator());
                    Log.i("Update confirmed", "txs returned:" + txs.size());
                    for (Tx t : txs) {
                    	if(t.getIncomingAddresses().size() > 0) {
                            Log.i("Update confirmed", "addresses for this tx:" + t.getIncomingAddresses().size());
                    		List<String> incoming_addresses = t.getIncomingAddresses();
                            for (String incoming : incoming_addresses) {
                                Log.i("Update confirmed", incoming + ", confirmed:" + t.isConfirmed());
                            	if(addrs.contains(incoming)) {
                            		if(t.isConfirmed()) {
                                        Log.i("Update confirmed", "updating database for incoming (confirmed):" + incoming);
                                		pdb.updateConfirmed(incoming, 1);
                            		}
                            		else {
                                        Log.i("Update confirmed", "updating database for incoming (received):" + incoming);
                                		pdb.updateConfirmed(incoming, 0);
                            		}
                            	}
                            }
                    	}
                    }
                }
                
                // get updated list from database
                vals = pdb.getAllPayments();
                pdb.close();
                
                if(vals.size() > 0) {
                	mListItems.clear();
                	mListItems.addAll(vals);
                }

        	}

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {

            ((PullToRefreshListView) getListView()).onRefreshComplete();

            super.onPostExecute(result);
        }
    }

    private class TransactionAdapter extends BaseAdapter {
    	
		private LayoutInflater inflater = null;
	    private SimpleDateFormat sdf = null;

	    TransactionAdapter() {
	        inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");
		}

		@Override
		public int getCount() {
			return mListItems.size();
		}

		@Override
		public String getItem(int position) {
	        return mListItems.get(position).getAsString("iad");
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view;
	        
	        if (convertView == null) {
	            view = inflater.inflate(R.layout.fragment_transactions, parent, false);
	        } else {
	            view = convertView;
	        }
	 
	        ContentValues vals = mListItems.get(position);
	        ((TextView)view.findViewById(R.id.tv_date)).setText(sdf.format(new Date(vals.getAsLong("ts") * 1000)));
	        ((TextView)view.findViewById(R.id.tv_note)).setText(vals.getAsString("msg"));
	        ((TextView)view.findViewById(R.id.tv_amount)).setText(BitcoinURI.bitcoinValueToPlainString(BigInteger.valueOf(vals.getAsLong("amt"))) + " BTC");
	        if(vals.getAsInteger("cfm") > 0) {
		        ((TextView)view.findViewById(R.id.tv_status)).setTextColor(Color.parseColor("#00994C"));
		        ((TextView)view.findViewById(R.id.tv_status)).setText("✔✔");
	        }
	        else if(vals.getAsInteger("cfm") == 0) {
		        ((TextView)view.findViewById(R.id.tv_status)).setTextColor(Color.parseColor("#00994C"));
		        ((TextView)view.findViewById(R.id.tv_status)).setText("✓");
	        }
	        else {
		        ((TextView)view.findViewById(R.id.tv_status)).setTextColor(Color.RED);
		        ((TextView)view.findViewById(R.id.tv_status)).setText("✘");
	        }
	 
	        return view;
		}

    }

    /*
    private class TxComparator implements Comparator<Tx> {
        @Override
        public int compare(Tx o1, Tx o2) {
            return (o1.getTime() < o2.getTime()) ? 1 : 0;
        }
    }
    */

}
