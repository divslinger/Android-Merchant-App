package info.blockchain.merchant.tabsswipe;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.math.BigInteger;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

import android.content.Context;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ImageView;
//import android.widget.Toast;
import android.view.LayoutInflater;
import android.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.Spannable;
import android.graphics.Typeface;
//import android.util.Log;

import com.google.bitcoin.uri.BitcoinURI;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.encode.QRCodeEncoder;
import com.markupartist.android.widget.PullToRefreshListView;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;

import org.apache.commons.io.IOUtils;

import info.blockchain.api.*;
import info.blockchain.merchant.NotificationData;
import info.blockchain.merchant.db.DBController;
import info.blockchain.merchant.R;
import info.blockchain.util.DateUtil;
import info.blockchain.util.TypefaceUtil;

public class TransactionsFragment extends ListFragment	{
    
    private static String receiving_address = null;
	private SharedPreferences prefs = null;
	private List<ContentValues> mListItems;
	private TransactionAdapter adapter = null;
	private NotificationData notification = null;
    private boolean push_notifications = false;
	private Timer timer = null;
    private PullToRefreshListView listView = null;
    private Typeface btc_font = null;

    private boolean doBTC = false;

    private static int DARK_BG = 0xFFF7F7F7;
    private static int LIGHT_BG = 0xFFFFFFFF;
 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    ViewGroup viewGroup = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

	    View oldView = viewGroup.findViewById(android.R.id.list);

	    listView = new PullToRefreshListView(getActivity());
	    listView.setId(android.R.id.list);
	    listView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	    listView.setDrawSelectorOnTop(false);
	    listView.setBackgroundColor(DARK_BG);
	    listView.setDivider(getActivity().getResources().getDrawable(R.drawable.list_divider));
	    
	    FrameLayout parent = (FrameLayout)oldView.getParent();

	    parent.removeView(oldView);
	    oldView.setVisibility(View.GONE);

	    parent.addView(listView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	    
	    // Set a listener to be invoked when the list should be refreshed.
        ((PullToRefreshListView)listView).setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetDataTask().execute();
            }
        });

        mListItems = new ArrayList<ContentValues>();
        adapter = new TransactionAdapter();
        setListAdapter(adapter);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        receiving_address = prefs.getString("receiving_address", "");
        push_notifications = prefs.getBoolean("push_notifications", false);
        doBTC = prefs.getBoolean("use_btc", false);
        
        btc_font = TypefaceUtil.getInstance(getActivity()).getTypeface();
        
	    return viewGroup;
	}

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser) {

        	receiving_address = prefs.getString("receiving_address", "");
            push_notifications = prefs.getBoolean("push_notifications", false);
            doBTC = prefs.getBoolean("use_btc", false);

            if(push_notifications) {
                if(timer == null) {
                    timer = new Timer();
                    try {
                        timer.scheduleAtFixedRate(new TimerTask() {
                            public void run() {
                                new GetDataTask().execute();
                            }
                    	}, 500L, 1000L * 60L * 10L);	// poll every 10 minutes
                    }
                    catch(IllegalStateException ise) {
                    	;
                    }
                    catch(IllegalArgumentException iae) {
                    	;
                    }
                }
        	}

        }
        else {
        	;
        }
    }

    @Override
    public void onResume() {
    	super.onResume();
     
    	receiving_address = prefs.getString("receiving_address", "");
        push_notifications = prefs.getBoolean("push_notifications", false);
        doBTC = prefs.getBoolean("use_btc", false);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
//        Log.i("TransactionsFragment", "Item clicked: " + id);
        doTxTap(id);
    }

    private class GetDataTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
        	
        	if(receiving_address.length() > 0) {

                WalletReceipts wallet = new WalletReceipts(receiving_address, 10);
                String json = null;
                try {
                    json = IOUtils.toString(new URL(wallet.getUrl()), "UTF-8");
                    wallet.setData(json);
                    wallet.parse();
                }
                catch(MalformedURLException mue) {
                	mue.printStackTrace();
                }
                catch(IOException ioe) {
                	ioe.printStackTrace();
                }

                DBController pdb = new DBController(getActivity());
//                pdb.deleteExpired();
                List<String> confirmedAddresses = pdb.getConfirmedPaymentIncomingAddresses();

                List<Tx> txs = wallet.getTxs();
                if(txs != null && txs.size() > 0) {
//                    Log.i("Update confirmed", "txs returned:" + txs.size());
                    for (Tx t : txs) {
                    	if(t.getIncomingAddresses().size() > 0) {
//                            Log.i("Update confirmed", "addresses for this tx:" + t.getIncomingAddresses().size());
                    		List<String> incoming_addresses = t.getIncomingAddresses();
                            for (String incoming : incoming_addresses) {
//                                Log.i("Update confirmed", incoming + ", confirmed:" + t.isConfirmed());
                        		if(t.isConfirmed()) {
//                                    Log.i("Update confirmed", "updating database for incoming (confirmed):" + incoming);
                                    if(pdb.updateConfirmed(incoming, 1) > 0) {
                                    	if(push_notifications && !confirmedAddresses.contains(incoming)) {
//                                            Log.i("Update confirmed", "push notification:" + incoming);
                                  			String strMarquee = getActivity().getResources().getString(R.string.marquee_start) + " " + incoming;
                                  			String strText = BitcoinURI.bitcoinValueToPlainString(BigInteger.valueOf(t.getAmount())) + " " + getActivity().getResources().getString(R.string.notification_end);
                                  			if(notification != null) {
                                 	    		notification.clearNotification();
                                 	    	}
                                    		notification = new NotificationData(getActivity(), strMarquee, strMarquee, strText, R.drawable.ic_launcher, info.blockchain.merchant.MainActivity.class, 1001);
                                    		notification.setNotification();
                                    	}
                                    }
                        		}
                        		else {
//                                    Log.i("Update confirmed", "updating database for incoming (received):" + incoming);
                            		pdb.updateConfirmed(incoming, 0);
                        		}
                            }
                    	}
                    }
                }
                
                // get updated list from database
                ArrayList<ContentValues> vals = pdb.getAllPayments();
//                pdb.close();
                
                if(vals.size() > 0) {
                	mListItems.clear();
                	mListItems.addAll(vals);
                }
                
        	}

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {

            ((PullToRefreshListView)listView).onRefreshComplete();

            super.onPostExecute(result);
        }
    }

    private class TransactionAdapter extends BaseAdapter {
    	
		private LayoutInflater inflater = null;
	    private SimpleDateFormat sdf = null;

	    TransactionAdapter() {
	        inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

	        boolean bkg = (position % 2 == 0) ? true : false;
	        if(bkg) {
	        	view.setBackgroundColor(DARK_BG);
	        }
	        else {
	        	view.setBackgroundColor(LIGHT_BG);
	        }

	        ContentValues vals = mListItems.get(position);
	        
	        String date_str = DateUtil.getInstance().formatted(vals.getAsLong("ts"));
	        SpannableStringBuilder ds = new SpannableStringBuilder(date_str);
	        if(date_str.indexOf("@") != -1) {
	        	int idx = date_str.indexOf("@");
		        ds.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, idx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		        ds.setSpan(new RelativeSizeSpan(0.75f), idx, date_str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	        }
	        ((TextView)view.findViewById(R.id.tv_date)).setText(ds);

	        ((TextView)view.findViewById(R.id.tv_note)).setText(vals.getAsString("msg"));

	        if(doBTC) {
	        	String displayValue = null;
        		String value = BitcoinURI.bitcoinValueToPlainString(BigInteger.valueOf(vals.getAsLong("amt")));
        		int idx = value.indexOf(".");
        		if(idx != -1) {
        			String tmp = value.substring(idx + 1, value.length());
        			if(vals.getAsLong("amt") < 100000000 && tmp.length() > 4) {
        				displayValue = value.substring(0, idx + 1) + tmp.substring(0, 4);
        			}
        			else if(vals.getAsLong("amt") >= 100000000 && tmp.length() > 3) {
        				displayValue = value.substring(0, idx + 1) + tmp.substring(0, 3);
        			}
        			else {
        				displayValue = value;
        			}
        		}
    			else {
    				displayValue = value;
    			}

    	        TextView btc_view = (TextView)view.findViewById(R.id.tv_btc);
    	        btc_view.setTypeface(btc_font);
    	        SpannableStringBuilder cs = new SpannableStringBuilder(getActivity().getResources().getString(R.string.bitcoin_currency_symbol));
    	        cs.setSpan(new RelativeSizeSpan((float)0.75), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    	        btc_view.setText(cs);
    	        ((TextView)view.findViewById(R.id.tv_amount)).setText(displayValue);
	        }
	        else {
    	        TextView btc_view = (TextView)view.findViewById(R.id.tv_btc);
    	        SpannableStringBuilder cs = new SpannableStringBuilder(vals.getAsString("famt").subSequence(0, 1));
    	        cs.setSpan(new RelativeSizeSpan((float)0.75), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    	        btc_view.setText(cs);
       	        ((TextView)view.findViewById(R.id.tv_amount)).setText(vals.getAsString("famt").substring(1));
	        }

	        if(vals.getAsInteger("cfm") > 0) {
		        ((ImageView)view.findViewById(R.id.tv_status)).setImageResource(R.drawable.filled_checkmark);
	        }
	        else if(vals.getAsInteger("cfm") == 0) {
		        ((ImageView)view.findViewById(R.id.tv_status)).setImageResource(R.drawable.hollow_checkmark);
	        }
	        else {
		        ((ImageView)view.findViewById(R.id.tv_status)).setImageResource(R.drawable.hourglass);
	        }
	 
	        return view;
		}

    }

    private class TxComparator implements Comparator<Tx> {
        @Override
        public int compare(Tx o1, Tx o2) {
            return (o1.getTime() < o2.getTime()) ? 1 : 0;
        }
    }
    
    private void doTxTap(final long item)	{
    	
        final ContentValues val = mListItems.get((int)item);

		SimpleDateFormat sd = new SimpleDateFormat("dd-MM-yyyy@HH:mm");
		String dateStr = sd.format(val.getAsLong("ts") * 1000L);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(dateStr + ": " + BitcoinURI.bitcoinValueToPlainString(BigInteger.valueOf(val.getAsLong("amt"))) + " BTC, " + val.getAsString("famt"));
        builder.setIcon(R.drawable.ic_launcher);
        builder.setItems(new CharSequence[]
                { "View transaction", "Redo scan", "Delete from payments" },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intent = new Intent( Intent.ACTION_VIEW , Uri.parse("https://blockchain.info/address/" + val.getAsString("iad")));
                                startActivity(intent);
                                break;
                            case 1:
                                doRedo(item);
                                break;
                            case 2:
                                doDelete(item);
                                break;
                        }
                    }
                });
        builder.create().show();
    }
    
    private Bitmap generateQRCode(String uri) {

        Bitmap bitmap = null;
        int qrCodeDimension = 380;

        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(uri, null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimension);

    	try {
            bitmap = qrCodeEncoder.encodeAsBitmap();
        } catch (WriterException e) {
            e.printStackTrace();
        }
    	
    	return bitmap;
    }

    private String generateURI(long item) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        ContentValues vals = mListItems.get((int)item);
        return BitcoinURI.convertToBitcoinURI(vals.getAsString("iad"), BigInteger.valueOf(vals.getAsLong("amt")), prefs.getString("receiving_name", ""), vals.getAsString("msg"));
    }

    private void doRedo(long item)	{

    	ImageView image = new ImageView(getActivity());
    	image.setImageBitmap(generateQRCode(generateURI(item)));

        ContentValues val = mListItems.get((int)item);

		SimpleDateFormat sd = new SimpleDateFormat("dd-MM-yyyy@HH:mm");
		String dateStr = sd.format(val.getAsLong("ts") * 1000L);

    	new AlertDialog.Builder(getActivity())
    		.setIcon(R.drawable.ic_launcher)
    		.setTitle(dateStr + ": " + BitcoinURI.bitcoinValueToPlainString(BigInteger.valueOf(val.getAsLong("amt"))) + " BTC, " + val.getAsString("famt"))
    		.setView(image)
    		.setPositiveButton(R.string.prompt_ok, new DialogInterface.OnClickListener() {
//          @Override
    		public void onClick(DialogInterface dialog, int which) {
        		return;
    		}
    	}
    	).show();

    }

    private void doDelete(final long item)	{

        final ContentValues val = mListItems.get((int)item);

		SimpleDateFormat sd = new SimpleDateFormat("dd-MM-yyyy@HH:mm");
		String dateStr = sd.format(val.getAsLong("ts") * 1000L);

    	new AlertDialog.Builder(getActivity())
    		.setIcon(R.drawable.ic_launcher)
    		.setTitle(dateStr + ": " + BitcoinURI.bitcoinValueToPlainString(BigInteger.valueOf(val.getAsLong("amt"))) + " BTC, " + val.getAsString("famt"))
    		.setMessage(R.string.delete_rec)
    		.setPositiveButton(R.string.prompt_yes, new DialogInterface.OnClickListener() {
//          	@Override
    			public void onClick(DialogInterface dialog, int which) {
    				
    				DBController pdb = new DBController(getActivity());
    				pdb.deleteIncomingAddress(val.getAsString("iad"));
    				pdb.close();
    				
    				if(mListItems.size() > 1) {
        				mListItems.remove(item);
    				}
    				else {
        				mListItems.clear();
    				}

    	            new GetDataTask().execute();
    			}
    		})
    		.setNegativeButton(R.string.prompt_no, new DialogInterface.OnClickListener() {
//          	@Override
    			public void onClick(DialogInterface dialog, int which) {
    				return;
    			}
    		}
    	).show();

    }

}
